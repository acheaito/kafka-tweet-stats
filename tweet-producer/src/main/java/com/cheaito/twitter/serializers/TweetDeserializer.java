package com.cheaito.twitter.serializers;

import com.cheaito.twitter.model.Hashtag;
import com.cheaito.twitter.model.Mention;
import com.cheaito.twitter.model.Tweet;
import com.cheaito.twitter.model.Url;
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
        return Tweet.newBuilder().setId(node.get("id").textValue())
                .setLang(node.get("lang").textValue())
                .setText(node.get("text").textValue())
                .setCreatedAt(node.get("created_at").textValue())
                .setHashtags(extractTags(node.get("entities"), p.getCodec()))
                .setMentions(extractMentions(node.get("entities"), p.getCodec()))
                .setUrls(extractUrls(node.get("entities"), p.getCodec()))
                .build();
    }

    private List<String> extractTags(JsonNode entitiesNode, ObjectCodec codec) throws IOException {
        if (entitiesNode == null || entitiesNode.get("hashtags") == null) {
            return Collections.emptyList();
        }

        Hashtag[] hashtags = entitiesNode.get("hashtags").traverse(codec).readValueAs(Hashtag[].class);
        return Arrays.stream(hashtags)
                .map(Hashtag::getTag)
                .collect(Collectors.toList());
    }

    private List<String> extractMentions(JsonNode entitiesNode, ObjectCodec codec) throws IOException {
        if (entitiesNode == null || entitiesNode.get("mentions") == null) {
            return Collections.emptyList();
        }

        Mention[] mentions = entitiesNode.get("mentions").traverse(codec).readValueAs(Mention[].class);
        return Arrays.stream(mentions)
                .map(Mention::getUsername)
                .collect(Collectors.toList());
    }

    private List<String> extractUrls(JsonNode entitiesNode, ObjectCodec codec) throws IOException {
        if (entitiesNode == null || entitiesNode.get("urls") == null) {
            return Collections.emptyList();
        }

        Url[] urls = entitiesNode.get("urls").traverse(codec).readValueAs(Url[].class);
        return Arrays.stream(urls)
                .map(Url::getUrl)
                .collect(Collectors.toList());
    }
}
