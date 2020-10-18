package com.cheaito.twitter;

import com.cheaito.twitter.model.Tweet;
import org.apache.kafka.streams.kstream.ValueTransformer;
import org.apache.kafka.streams.processor.ProcessorContext;

class LanguageCleaner implements ValueTransformer<Tweet, Tweet> {

    @Override
    public void init(ProcessorContext context) {
    }

    @Override
    public Tweet transform(Tweet value) {
        return Tweet.newBuilder(value).setText(cleanMessage(value.getText())).build();
    }

    private String cleanMessage(String text) {
        return WordMasker.maskUndesirableWords(text);
    }

    @Override
    public void close() {

    }
}
