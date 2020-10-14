package com.cheaito.twitter.serializers;

import com.cheaito.twitter.domain.Tweet;
import com.cheaito.twitter.model.TwitterApiResponse;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
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
                        TreeNode node = p.readValueAsTree();
                        return new Tweet(node.get("id").toString(),
                                node.get("lang").toString(),
                                node.get("text").toString());
                    }
                });
        SimpleModule twitterApiResponseModule = new SimpleModule().addDeserializer(TwitterApiResponse.class,
                new JsonDeserializer<TwitterApiResponse>() {
                    @Override
                    public TwitterApiResponse deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
                        TreeNode node = p.readValueAsTree();
                        return new TwitterApiResponse(node.get("data").traverse(p.getCodec()).readValueAs(Tweet.class));
                    }
                });
        return new ObjectMapper().registerModule(tweetModule)
                .registerModule(twitterApiResponseModule);
    }
}
