package com.cheaito.twitter.serializers;

import com.cheaito.twitter.domain.Tweet;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Map;

public class TweetKafkaTypeAdapter implements Serializer<Tweet>, Deserializer<Tweet> {
    private final ObjectMapper objectMapper = ObjectMapperFactory.instance();
    private static final StringSerializer stringSerializer = new StringSerializer();
    private static final StringDeserializer stringDeserializer = new StringDeserializer();

    @Override
    public Tweet deserialize(String topic, byte[] bytes) {
        try {
            return objectMapper.readValue(stringDeserializer.deserialize(topic, bytes), Tweet.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public byte[] serialize(String topic, Tweet tweet) {
        ObjectMapper om = new ObjectMapper();
        String serializedTweet = null;
        try {
            serializedTweet = om.writeValueAsString(tweet);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return stringSerializer.serialize(topic, serializedTweet);
    }

    @Override
    public void close() {

    }
}
