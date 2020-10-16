package com.cheaito.twitter;

import com.cheaito.twitter.client.TwitterClient;
import com.cheaito.twitter.kafka.TopicName;
import com.cheaito.twitter.model.Tweet;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import javax.enterprise.inject.Produces;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

public class TweetProducerApplication {
    private final Properties props;

    public static void main(String[] args) throws IOException, URISyntaxException {
        new TweetProducerApplication().start();
    }

    private void start() throws IOException, URISyntaxException {
        Weld weld = new Weld();
        WeldContainer weldContainer = weld.initialize();
        TwitterClient application = weldContainer.select(TwitterClient.class).get();
        application.start();
        weld.shutdown();
    }

    public TweetProducerApplication() throws IOException {
        this.props = new Properties();
        props.load(TweetProducerApplication.class.getClassLoader().getResourceAsStream("kafka.properties"));
    }

    @Produces
    @TopicName
    public String topicName() {
        return props.getProperty("topic.name");
    }

    @Produces
    public Producer<String, Tweet> producerInstance() {
        return new KafkaProducer<>(props);
    }
}
