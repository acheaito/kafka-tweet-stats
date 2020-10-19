package com.cheaito.twitter.client;

import com.cheaito.twitter.kafka.producer.TweetKafkaProducer;
import com.cheaito.twitter.model.TwitterApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private Logger logger = LoggerFactory.getLogger(TwitterClient.class);

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
                + "?tweet.fields=entities,lang,created_at&user.fields=username&place.fields=full_name");
        long readDelay = Long.parseLong(twitterProps.getProperty("app.readDelay", "1000"));
        Request.Get(endpoint)
                .addHeader("Authorization", "Bearer " + twitterProps.getProperty("app.bearerToken"))
                .execute()
                .handleResponse(httpResponse -> {
                            if (httpResponse.getStatusLine().getStatusCode() >= HttpStatus.SC_BAD_REQUEST) {
                                throw new RuntimeException("Bad response from Twitter: "
                                        + httpResponse.getStatusLine().getStatusCode() + " - " + httpResponse.getStatusLine().getReasonPhrase());
                            }
                            BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(httpResponse.getEntity().getContent())));
                    logger.debug("Starting Twitter API stream loop");
                            boolean keepReading = true;
                            while (keepReading) {
                                String response = br.readLine();
                                if (response == null) {
                                    keepReading = false;
                                }
                                logger.debug("Parsing api response: {}", response);
                                if (!response.isEmpty()) {
                                    TwitterApiResponse apiResponse = objectMapper.readValue(response, TwitterApiResponse.class);
                                    tweetKafkaProducer.produce(apiResponse.getData());
                                }
                                try {
                                    Thread.sleep(readDelay);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                            }
                            return null;
                        }
                );
        System.out.println("Exited API consumption call");
    }
}
