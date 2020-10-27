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
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.WindowStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Properties;
import java.util.stream.Collectors;

public class TweetAggregatorApplication {

    private Logger logger = LoggerFactory.getLogger(TweetAggregatorApplication.class);

    public static void main(String[] args) throws IOException {
        new TweetAggregatorApplication().start();
    }

    private void start() throws IOException {
        Properties props = new Properties();
        props.load(this.getClass().getClassLoader().getResourceAsStream("kafka.properties"));
        logger.debug("Props loaded: {}", props.toString());
        KafkaStreams streams = createStreamTopology(props);
        streams.cleanUp();
        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    private KafkaStreams createStreamTopology(Properties props) {
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        String sourceTopicName = props.getProperty("source.topic.name");
        String longtermStatsTopicName = props.getProperty("stats.longterm.topic.name");
        String recentStatsTopicName = props.getProperty("stats.recent.topic.name");
        SpecificAvroSerde<HashtagStats> hashtagStatsSpecificAvroSerde = new SpecificAvroSerde<>();

        hashtagStatsSpecificAvroSerde.configure(Collections.singletonMap(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, props.getProperty("schema.registry.url")), false);

        KStream<String, Tweet> tweetsByLang = streamsBuilder.stream(sourceTopicName);
        KStream<String, Tweet> tweetsByHashtag = tweetsByLang
                .flatMap((key, tweet) -> tweet.getHashtags()
                        .stream()
                        .map(hashtag -> KeyValue.pair(hashtag.toLowerCase(), tweet))
                        .collect(Collectors.toSet()));
        setupLongTermStatsStream(longtermStatsTopicName, hashtagStatsSpecificAvroSerde, tweetsByHashtag);

        Duration recentWindow = Duration.ofMinutes(Long.parseLong(props.getProperty("stats.recent.duration")));
        Duration windowAdvance = Duration.ofMinutes(Long.parseLong(props.getProperty("stats.recent.duration.advance")));
        setupWindowedStatsStream(recentStatsTopicName, hashtagStatsSpecificAvroSerde, tweetsByHashtag, recentWindow, windowAdvance);
        return new KafkaStreams(streamsBuilder.build(), props);
    }

    private void setupLongTermStatsStream(String topicName, SpecificAvroSerde<HashtagStats> hashtagStatsSpecificAvroSerde, KStream<String, Tweet> tweetsByHashtag) {
        KTable<String, HashtagStats> longTermStats =
                tweetsByHashtag.groupByKey().aggregate(
                        () -> HashtagStats.newBuilder().build(),
                        this::aggregateStats,
                        Materialized.<String, HashtagStats, KeyValueStore<Bytes, byte[]>>as(topicName)
                                .withValueSerde(hashtagStatsSpecificAvroSerde)
                );

        longTermStats.toStream().to(topicName, Produced.with(new Serdes.StringSerde(), hashtagStatsSpecificAvroSerde));
    }

    private void setupWindowedStatsStream(String topicName, SpecificAvroSerde<HashtagStats> hashtagStatsSpecificAvroSerde, KStream<String, Tweet> tweetsByHashtag, Duration windowSizeDuration, Duration advanceDuration) {
        TimeWindows timeWindows = TimeWindows.of(windowSizeDuration).advanceBy(advanceDuration);

        KTable<Windowed<String>, HashtagStats> windowsHashtagStatsKTable =
                tweetsByHashtag.filter((k, tweet) -> isTweetRecent(tweet, windowSizeDuration))
                        .groupByKey()
                        .windowedBy(timeWindows)
                        .aggregate(
                                () -> HashtagStats.newBuilder().build(),
                                this::aggregateStats,
                                Materialized.<String, HashtagStats, WindowStore<Bytes, byte[]>>as(topicName)
                                        .withValueSerde(hashtagStatsSpecificAvroSerde)
                        );

        KStream<String, HashtagStats> recentStats = windowsHashtagStatsKTable.toStream()
                .filter((window, hashtagStats) -> isCurrentWindow(window, advanceDuration))
                .peek((key, value) -> logger.debug(value.toString()))
                .selectKey((k, v) -> k.key());

        recentStats.to(topicName, Produced.with(new Serdes.StringSerde(), hashtagStatsSpecificAvroSerde));
    }

    private boolean isCurrentWindow(Windowed<String> windowDuration, Duration advanceDuration) {
        Instant now = Instant.now();
        Instant limit = now.plus(advanceDuration);
        Instant windowEnd = windowDuration.window().endTime();
        return windowEnd.isAfter(now) && windowEnd.isBefore(limit);
    }

    private boolean isTweetRecent(Tweet tweet, Duration windowSizeDuration) {
        Instant windowStart = Instant.now().minus(windowSizeDuration);
        Instant tweetCreation = Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(tweet.getCreatedAt()));
        return tweetCreation.isAfter(windowStart);
    }

    private HashtagStats aggregateStats(String hashtag, Tweet tweet, HashtagStats currentStats) {
        HashtagStats.Builder hashtagStatsBuilder = HashtagStats.newBuilder(currentStats);
        hashtagStatsBuilder.setTag(hashtag);
        hashtagStatsBuilder.setCount(hashtagStatsBuilder.getCount() + 1);
        hashtagStatsBuilder.setLastTweetTime(tweet.getCreatedAt());
        return hashtagStatsBuilder.build();
    }
}
