package com.cheaito.twitter;

import com.cheaito.twitter.domain.Tweet;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

public class LanguageFilterApplication {
    private static final String ENGLISH_LANG = "en";
    Logger logger = LoggerFactory.getLogger("language-filter");

    public static void main(String[] args) throws IOException, URISyntaxException {
        Weld weld = new Weld();
        WeldContainer weldContainer = weld.initialize();
        LanguageFilterApplication application = weldContainer.select(LanguageFilterApplication.class).get();
        application.start();
        weld.shutdown();
    }

    private void start() throws IOException, URISyntaxException {
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
//                .peek((k, tweet) -> logger.info("Tweet: {} - {}", tweet.getLang(), tweet.getText()))
                .filter((key, tweet) -> tweet.getLang().equals(ENGLISH_LANG))
                .transformValues(LanguageCleaner::new)
//                .peek((k, tweet) -> logger.info("Cleaned: {} - {}", tweet.getLang(), tweet.getText()))
                .to(sanatizedTopicName);

        return new KafkaStreams(builder.build(), props);
    }
}
