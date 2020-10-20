package com.cheaito.twitter.serializers;

import com.cheaito.twitter.model.Tweet;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TweetDeserializerTest {

    private TweetDeserializer subject;
    private ObjectMapper om;
    private JsonNode exampleTweets;

    @BeforeEach
    public void setupEach() throws Exception {
        subject = new TweetDeserializer();
        om = ObjectMapperFactory.instance();
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("example-tweets.json")) {
            exampleTweets = om.createParser(is).readValueAsTree();
        }
    }

    @Test
    public void deserializesSimpleTweet() throws Exception {
        JsonNode simpleTweet = exampleTweets.get("SIMPLE");
        JsonParser parser = om.createParser(simpleTweet.toString());
        Tweet expected = Tweet.newBuilder()
                .setId("0")
                .setLang("en")
                .setText("Hello world")
                .setCreatedAt(readDateFromExample(simpleTweet).toString())
                .build();

        Tweet actual = subject.deserialize(parser, null);
        assertEquals(expected, actual);
    }

    @Test
    public void handlesMissingEntitiesNode() throws Exception {
        JsonNode simpleTweet = exampleTweets.get("SIMPLE");
        JsonParser parser = om.createParser(simpleTweet.toString());
        Tweet actual = subject.deserialize(parser, null);
        assertEquals(Collections.emptyList(), actual.getHashtags());
    }

    @Test
    public void handlesMissingHashtagsNode() throws Exception {
        JsonNode withEmptyEntities = exampleTweets.get("WITH_EMPTY_ENTITIES");
        JsonParser parser = om.createParser(withEmptyEntities.toString());
        Tweet actual = subject.deserialize(parser, null);
        assertEquals(Collections.emptyList(), actual.getHashtags());
    }

    @Test
    public void handlesHashtags() throws Exception {
        JsonNode withHashtags = exampleTweets.get("WITH_HASHTAGS");
        JsonParser parser = om.createParser(withHashtags.toString());
        Tweet actual = subject.deserialize(parser, null);
        assertArrayEquals(new String[]{"#helloworld", "#hi"}, actual.getHashtags().toArray());
    }

    @Test
    public void handlesMentions() throws Exception {
        JsonNode withMentions = exampleTweets.get("WITH_MENTIONS");
        JsonParser parser = om.createParser(withMentions.toString());
        Tweet actual = subject.deserialize(parser, null);
        assertArrayEquals(new String[]{"Someone"}, actual.getMentions().toArray());
    }

    @Test
    public void handlesUrls() throws Exception {
        JsonNode withUrls = exampleTweets.get("WITH_URLS");
        JsonParser parser = om.createParser(withUrls.toString());
        Tweet actual = subject.deserialize(parser, null);
        assertArrayEquals(new String[]{"https://google.com"}, actual.getUrls().toArray());
    }

    private Instant readDateFromExample(JsonNode tweet) {
        return Instant.from(
                DateTimeFormatter.ISO_DATE_TIME
                        .parse(tweet.get("created_at").asText()));
    }
}