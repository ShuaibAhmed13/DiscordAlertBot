import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.github.twitch4j.events.ChannelGoOfflineEvent;
import io.github.cdimascio.dotenv.Dotenv;

public class Twitch4JSetup {
    public Twitch4JSetup() {
        Dotenv dotenv = Dotenv.configure().load();
        TwitchClient twitchClient = TwitchClientBuilder.builder()
                .withEnableHelix(true)
                .withClientId(dotenv.get("TwitchClientID"))
                .withClientSecret(dotenv.get("TwitchClientSecret"))
                .withDefaultEventHandler(SimpleEventHandler.class)
                .build();
        twitchClient.getEventManager().onEvent(ChannelGoLiveEvent.class, event -> {

        });
        twitchClient.getEventManager().onEvent(ChannelGoOfflineEvent.class, event -> {

        });
    }

}
