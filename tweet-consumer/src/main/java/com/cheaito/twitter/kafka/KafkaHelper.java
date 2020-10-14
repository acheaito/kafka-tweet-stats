package com.cheaito.twitter.kafka;

import com.cheaito.twitter.domain.Tweet;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import javax.enterprise.inject.Produces;
import java.io.IOException;
import java.util.Properties;

public class KafkaHelper {
    private final Properties props;

    public KafkaHelper() throws IOException {
        this.props = new Properties();
        props.load(this.getClass().getClassLoader().getResourceAsStream("kafka.properties"));
    }

    @Produces
    @TopicName
    public String topicName() {
        return props.getProperty("topic.name");
    }

    @Produces
    public Consumer<String, Tweet> consumerInstance() {
        return new KafkaConsumer<>(props);
    }

}
