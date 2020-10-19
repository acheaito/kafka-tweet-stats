package com.cheaito.twitter.client;

import com.cheaito.twitter.kafka.producer.TweetKafkaProducer;
import com.cheaito.twitter.model.TwitterApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Properties;

public class TwitterClient {
    private final static String TWITTER_PROPS_FILE = "twitter.properties";
    private final static String TWEETS_SAMPLE_STREAM_ENDPOINT = "/tweets/sample/stream";

    private final Properties twitterProps;
    private final TweetKafkaProducer tweetKafkaProducer;
    private final ObjectMapper objectMapper;
    private final CloseableHttpClient httpClient;
    private Logger logger = LoggerFactory.getLogger(TwitterClient.class);

    @Inject
    public TwitterClient(CloseableHttpClient httpClient, TweetKafkaProducer tweetKafkaProducer, ObjectMapper objectMapper) throws IOException {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient should not be null");
        this.tweetKafkaProducer = Objects.requireNonNull(tweetKafkaProducer, "kafkaProducer should not be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper should not be null");
        twitterProps = new Properties();
        twitterProps.load(TwitterClient.class.getClassLoader().getResourceAsStream(TWITTER_PROPS_FILE));
    }

    public void start() throws URISyntaxException, IOException {
        URI endpoint = new URI(twitterProps.getProperty("twitter.url") +
                TWEETS_SAMPLE_STREAM_ENDPOINT +
                "?tweet.fields=entities,lang,created_at" +
                "&user.fields=username" +
                "&place.fields=full_name");
        HttpGet get = new HttpGet(endpoint);
        get.addHeader("Authorization", "Bearer " + twitterProps.getProperty("app.bearerToken"));
        try {
            httpClient.execute(get, this::consumeTwitterStream);
        } finally {
            httpClient.close();
        }
        logger.debug("Done");
    }

    private String consumeTwitterStream(HttpResponse httpResponse) throws IOException {
        if (httpResponse.getStatusLine().getStatusCode() >= HttpStatus.SC_BAD_REQUEST) {
            throw new RuntimeException("Bad response from Twitter: "
                    + httpResponse.getStatusLine().getStatusCode() + " - " + httpResponse.getStatusLine().getReasonPhrase());
        }
        long readDelay = Long.parseLong(twitterProps.getProperty("app.readDelay", "1000"));
        BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(httpResponse.getEntity().getContent())));
        logger.debug("Starting Twitter API stream loop");
        boolean keepReading = true;
        while (keepReading) {
            String response = br.readLine();
            if (response == null) {
                keepReading = false;
            }
            logger.debug("Parsing api response: {}", response);
            try {
                if (!response.isEmpty()) {
                    TwitterApiResponse apiResponse = objectMapper.readValue(response, TwitterApiResponse.class);
                    tweetKafkaProducer.produce(apiResponse.getData());
                }
                Thread.sleep(readDelay);
            } catch (Throwable e) {
                logger.error("Error processing tweet: {}", e.getMessage(), e);
            }
        }
        logger.debug("Exited Twitter API stream loop");

        return null;
    }
}
