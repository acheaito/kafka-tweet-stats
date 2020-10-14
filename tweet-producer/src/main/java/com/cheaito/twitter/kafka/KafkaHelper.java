package com.cheaito.twitter.kafka;

import com.cheaito.twitter.TweetProducerApplication;
import com.cheaito.twitter.domain.Tweet;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;

import javax.enterprise.inject.Produces;
import java.io.IOException;
import java.util.Properties;

public class KafkaHelper {
    private final Properties props;

    public KafkaHelper() throws IOException {
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
