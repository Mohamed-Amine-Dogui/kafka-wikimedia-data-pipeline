package org.example.kafka;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerDemoWithCallback {

    private static final Logger log = LoggerFactory.getLogger(ProducerDemoWithCallback.class.getSimpleName());
    public static void main(String[] args) {
        log.info("I'm a Kafka Producer!");

        // create Producer Properties
        Properties properties = new Properties();

        //connect to localhost
        properties.setProperty("bootstrap.servers", "127.0.0.1:9092");

        //connect to Conduktor playground
//        properties.setProperty("bootstrap.server", "cluster.playground.cdkt.io:9092");
//        properties.setProperty("security.protocol", "SASL_SSL");
//        properties.setProperty("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required  username=\"alice\" password=\"alice-secret\";");
//        properties.setProperty("sasl.mechanism", "PLAIN");


        // set Producer properties
        properties.setProperty("key.serializer", StringSerializer.class.getName());
        properties.setProperty("value.serializer", StringSerializer.class.getName());

        properties.setProperty("batch.size", "400");

        //properties.setProperty("partitioner.class", RoundRobinPartitioner.class.getName());


        // create the Producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        for (int j =0; j<10; j++) {
            for (int i =0; i<30; i++){
                // create a Producer Record
                ProducerRecord<String, String> producerRecord =
                        new ProducerRecord<>("demo_java", "hello from demo_java topic "+ i);

                // send data
                producer.send(producerRecord, new Callback() {
                    @Override
                    public void onCompletion(RecordMetadata metadata, Exception e) {
                        // this will be executed every time a record was successfully sent or an exception is thrown
                        if (e == null) {
                            // the record was successfully sent
                            log.info("Recieved new metadata \n" +
                                    "Topic: " + metadata.topic() +"\n" +
                                    "Partition: " + metadata.partition() +"\n" +
                                    "Offset: " + metadata.offset() +"\n" +
                                    "Timestamp: " + metadata.timestamp() +"\n" );
                        } else {
                            log.error("Error while producing", e );
                        }
                    }
                });

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        }

        //  flush: tell the producer to send all data and block until done --synchronous
        producer.flush();

        // close the producer
        producer.close();
    }
}
