package com.cheaito.twitter.serializers;

import com.cheaito.twitter.model.Tweet;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TweetDeserializerTest {
    private final static String SIMPLE_TWEET = "{\"id\": \"1\", \"lang\": \"en\", \"text\": \"Hello world\"}";
    private TweetDeserializer subject;
    private ObjectMapper om;

    @BeforeEach
    public void setupEach() throws Exception {
        subject = new TweetDeserializer();
        om = new ObjectMapper();
    }

    @Test
    public void deserializesSimpleTweet() throws Exception {
        JsonParser parser = om.createParser(SIMPLE_TWEET);
        DeserializationContext context = om.getDeserializationContext();
        Tweet expected = new Tweet("1", "en", "Hello world", Collections.emptyList());
        Tweet actual = subject.deserialize(parser, context);
        assertEquals(expected, actual);
    }

    @Test
    public void handlesMissingEntitiesNode() throws Exception {
        JsonParser parser = om.createParser(SIMPLE_TWEET);
        DeserializationContext context = om.getDeserializationContext();
        Tweet actual = subject.deserialize(parser, context);
        assertTrue(actual.getHashtags().isEmpty());
    }

}