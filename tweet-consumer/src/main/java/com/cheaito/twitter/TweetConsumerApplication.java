package com.cheaito.twitter;

import com.cheaito.twitter.domain.Tweet;
import com.cheaito.twitter.kafka.TopicName;
import com.cheaito.twitter.kafka.consumer.AnalyticsConsumer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import javax.enterprise.inject.Produces;
import java.io.IOException;
import java.util.Properties;

public class TweetConsumerApplication {
    private final Properties props;

    public static void main(String[] args) throws IOException {
        new TweetConsumerApplication().start();
    }

    private void start() {
        Weld weld = new Weld();
        WeldContainer weldContainer = weld.initialize();
        AnalyticsConsumer analyticsConsumer = weldContainer.select(AnalyticsConsumer.class).get();
        Runtime.getRuntime().addShutdownHook(new Thread(analyticsConsumer::shutdown));
        analyticsConsumer.runConsume();
        weld.shutdown();
    }

    public TweetConsumerApplication() throws IOException {
        this.props = new Properties();
        props.load(this.getClass().getClassLoader().getResourceAsStream("kafka.properties"));
    }

    @Produces
    @TopicName
    public String topicName() {
        return props.getProperty("topic.name");
    }

    @Produces
    public Consumer<String, Tweet> consumerInstance() {
        return new KafkaConsumer<>(props);
    }
}
