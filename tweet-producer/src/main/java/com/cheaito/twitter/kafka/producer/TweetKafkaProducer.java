package com.cheaito.twitter.kafka.producer;

import com.cheaito.twitter.domain.Tweet;
import com.cheaito.twitter.kafka.TopicName;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import javax.inject.Inject;
import java.util.Objects;

public class TweetKafkaProducer {
    private final Producer<String, Tweet> producer;
    private final String topic;

    @Inject
    public TweetKafkaProducer(Producer<String, Tweet> producer, @TopicName String topic) {
        this.producer = Objects.requireNonNull(producer, "producer should not be null");
        this.topic = Objects.requireNonNull(topic, "topic should not be null");
    }

    public void produce(Tweet tweet) {
        if (tweet == null) {
            return;
        }
        producer.send(new ProducerRecord<>(topic, tweet.getLang(), tweet));
    }
}
