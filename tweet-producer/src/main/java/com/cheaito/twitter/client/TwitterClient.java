package com.cheaito.twitter.client;

import com.cheaito.twitter.kafka.producer.TweetKafkaProducer;
import com.cheaito.twitter.model.TwitterApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
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
        URI endpoint = new URI(twitterProps.getProperty("twitter.url") +
                TWEETS_SAMPLE_STREAM_ENDPOINT +
                "?tweet.fields=entities,lang,created_at" +
                "&user.fields=username" +
                "&place.fields=full_name");
        Request.Get(endpoint)
                .addHeader("Authorization", "Bearer " + twitterProps.getProperty("app.bearerToken"))
                .execute()
                .handleResponse(this::consumeTwitterStream);
        logger.debug("Done");
    }

    private Object consumeTwitterStream(HttpResponse httpResponse) throws IOException {
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
                logger.error("Error processing tweets: {}", e.getMessage(), e);
                keepReading = false;
            }
        }
        logger.debug("Exited Twitter API stream loop");
        EntityUtils.consume(httpResponse.getEntity());
        return Response.status(Response.Status.OK).build();
    }
}
