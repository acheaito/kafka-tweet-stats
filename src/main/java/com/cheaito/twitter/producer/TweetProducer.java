package com.cheaito.twitter.producer;

import com.cheaito.twitter.domain.Tweet;
import com.cheaito.twitter.serializers.TweetTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.concurrent.Future;

public class TweetProducer {
    private final Producer<String, Tweet> producer;
    private final String topic;
    private final Gson gson;

    public TweetProducer(Producer<String, Tweet> producer, String topic) {
        this.producer = producer;
        this.topic = topic;
        gson = new GsonBuilder()
                .registerTypeAdapter(Tweet.class, new TweetTypeAdapter())
                .create();
    }

    public Future<RecordMetadata> produce(String tweetText) {
        System.out.println("PARSING " + tweetText);
        Tweet tweet = gson.fromJson(tweetText, Tweet.class);
        return this.produce(tweet);
    }

    public Future<RecordMetadata> produce(Tweet tweet) {
        System.out.println("PRODUCING " + tweet.getText());
        return producer.send(new ProducerRecord<>(topic, tweet.getLang(), tweet));
    }
}
