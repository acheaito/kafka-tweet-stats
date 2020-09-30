package com.cheaito.twitter.producer;

import com.cheaito.twitter.Application;
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
        props.load(Application.class.getClassLoader().getResourceAsStream("kafka.properties"));
    }

    @Produces
    public Producer<String, Tweet> instance() throws IOException {
        return new KafkaProducer<>(props);
    }

    @Produces
    @TopicName
    public String topicName() {
        return props.getProperty("topic.name");
    }
}
