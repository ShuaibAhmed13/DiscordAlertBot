import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;

public class JDASetup extends ListenerAdapter {
    public JDASetup() throws LoginException, InterruptedException {
        JDA jda = JDABuilder.createDefault(Dotenv.configure().load().get("JDABotAPI"))
                .setActivity(Activity.watching("Twitch.tv")).addEventListeners(new JDASetup())
                .build().awaitReady();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        super.onSlashCommandInteraction(event);
    }
}
