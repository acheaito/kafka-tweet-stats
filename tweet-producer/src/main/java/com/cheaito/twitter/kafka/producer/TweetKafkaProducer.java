package com.cheaito.twitter.kafka.producer;


import com.cheaito.twitter.model.Tweet;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class TweetKafkaProducer {
    private final Logger logger = LoggerFactory.getLogger(TweetKafkaProducer.class);
    private final Producer<String, Tweet> producer;
    private final String topic;

    public TweetKafkaProducer(Producer<String, Tweet> producer, String topic) {
        this.producer = Objects.requireNonNull(producer, "producer should not be null");
        this.topic = Objects.requireNonNull(topic, "topic should not be null");
    }

    public void produce(Tweet tweet) {
        if (tweet == null) {
            return;
        }
        logger.debug("Producing message to topic {}: {} - {} - {} - {}", topic, tweet.getId(), tweet.getLang(), tweet.getText(), tweet.getHashtags());
        producer.send(new ProducerRecord<>(topic, tweet.getLang(), tweet));
    }
}
