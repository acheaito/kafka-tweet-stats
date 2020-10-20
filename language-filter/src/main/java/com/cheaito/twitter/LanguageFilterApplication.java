package com.cheaito.twitter;

import com.cheaito.twitter.model.Tweet;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

public class LanguageFilterApplication {
    Logger logger = LoggerFactory.getLogger(LanguageFilterApplication.class);

    public static void main(String[] args) throws IOException {
        LanguageFilterApplication application = new LanguageFilterApplication();
        application.start();
    }

    private void start() throws IOException {
        Properties props = new Properties();
        props.load(this.getClass().getClassLoader().getResourceAsStream("kafka.properties"));
        KafkaStreams streams = createStreamTopology(props);
        streams.cleanUp();
        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    private KafkaStreams createStreamTopology(Properties props) {
        StreamsBuilder builder = new StreamsBuilder();

        String sourceTopicName = props.getProperty("source.topic.name");
        KStream<String, Tweet> tweets = builder.stream(sourceTopicName);

        String sanatizedTopicName = props.getProperty("sanitized.topic.name");

        tweets
//                .peek((k, tweet) -> logger.debug("Tweet: {} - {}", tweet.getLang(), tweet.getText()))
//                .filter((key, tweet) -> tweet.getLang().equals(ENGLISH_LANG))
                .transformValues(LanguageCleaner::new)
                .repartition()
                .peek((k, tweet) -> logger.debug("Cleaned: {} - {}", tweet.getLang(), tweet.getText()))
                .to(sanatizedTopicName);

        return new KafkaStreams(builder.build(), props);
    }
}
