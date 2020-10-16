package com.cheaito.twitter.serializers;

import com.cheaito.twitter.model.Tweet;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class TweetDeserializerTest {
    private final static String SIMPLE_TWEET = "{\"id\": \"1\", \"lang\": \"en\", \"text\": \"Hello world\"}";
    private final static String TWEET_NO_HASHTAGS = "{\"id\": \"1\", \"lang\": \"en\", \"text\": \"Hello world\", " +
            "\"entities\": {\"mentions\": {}}}";
    private final static String TWEET_WITH_HASHTAGS = "{\"id\": \"1\", \"lang\": \"en\", " +
            "\"text\": \"Hello world #helloworld #hi\", \"entities\": " +
            "{\"mentions\": [], \"hashtags\": [ {\"tag\": \"#helloworld\", \"start\": 12, \"end\": 22}, {\"tag\": \"#hi\"}]}}";

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
        Tweet expected = new Tweet("1", "en", "Hello world", Instant.now().toString(), Collections.emptyList());
        Tweet actual = subject.deserialize(parser, null);
        assertEquals(expected, actual);
    }

    @Test
    public void handlesMissingEntitiesNode() throws Exception {
        JsonParser parser = om.createParser(SIMPLE_TWEET);
        Tweet actual = subject.deserialize(parser, null);
        assertTrue(actual.getHashtags().isEmpty());
    }

    @Test
    public void handlesMissingHashtagsNode() throws Exception {
        JsonParser parser = om.createParser(TWEET_NO_HASHTAGS);
        Tweet actual = subject.deserialize(parser, null);
        assertTrue(actual.getHashtags().isEmpty());
    }

    @Test
    public void handlesHashtags() throws Exception {
        JsonParser parser = om.createParser(TWEET_WITH_HASHTAGS);
        Tweet actual = subject.deserialize(parser, null);
        assertArrayEquals(actual.getHashtags().toArray(), new String[]{"#helloworld", "#hi"});
    }
}