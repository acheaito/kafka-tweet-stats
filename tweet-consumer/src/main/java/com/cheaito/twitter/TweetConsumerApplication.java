package com.cheaito.twitter;

import com.cheaito.twitter.kafka.consumer.AnalyticsConsumer;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

public class TweetConsumerApplication {
    public static void main(String[] args) {
        Weld weld = new Weld();
        WeldContainer weldContainer = weld.initialize();
        AnalyticsConsumer analyticsConsumer = weldContainer.select(AnalyticsConsumer.class).get();
        Runtime.getRuntime().addShutdownHook(new Thread(analyticsConsumer::shutdown));
        analyticsConsumer.runConsume();
        weld.shutdown();
    }
}
