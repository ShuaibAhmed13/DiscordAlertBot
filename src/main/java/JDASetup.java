import io.github.cdimascio.dotenv.Dotenv;
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
import java.util.ArrayList;
import java.util.List;

public class JDASetup extends ListenerAdapter {

    private final Twitch4JSetup twitchClient;
    public List<String> guilds;
    public List<String> streamers;
    JDA jda;
    public JDASetup() throws LoginException, InterruptedException {
        jda = JDABuilder.createDefault(Dotenv.configure().load().get("JDABotAPI"))
                .setActivity(Activity.watching("Twitch.tv")).addEventListeners(this)
                .build().awaitReady();
        twitchClient = new Twitch4JSetup();
        guilds = new ArrayList<>();
        streamers = new ArrayList<>();

        jda.upsertCommand("addstreamer", "Type in a Twitch Streamer to get stream alerts for them.")
                .addOption(OptionType.STRING, "streamername", "Type in the streamer's name.", true)
                .queue();
        jda.upsertCommand("streamers", "See the list off all added streamers.").queue();
        jda.upsertCommand("removestreamer", "Remove this streamer from the notification list.")
                .addOption(OptionType.STRING, "streamername", "Type in the streamer's name.", true)
                .queue();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        OptionMapping streamername = event.getOption("streamername");
        event.deferReply().queue();
        if(event.getName().equals("addstreamer")) {
            addStreamer(streamername.getAsString(), event);
        }
        else if(event.getName().equals("streamers")) {
            displayStreamers(event);
        }
        else if(event.getName().equals("removestreamer")) {
            removeStreamer(streamername.getAsString(), event);
        }
        else event.getHook().sendMessage("Whoops, there was a problem.").queue();
    }

    private void addStreamer(String streamerName, SlashCommandInteractionEvent event) {
        boolean valid = twitchClient.validateTwitchStreamer(streamerName);
        if(!valid) event.getHook().sendMessage("This streamer does not exist!").queue();
        else {
            streamers.add(streamerName);
            event.getHook().sendMessage("Streamer added successfully!").queue();
        }
    }

    private void displayStreamers(SlashCommandInteractionEvent event) {
        if(streamers.isEmpty()) event.getHook().sendMessage("The list is empty!").queue();
        else event.getHook().sendMessage(streamers.toString()).queue();
    }

    private void removeStreamer(String streamerName, SlashCommandInteractionEvent event) {
        if(!streamers.contains(streamerName)) event.getHook().sendMessage("This streamer is not on the list.").queue();
        else {
            streamers.remove(streamerName);
            event.getHook().sendMessage("Streamer removed from list.").queue();
        }
    }

//    private void sendStreamAlert() {
//
//    }

}
