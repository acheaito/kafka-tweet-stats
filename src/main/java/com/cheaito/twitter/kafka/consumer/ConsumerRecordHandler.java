package com.cheaito.twitter.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecords;

public interface ConsumerRecordHandler<K, V> {
    void process(ConsumerRecords<K, V> consumerRecords);
}
