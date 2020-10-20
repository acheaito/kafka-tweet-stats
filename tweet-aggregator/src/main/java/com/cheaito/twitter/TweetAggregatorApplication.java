package com.cheaito.twitter;

import com.cheaito.twitter.model.HashtagStats;
import com.cheaito.twitter.model.Tweet;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;

import java.io.IOException;
import java.util.Collections;
import java.util.Properties;
import java.util.stream.Collectors;

public class TweetAggregatorApplication {

    public static void main(String[] args) throws IOException {
//        Weld weld = new Weld();
//        WeldContainer weldContainer = weld.initialize();
//        TweetAggregatorApplication application = weldContainer.select(TweetAggregatorApplication.class).get();
//        application.start();
//        weld.shutdown();
        new TweetAggregatorApplication().start();
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
        String longtermStatsTopicName = props.getProperty("stats.alltime.topic.name");
        SpecificAvroSerde<HashtagStats> hashtagStatsSpecificAvroSerde = new SpecificAvroSerde<>();
        Serdes.StringSerde stringSerde = new Serdes.StringSerde();

        hashtagStatsSpecificAvroSerde.configure(Collections.singletonMap(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, props.getProperty("schema.registry.url")), false);

        KStream<String, Tweet> tweetsByLang = streamsBuilder.stream(sourceTopicName);
        KStream<String, Tweet> tweetsByHashtag = tweetsByLang.flatMap((key, tweet) -> tweet.getHashtags()
                .stream()
                .map(hashtag -> KeyValue.pair(hashtag.toLowerCase(), tweet))
                .collect(Collectors.toSet()));

        KTable<String, HashtagStats> longTermStats =
                tweetsByHashtag.groupByKey().aggregate(
                        () -> HashtagStats.newBuilder().build(),
                        this::aggregateStats,
                        Materialized.<String, HashtagStats, KeyValueStore<Bytes, byte[]>>as(longtermStatsTopicName)
                                .withValueSerde(hashtagStatsSpecificAvroSerde)
                );

        longTermStats.toStream().to(longtermStatsTopicName, Produced.with(stringSerde, hashtagStatsSpecificAvroSerde));
        return new KafkaStreams(streamsBuilder.build(), props);
    }

    private HashtagStats aggregateStats(String hashtag, Tweet tweet, HashtagStats currentStats) {
        HashtagStats.Builder hashtagStatsBuilder = HashtagStats.newBuilder(currentStats);
        hashtagStatsBuilder.setTag(hashtag);
        hashtagStatsBuilder.setCount(hashtagStatsBuilder.getCount() + 1);
        hashtagStatsBuilder.setLastTweetTime(tweet.getCreatedAt());
        return hashtagStatsBuilder.build();
    }
}
