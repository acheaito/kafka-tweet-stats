package com.cheaito.twitter;

import java.io.IOException;
import java.net.URISyntaxException;

public class Application {
    public static void main(String[] args) throws IOException, URISyntaxException {
        new TwitterDigester().start();
    }
}
