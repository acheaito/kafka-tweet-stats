package com.cheaito.twitter;

import com.cheaito.twitter.kafka.consumer.AnalyticsConsumer;
import com.cheaito.twitter.kafka.consumer.ConsoleWritingRecordHandler;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.IOException;
import java.util.Properties;

public class TweetConsumerApplication {
    private final Properties props;

    public TweetConsumerApplication() throws IOException {
        this.props = new Properties();
        props.load(this.getClass().getClassLoader().getResourceAsStream("kafka.properties"));
    }

    public static void main(String[] args) throws IOException {
        new TweetConsumerApplication().start();
    }

    private void start() {
        AnalyticsConsumer analyticsConsumer = createConsumer();
        Runtime.getRuntime().addShutdownHook(new Thread(analyticsConsumer::shutdown));
        analyticsConsumer.runConsume();
    }

    public AnalyticsConsumer createConsumer() {
        return new AnalyticsConsumer(new KafkaConsumer<>(props),
                new ConsoleWritingRecordHandler(),
                props.getProperty("topic.name"));
    }
}
