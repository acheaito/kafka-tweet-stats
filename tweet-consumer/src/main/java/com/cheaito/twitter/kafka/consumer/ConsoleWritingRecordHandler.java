package com.cheaito.twitter.kafka.consumer;

import com.cheaito.twitter.domain.Tweet;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

public class ConsoleWritingRecordHandler implements ConsumerRecordHandler<String, Tweet> {
    @Override
    public void process(ConsumerRecords<String, Tweet> consumerRecords) {
        for (ConsumerRecord<String, Tweet> consumerRecord : consumerRecords) {
            System.out.println(consumerRecord.value().getText());
        }
    }
}
