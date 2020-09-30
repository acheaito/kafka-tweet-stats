package com.cheaito.twitter.serializers;

import com.cheaito.twitter.domain.Tweet;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import javax.enterprise.inject.Produces;
import java.io.IOException;

public class ObjectMapperFactory {

    @Produces
    public static ObjectMapper instance() {
        SimpleModule tweetModule = new SimpleModule().addDeserializer(Tweet.class,
                new JsonDeserializer<Tweet>() {
                    @Override
                    public Tweet deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
                        TreeNode data = p.readValueAsTree().get("data");
                        return new Tweet(data.get("id").toString(),
                                data.get("lang").toString(),
                                data.get("text").toString());
                    }
                });
        return new ObjectMapper().registerModule(tweetModule);
    }
}
