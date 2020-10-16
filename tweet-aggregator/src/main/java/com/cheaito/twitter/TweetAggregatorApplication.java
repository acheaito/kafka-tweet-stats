package com.cheaito.twitter;

import com.cheaito.twitter.model.Tweet;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import java.io.IOException;
import java.util.Properties;
import java.util.stream.Collectors;

public class TweetAggregatorApplication {
    public static void main(String[] args) throws IOException {
        Weld weld = new Weld();
        WeldContainer weldContainer = weld.initialize();
        TweetAggregatorApplication application = weldContainer.select(TweetAggregatorApplication.class).get();
        application.start();
        weld.shutdown();
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
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        String sourceTopicName = props.getProperty("source.topic.name");
        KStream<String, Tweet> tweetsByLang = streamsBuilder.stream(sourceTopicName);
        KStream<String, Tweet> tweetsByHashtag = tweetsByLang.flatMap((key, tweet) -> tweet.getHashtags()
                .stream()
                .map(hashtag -> KeyValue.pair(hashtag, tweet))
                .collect(Collectors.toSet()));
//        KTable<String, HashtagStats> longTermCourseStats =
//                tweetsByHashtag.groupByKey().aggregate(
//                        this::emptyStats,
//                        this::reviewAggregator,
//                        courseStatisticSpecificAvroSerde
//                );
//        longTermCourseStats.toStream().to("long-term-stats");
        return null;
    }
}
