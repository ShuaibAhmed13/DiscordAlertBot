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
                .addOption(OptionType.STRING, "streamername", "Type in the streamers name", true)
                .queue();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        OptionMapping streamername = event.getOption("streamername");
        if(event.getName().equals("addstreamer")) {
            event.deferReply().queue();
            addStreamer(streamername.getAsString(), event);
        }
    }

    private void addStreamer(String streamerName, SlashCommandInteractionEvent event) {
        boolean valid = twitchClient.validateTwitchStreamer(streamerName);
        if(!valid) event.getHook().sendMessage("This streamer does not exist!").queue();
        else {
            streamers.add(streamerName);
            event.getHook().sendMessage("Streamer added successfully!").queue();
        }
    }

    
}
