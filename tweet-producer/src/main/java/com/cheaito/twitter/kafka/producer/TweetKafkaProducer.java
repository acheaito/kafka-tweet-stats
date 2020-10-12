package com.cheaito.twitter.kafka.producer;

import com.cheaito.twitter.domain.Tweet;
import com.cheaito.twitter.kafka.TopicName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import javax.inject.Inject;
import java.util.Objects;

public class TweetKafkaProducer {
    private final Producer<String, Tweet> producer;
    private final String topic;
    private final ObjectMapper objectMapper;

    @Inject
    public TweetKafkaProducer(Producer<String, Tweet> producer, @TopicName String topic, ObjectMapper objectMapper) {
        this.producer = Objects.requireNonNull(producer, "producer should not be null");
        this.topic = Objects.requireNonNull(topic, "topic should not be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "ObjectMapper should be null");
    }

    public void produce(String tweetText) {
        if (tweetText == null) {
            return;
        }
        Tweet tweet = null;
        try {
            tweet = objectMapper.readValue(tweetText, Tweet.class);
            producer.send(new ProducerRecord<>(topic, tweet.getLang(), tweet));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
