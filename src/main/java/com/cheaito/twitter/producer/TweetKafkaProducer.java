package com.cheaito.twitter.producer;

import com.cheaito.twitter.domain.Tweet;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import javax.inject.Inject;
import java.util.concurrent.Future;

public class TweetKafkaProducer {
    private final Producer<String, Tweet> producer;
    private final String topic;
    private final ObjectMapper objectMapper;

    @Deprecated
    public TweetKafkaProducer() {
        producer = null;
        topic = null;
        objectMapper = null;
    }

    @Inject
    public TweetKafkaProducer(Producer<String, Tweet> producer, @TopicName String topic, ObjectMapper objectMapper) {
        this.producer = producer;
        this.topic = topic;
        this.objectMapper = objectMapper;
    }

    public Future<RecordMetadata> produce(String tweetText) throws JsonProcessingException {
        System.out.println("PARSING " + tweetText);
        Tweet tweet = objectMapper.readValue(tweetText, Tweet.class);
        return this.produce(tweet);
    }

    public Future<RecordMetadata> produce(Tweet tweet) {
        System.out.println("PRODUCING " + tweet.getText());
        return producer.send(new ProducerRecord<>(topic, tweet.getLang(), tweet));
    }
}
