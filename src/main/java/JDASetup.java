import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.github.twitch4j.events.ChannelGoOfflineEvent;
import com.github.twitch4j.helix.domain.User;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author ahmed
 * @version 1.0
 */
public class JDASetup extends ListenerAdapter {

    private final Twitch4JSetup tc;
    public List<TextChannel> channels;
    public List<User> streamers;
    JDA jda;

    public JDASetup() throws LoginException, InterruptedException {
        jda = JDABuilder.createDefault(Dotenv.configure().load().get("JDABotAPI"))
                .setActivity(Activity.watching("Twitch.tv")).addEventListeners(this)
                .build().awaitReady();
        tc = new Twitch4JSetup();
        tc.twitchClient.getEventManager().onEvent(ChannelGoLiveEvent.class, liveEvent -> {
            sendStreamAlert(liveEvent);
        });
        tc.twitchClient.getEventManager().onEvent(ChannelGoOfflineEvent.class, offlineEvent -> {
            sendOfflineAlert(offlineEvent);
        });
        streamers = new ArrayList<>();
        channels = new ArrayList<>();
        jda.upsertCommand("addstreamer", "Type in a Twitch Streamer to get stream alerts for them.")
                .addOption(OptionType.STRING, "streamername", "Type in the streamer's name.", true)
                .queue();
        jda.upsertCommand("displaystreamers", "See the list off all added streamers.").queue();
        jda.upsertCommand("removestreamer", "Remove this streamer from the notification list.")
                .addOption(OptionType.STRING, "streamername", "Type in the streamer's name.", true)
                .queue();
        jda.upsertCommand("addchannel", "Add a text channel in which you would like the stream alerts.")
                .addOption(OptionType.STRING, "channelname", "Name of channel.", true)
                .queue();
        jda.upsertCommand("displaychannels", "Get a list of all the channels in which you will receive stream notifications.")
                .queue();
        jda.upsertCommand("removechannel", "Remove this channel to stop getting stream alerts there.")
                .addOption(OptionType.STRING, "channelname", "Name of channel.", true)
                .queue();
    }

    /**
     * Commands that a user can select by typing in '/'
     *
     * @param event
     */
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        OptionMapping streamername = event.getOption("streamername");
        OptionMapping channelName = event.getOption("channelname");
        event.deferReply().queue();
        if (event.getName().equals("addstreamer")) {
            addStreamer(streamername.getAsString(), event);
        } else if (event.getName().equals("displaystreamers")) {
            displayStreamers(event);
        } else if (event.getName().equals("removestreamer")) {
            removeStreamer(streamername.getAsString(), event);
        } else if (event.getName().equals("addchannel")) {
            addChannel(channelName.getAsString(), event);
        } else if (event.getName().equals("displaychannels")) {
            displayChannels(event);
        } else if (event.getName().equals("removechannel")) {
            removeChannel(channelName.getAsString(), event);
        } else event.getHook().sendMessage("Whoops, there was a problem.").queue();
    }

    /**
     * Add a streamer to the streamerlist to get notitications when they go live
     *
     * @param streamerName
     * @param event
     */
    private void addStreamer(String streamerName, SlashCommandInteractionEvent event) {
        boolean valid = tc.validateTwitchStreamer(streamerName);
        boolean present = streamers.contains(tc.getTwitchStreamer(streamerName));
        if (!valid) event.getHook().sendMessage("This streamer does not exist!").queue();
        else if (present) event.getHook().sendMessage("This streamer is already on your list!").queue();
        else {
            streamers.add(tc.getTwitchStreamer(streamerName));
            tc.twitchClient.getClientHelper().enableStreamEventListener(streamerName);
            event.getHook().sendMessage("Streamer added successfully!").queue();
        }
    }

    /**
     * Display the list of streamers for whom you are getting notifications
     *
     * @param event
     */
    private void displayStreamers(SlashCommandInteractionEvent event) {
        if (streamers.isEmpty()) event.getHook().sendMessage("The list is empty!").queue();
        else {
            String str = "";
            for (int i = 0; i < streamers.size(); i++) {
                str += streamers.get(i).getDisplayName();
                if (i == streamers.size() - 1) break;
                str += ", ";
            }
            event.getHook().sendMessage(str).queue();
        }
    }

    /**
     * Remove a streamer from your notification list
     *
     * @param streamerName
     * @param event
     */
    private void removeStreamer(String streamerName, SlashCommandInteractionEvent event) {
        if (!streamers.contains(tc.getTwitchStreamer(streamerName)))
            event.getHook().sendMessage("This streamer is not on your list.").queue();
        else {
            streamers.remove(tc.getTwitchStreamer(streamerName));
            tc.twitchClient.getClientHelper().disableStreamEventListener(streamerName);
            event.getHook().sendMessage("Streamer removed from list.").queue();
        }
    }

    /**
     * Add a channel on which you would like to receive streamer notifications
     *
     * @param channelName
     * @param event
     */
    private void addChannel(String channelName, SlashCommandInteractionEvent event) {
        List<TextChannel> ch = event.getGuild().getTextChannelsByName(channelName, true);
        if (!ch.isEmpty()) {
            if (channels.contains(ch.get(0)))
                event.getHook().sendMessage("This channel is already on your list.").queue();
            else {
                this.channels.add(ch.get(0));
                event.getHook().sendMessage("Channel added successfully!").queue();
            }
        } else event.getHook().sendMessage("Please enter a valid channel name.").queue();
    }

    /**
     * Get a list of all channels where streamer notifications are enabled
     *
     * @param event
     */
    private void displayChannels(SlashCommandInteractionEvent event) {
        if (channels.isEmpty()) event.getHook().sendMessage("No channels in the list.").queue();
        else {
            String str = "";
            for (int i = 0; i < channels.size(); i++) {
                str += channels.get(i).getName() + ":" + channels.get(i).getId();
                if (i == channels.size() - 1) break;
                str += ", ";
            }
            event.getHook().sendMessage(str).queue();
        }
    }

    /**
     * Remove this channel from the channellist to avoid getting notifications there
     *
     * @param channelName
     * @param event
     */
    private void removeChannel(String channelName, SlashCommandInteractionEvent event) {
        List<TextChannel> ch = event.getGuild().getTextChannelsByName(channelName, true);
        if (!ch.isEmpty()) {
            if (!channels.contains(ch.get(0))) event.getHook().sendMessage("This channel is not on your list.").queue();
            else {
                channels.remove(ch.get(0));
                event.getHook().sendMessage("Channel successfully removed from list.").queue();
            }
        } else event.getHook().sendMessage("This channel doesn't exist.").queue();

    }

    /**
     * Send an embedded message to all enabled channels notifying people that a streamer went live
     *
     * @param event
     */
    private void sendStreamAlert(ChannelGoLiveEvent event) {
        User streamer = tc.twitchClient.getHelix().getUsers(null, null, Arrays.asList(event.getChannel().getName()))
                .execute().getUsers().get(0);
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(streamer.getDisplayName() + " is live!", "https://twitch.tv/" + streamer.getDisplayName());
        if (streamer.getOfflineImageUrl() != null && streamer.getOfflineImageUrl().length() > 0)
            eb.setImage(streamer.getOfflineImageUrl());
        if (streamer.getProfileImageUrl() != null) eb.setThumbnail(streamer.getProfileImageUrl());
        eb.setAuthor(jda.getSelfUser().getName(), null, jda.getSelfUser().getAvatarUrl());
        eb.setColor(new Color(106, 227, 101));
        channels.forEach(channel -> {
            channel.sendTyping().queue();
            channel.sendMessageEmbeds(eb.build()).queue();
        });
    }

    /**
     * Send an alert (in the form of an embedded message) for when a streamer has gone offline
     *
     * @param event
     */
    private void sendOfflineAlert(ChannelGoOfflineEvent event) {
        User streamer = tc.twitchClient.getHelix().getUsers(null, null, Arrays.asList(event.getChannel().getName()))
                .execute().getUsers().get(0);
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(streamer.getDisplayName() + " has gone offline");
        eb.setColor(new Color(71, 201, 249));
        eb.setAuthor(jda.getSelfUser().getName(), null, jda.getSelfUser().getAvatarUrl());
        if (streamer.getOfflineImageUrl() != null && streamer.getOfflineImageUrl().length() > 0)
            eb.setImage(streamer.getOfflineImageUrl());
        channels.forEach(channel -> {
            channel.sendMessageEmbeds(eb.build()).queue();
        });
    }
}
