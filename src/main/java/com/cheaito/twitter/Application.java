package com.cheaito.twitter;

import com.cheaito.twitter.digester.TwitterDigester;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import java.io.IOException;
import java.net.URISyntaxException;

public class Application {
    public static void main(String[] args) throws IOException, URISyntaxException {
        Weld weld = new Weld();
        WeldContainer weldContainer = weld.initialize();
        TwitterDigester application = weldContainer.select(TwitterDigester.class).get();
        application.start();
        weld.shutdown();
    }
}
