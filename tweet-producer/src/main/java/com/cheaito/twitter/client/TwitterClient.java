package com.cheaito.twitter.client;

import com.cheaito.twitter.kafka.producer.TweetKafkaProducer;
import com.cheaito.twitter.model.TwitterApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.fluent.Request;

import javax.inject.Inject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

public class TwitterClient {
    private final static String TWITTER_PROPS_FILE = "twitter.properties";
    private final static String TWEETS_SAMPLE_STREAM_ENDPOINT = "/tweets/sample/stream";

    private final Properties twitterProps;
    private final TweetKafkaProducer tweetKafkaProducer;
    private final ObjectMapper objectMapper;

    @Inject
    public TwitterClient(TweetKafkaProducer tweetKafkaProducer, ObjectMapper objectMapper) throws IOException {
        this.tweetKafkaProducer = tweetKafkaProducer;
        twitterProps = new Properties();
        twitterProps.load(TwitterClient.class.getClassLoader().getResourceAsStream(TWITTER_PROPS_FILE));
        this.objectMapper = objectMapper;
    }

    public void start() throws URISyntaxException, IOException {
        URI endpoint = new URI(twitterProps.getProperty("twitter.url")
                + TWEETS_SAMPLE_STREAM_ENDPOINT
                + "?tweet.fields=lang&user.fields=username&place.fields=full_name");
        long readDelay = Long.parseLong(twitterProps.getProperty("app.readDelay", "1000"));
        Request.Get(endpoint)
                .addHeader("Authorization", "Bearer " + twitterProps.getProperty("app.bearerToken"))
                .execute()
                .handleResponse(s -> {
                    BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(s.getEntity().getContent())));
                    boolean keepReading = true;
                            while (keepReading) {
                                String tweet = br.readLine();
                                if (tweet == null) {
                                    keepReading = false;
                                }
                                System.out.println("raw=" + tweet);
                                TwitterApiResponse apiResponse = objectMapper.readValue(tweet, TwitterApiResponse.class);
                                System.out.println("parsed=" + apiResponse.getData());
                                tweetKafkaProducer.produce(apiResponse.getData());
                                try {
                                    Thread.sleep(readDelay);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            return null;
                        }
                );
    }
}
