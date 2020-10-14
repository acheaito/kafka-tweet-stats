package com.cheaito.twitter;

import com.cheaito.twitter.client.TwitterClient;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import java.io.IOException;
import java.net.URISyntaxException;

public class TweetProducerApplication {
    public static void main(String[] args) throws IOException, URISyntaxException {
        Weld weld = new Weld();
        WeldContainer weldContainer = weld.initialize();
        TwitterClient application = weldContainer.select(TwitterClient.class).get();
        application.start();
        weld.shutdown();
    }
}
