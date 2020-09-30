package com.cheaito.twitter.kafka.consumer;

import com.cheaito.twitter.domain.Tweet;
import com.cheaito.twitter.kafka.TopicName;
import org.apache.kafka.clients.consumer.Consumer;

import javax.inject.Inject;
import java.time.Duration;
import java.util.Collections;

public class AnalyticsConsumer {
    private volatile boolean keepConsuming = true;
    private final Consumer<String, Tweet> consumer;
    private final ConsumerRecordHandler<String, Tweet> recordHandler;
    private final String topicName;

    @Inject
    public AnalyticsConsumer(Consumer<String, Tweet> consumer, ConsumerRecordHandler<String, Tweet> recordHandler, @TopicName String topicName) {
        this.consumer = consumer;
        this.recordHandler = recordHandler;
        this.topicName = topicName;
    }

    public void runConsume() {
        try {
            consumer.subscribe(Collections.singletonList(topicName));
            while (keepConsuming) {
                recordHandler.process(consumer.poll(Duration.ofMillis(1000)));
            }
        } finally {
            consumer.close();
        }
    }

    public void shutdown() {
        keepConsuming = false;
    }
}
