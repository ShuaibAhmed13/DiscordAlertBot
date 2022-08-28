import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.github.twitch4j.events.ChannelGoOfflineEvent;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.helix.domain.UserList;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Twitch4JSetup {

    TwitchClient twitchClient;
    public Twitch4JSetup() {
        Dotenv dotenv = Dotenv.configure().load();
        twitchClient = TwitchClientBuilder.builder()
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

    public boolean validateTwitchStreamer(String streamerName) {
        UserList users = twitchClient.getHelix().getUsers(null, null, Arrays.asList(streamerName)).execute();
        if(users.getUsers().isEmpty()) return false;
        return true;
    }
}
