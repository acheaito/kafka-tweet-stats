package com.cheaito.twitter.kafka.consumer;

import com.cheaito.twitter.model.Tweet;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.time.Duration;
import java.util.Collections;

public class AnalyticsConsumer {
    private final Consumer<String, Tweet> consumer;
    private final ConsumerRecordHandler<String, Tweet> recordHandler;
    private final String topicName;
    private volatile boolean keepConsuming = true;

    public AnalyticsConsumer(Consumer<String, Tweet> consumer, ConsumerRecordHandler<String, Tweet> recordHandler, String topicName) {
        this.consumer = consumer;
        this.recordHandler = recordHandler;
        this.topicName = topicName;
    }

    public void runConsume() {
        try {
            consumer.subscribe(Collections.singletonList(topicName));
            while (keepConsuming) {
                ConsumerRecords<String, Tweet> records = consumer.poll(Duration.ofMillis(10000));
                recordHandler.process(records);
            }
        } finally {
            consumer.close();
        }
    }

    public void shutdown() {
        keepConsuming = false;
    }
}
