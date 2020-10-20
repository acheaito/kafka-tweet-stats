package com.cheaito.twitter;

import com.cheaito.twitter.client.TwitterClient;
import com.cheaito.twitter.kafka.producer.TweetKafkaProducer;
import com.cheaito.twitter.serializers.ObjectMapperFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.kafka.clients.producer.KafkaProducer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

public class TweetProducerApplication {
    private final Properties props;

    public TweetProducerApplication() throws IOException {
        this.props = new Properties();
        props.load(TweetProducerApplication.class.getClassLoader().getResourceAsStream("kafka.properties"));
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        new TweetProducerApplication().start();
    }

    private void start() throws IOException, URISyntaxException {
        new TwitterClient(HttpClients.createDefault(),
                createProducer(),
                ObjectMapperFactory.instance()).start();
    }

    public TweetKafkaProducer createProducer() {
        return new TweetKafkaProducer(new KafkaProducer<>(props), props.getProperty("topic.name"));
    }
}
