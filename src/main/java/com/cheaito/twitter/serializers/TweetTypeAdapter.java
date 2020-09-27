package com.cheaito.twitter.serializers;

import com.cheaito.twitter.domain.Tweet;
import com.google.gson.*;

import java.lang.reflect.Type;

public class TweetTypeAdapter implements JsonDeserializer<Tweet>, JsonSerializer<Tweet> {

    @Override
    public Tweet deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject data = jsonElement.getAsJsonObject().getAsJsonObject("data");
        return context.deserialize(data, type);
    }

    @Override
    public JsonElement serialize(Tweet tweet, Type type, JsonSerializationContext context) {
        return context.serialize(tweet, type);
    }
}
