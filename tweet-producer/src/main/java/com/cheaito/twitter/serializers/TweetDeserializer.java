package com.cheaito.twitter.serializers;

import com.cheaito.twitter.model.Hashtag;
import com.cheaito.twitter.model.Tweet;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TweetDeserializer extends JsonDeserializer<Tweet> {

    @Override
    public Tweet deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.readValueAsTree();
        return new Tweet(node.get("id").textValue(),
                node.get("lang").textValue(),
                node.get("text").textValue(),
                extractTags(node.get("entities"), p.getCodec(), ctxt));
    }

    private List<String> extractTags(JsonNode entitiesNode, ObjectCodec codec, DeserializationContext ctxt) throws IOException {
        if (entitiesNode == null) {
            return Collections.emptyList();
        }

        JsonNode hashtagsNode = entitiesNode.get("hashtags");
        if (hashtagsNode == null) {
            return Collections.emptyList();
        }

        Hashtag[] hashtagArray = ctxt.readValue(hashtagsNode.traverse(codec), Hashtag[].class);
        return Arrays.stream(hashtagArray)
                .map(Hashtag::getTag)
                .collect(Collectors.toList());
    }
}
