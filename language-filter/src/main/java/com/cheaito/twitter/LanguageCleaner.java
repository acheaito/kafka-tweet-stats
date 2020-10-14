package com.cheaito.twitter;

import com.cheaito.twitter.domain.Tweet;
import org.apache.kafka.streams.kstream.ValueTransformer;
import org.apache.kafka.streams.processor.ProcessorContext;

class LanguageCleaner implements ValueTransformer<Tweet, Tweet> {

    @Override
    public void init(ProcessorContext context) {
    }

    @Override
    public Tweet transform(Tweet value) {
        String cleanedMessage = cleanMessage(value.getText());
        return new Tweet(value.getId(), value.getLang(), cleanedMessage);
    }

    private String cleanMessage(String text) {
        return WordMasker.maskUndesirableWords(text);
    }

    @Override
    public void close() {

    }
}
