package com.example.kafka;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

//The imports below are essential for using Twitter4J
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.FilterQuery;
import twitter4j.StatusListener;
import twitter4j.Status;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class TwitterKafkaProducer {
    private String CONSUMER_KEY;
    private String CONSUMER_SECRET;
    private String TOKEN;
    private String TOKEN_SECRET;

    public static void main(String[] args) {
        new TwitterKafkaProducer().run();
    }

    public TwitterKafkaProducer() {
        loadConfig();
    }

    public void run() {
        System.out.println("Setting up...");
        KafkaProducer<String, String> producer = createKafkaProducer();

        // Set up the Twitter Stream
        TwitterStream twitterStream = createTwitterStream(producer);

        // Filter tweets by keywords
        FilterQuery filterQuery = new FilterQuery();
        filterQuery.track("java", "kafka", "bigdata");
        twitterStream.filter(filterQuery);
    }

    private void loadConfig() {
        try (FileInputStream input = new FileInputStream("backend-api/config.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            CONSUMER_KEY = prop.getProperty("twitter.consumer.key");
            CONSUMER_SECRET = prop.getProperty("twitter.consumer.secret");
            TOKEN = prop.getProperty("twitter.token");
            TOKEN_SECRET = prop.getProperty("twitter.token.secret");

            System.out.println("API keys loaded successfully.");
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Unable to load Twitter API keys from config.properties file");
        }
    }

    public KafkaProducer<String, String> createKafkaProducer() {
        String bootstrapServers = "127.0.0.1:9092"; // Make sure Kafka broker is running

        // Create producer properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Safe Producer Configs
        properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        properties.setProperty(ProducerConfig.ACKS_CONFIG, "all");
        properties.setProperty(ProducerConfig.RETRIES_CONFIG, Integer.toString(Integer.MAX_VALUE));

        // Create the producer
        return new KafkaProducer<>(properties);
    }

    public TwitterStream createTwitterStream(KafkaProducer<String, String> producer) {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(CONSUMER_KEY)
                .setOAuthConsumerSecret(CONSUMER_SECRET)
                .setOAuthAccessToken(TOKEN)
                .setOAuthAccessTokenSecret(TOKEN_SECRET);

        TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();

        twitterStream.addListener(new StatusListener() {
            @Override
            public void onStatus(Status status) {
                String tweet = status.getText();
                System.out.println("Tweet received: " + tweet);
                producer.send(new ProducerRecord<String, String>("twitter-stream", null, tweet));
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                System.out.println("Received status deletion notice for: " + statusDeletionNotice.getStatusId());
            }

            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                System.out.println("Track limitation notice received: " + numberOfLimitedStatuses);
            }

            @Override
            public void onScrubGeo(long userId, long upToStatusId) {
                System.out.println("Received scrub geo event for user: " + userId);
            }

            @Override
            public void onStallWarning(StallWarning warning) {
                System.out.println("Received stall warning: " + warning.getMessage());
            }

            @Override
            public void onException(Exception ex) {
                ex.printStackTrace();
            }
        });

        return twitterStream;
    }
}

//dsadadadas
//dsadadadas//dsadadadas

//dsadadadas
//dsadadadas
//dsadadadas
//dsadadadas