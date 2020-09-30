package com.cheaito.twitter.digester;

import com.cheaito.twitter.kafka.producer.TweetKafkaProducer;
import org.apache.http.client.fluent.Request;

import javax.inject.Inject;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

public class TwitterDigester {
    private final static String TWITTER_PROPS_FILE = "twitter.properties";
    private final static String TWEETS_SAMPLE_STREAM_ENDPOINT = "/tweets/sample/stream";

    private final Properties twitterProps;
    private final TweetKafkaProducer tweetKafkaProducer;

    @Deprecated
    public TwitterDigester() {
        this.twitterProps = null;
        this.tweetKafkaProducer = null;
    }

    @Inject
    public TwitterDigester(TweetKafkaProducer tweetKafkaProducer) throws IOException {
        this.tweetKafkaProducer = tweetKafkaProducer;
        twitterProps = new Properties();
        twitterProps.load(TwitterDigester.class.getClassLoader().getResourceAsStream(TWITTER_PROPS_FILE));
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
                            InputStream is = s.getEntity().getContent();
                            BufferedInputStream bis = new BufferedInputStream(s.getEntity().getContent());
                            BufferedReader br = new BufferedReader(new InputStreamReader(bis));
                            boolean keepReading = true;
                            while (keepReading) {
                                String tweet = br.readLine();
                                if (tweet == null) {
                                    keepReading = false;
                                }
                                tweetKafkaProducer.produce(tweet);
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
