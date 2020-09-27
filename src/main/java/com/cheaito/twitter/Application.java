package com.cheaito.twitter;

import com.cheaito.twitter.digester.TwitterDigester;
import com.cheaito.twitter.domain.Tweet;
import com.cheaito.twitter.producer.TweetProducer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

public class Application {
    public static void main(String[] args) throws IOException, URISyntaxException {
        Properties props = new Properties();
        props.load(Application.class.getClassLoader().getResourceAsStream("kafka.properties"));
        new TwitterDigester(new TweetProducer(new KafkaProducer<>(props), props.getProperty("topic.name"))).start();
    }
}
