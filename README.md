# Kafka Wikimedia Data Pipeline

This project is a hands-on guide to building a Kafka data pipeline that streams live data from Wikimedia into Apache Kafka, processes it, and sends it to OpenSearch for indexing and analysis. You'll gain practical experience with Kafka producers, consumers, and advanced Kafka features through this real-world application.

### Table of Contents

1. [Kafka Wikimedia Data Pipeline Overview](#1-kafka-wikimedia-data-pipeline-overview)
2. [Setting Up the Environment](#2-setting-up-the-environment)
   1. [Prerequisites](#21-prerequisites)
   2. [Installing Docker Compose on Debian](#22-installing-docker-compose-on-debian)
      1. [Step 1: Installing Docker Engine](#221-step-1-installing-docker-engine)
      2. [Step 2: Installing Docker Compose](#222-step-2-installing-docker-compose)
      3. [Step 3: Running Docker Compose](#223-step-3-running-docker-compose)
   3. [Running the Kafka Wikimedia Data Pipeline](#23-running-the-kafka-wikimedia-data-pipeline)
   4. [Accessing the Conduktor Platform](#24-accessing-the-conduktor-platform)
   5. [Troubleshooting](#25-troubleshooting)
3. [Implementing the Kafka Producer for Wikimedia](#3-implementing-the-kafka-producer-for-wikimedia)
   1. [Code Overview](#31-code-overview)
   2. [Explanation](#32-explanation)
      1. [Bootstrap Servers Configuration](#321-bootstrap-servers-configuration)
      2. [Wikimedia Change Handler](#322-wikimedia-change-handler)
      3. [Event Source and Stream URL](#323-event-source-and-stream-url)
      4. [Blocking the Program](#324-blocking-the-program)
   3. [Key Takeaways](#33-key-takeaways)
4. [Implementing the WikimediaChangeHandler Class](#4-implementing-the-wikimediachangehandler-class)
   1. [Code Overview](#41-code-overview)
   2. [Explanation](#42-explanation)
      1. [onMessage Method](#421-onmessage-method)
      2. [onComment Methods](#422-oncomment-methods)
   3. [Key Takeaways](#43-key-takeaways)
5. [Kafka Producer Acknowledgments](#5-kafka-producer-acknowledgments)
   1. [Overview of Acks Setting](#51-overview-of-acks-setting)
   2. [Acks Configurations](#52-acks-configurations)
6. [Kafka Topic Durability & Availability](#6-kafka-topic-durability--availability)
   1. [Durability](#61-durability)
   2. [Availability](#62-availability)
7. [Producer Retries and Idempotent Producers](#7-producer-retries-and-idempotent-producers)
   1. [Understanding Producer Retries](#71-understanding-producer-retries)
      1. [Producer Retry Configurations](#711-producer-retry-configurations)
   2. [Idempotent Producers](#72-idempotent-producers)
8. [Kafka Message Compression](#8-kafka-message-compression)
   1. [Understanding Kafka Message Compression](#81-understanding-kafka-message-compression)
      1. [Producer-Level Message Compression](#811-producer-level-message-compression)
      2. [Broker-Level Compression](#812-broker-level-compression)
   
## 1. Kafka Wikimedia Data Pipeline Overview

In this project, you'll build a Kafka-based data pipeline to stream, process, and analyze data from Wikimedia. The project involves using Kafka producers and consumers to handle real-time data, integrating with OpenSearch for advanced analytics.

### What You'll Learn:

- **Streaming Data**: Capture real-time data from Wikimedia and send it to Kafka topics.
- **Processing Data**: Develop consumers to process Kafka data and forward it to OpenSearch.
- **Kafka Concepts**: Understand Kafka producers, consumers, topics, partitions, and rebalancing strategies.
- **Integration with OpenSearch**: Learn how to integrate Kafka with OpenSearch for indexing and analytics.

---

## 2. Setting Up the Environment

### 2.1 Prerequisites

Before getting started, ensure you have the following installed:

- **Debian Linux**: The environment used in this guide.
- **Docker**: Containerization platform required to run Kafka, Zookeeper, and other services.
- **Docker Compose**: A tool for defining and running multi-container Docker applications.

### 2.2 Installing Docker Compose on Debian

#### 2.2.1 Step 1: Installing Docker Engine

Follow these steps to install Docker Engine:

1. **Update the package list**:

    ```bash
    sudo apt update
    ```

2. **Install dependencies**:

    ```bash
    sudo apt install apt-transport-https ca-certificates curl software-properties-common
    ```

3. **Add Docker’s official GPG key**:

    ```bash
    curl -fsSL https://download.docker.com/linux/debian/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
    ```

4. **Set up the stable repository**:

    ```bash
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/debian $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
    ```

5. **Update the package list again**:

    ```bash
    sudo apt update
    ```

6. **Install Docker Engine**:

    ```bash
    sudo apt install docker-ce docker-ce-cli containerd.io
    ```

7. **Start and enable Docker**:

    ```bash
    sudo systemctl start docker
    sudo systemctl enable docker
    ```

#### 2.2.2 Step 2: Installing Docker Compose

With Docker Engine installed, install Docker Compose:

1. **Install Docker Compose plugin**:

    ```bash
    sudo apt install docker-compose-plugin
    ```

2. **Verify the installation**:

    ```bash
    docker compose version
    ```

#### 2.2.3 Step 3: Running Docker Compose

Run your Kafka Wikimedia Data Pipeline using Docker Compose:

1. **Navigate to your project directory**:

    ```bash
    cd ~/Documents/projects/kafka-wikimedia-data-pipeline/conduktor-platform
    ```

2. **Start the Docker services**:

    ```bash
    sudo docker compose up -d
    ```

### 2.3 Running the Kafka Wikimedia Data Pipeline

After installing Docker Compose, you can now run the Kafka Wikimedia Data Pipeline. This will start Zookeeper, Kafka, Schema Registry, and the Conduktor platform.

### 2.4 Accessing the Conduktor Platform

Once the containers are up and running, access Conduktor:

1. **Open your browser** and go to:

    ```
    http://localhost:8080
    ```

2. **Login using the credentials** specified in `platform-config.yml`:

   - **Email:** `admin@conduktor.io`
   - **Password:** `admin`

3. **Explore the Conduktor Platform**:
   - Manage your Kafka cluster, monitor streams, and utilize the full feature set of Conduktor.


### Troubleshooting

If you encounter an error when trying to access Kafka or its services, and you receive the following message:

```plaintext
The server is unreachable. Make sure the host is correct and retry.
```

This issue may occur if the Kafka broker service is not properly reachable by other containers or services within the Docker network.

#### Solution:

1. **Restart the Kafka Docker Container**:

   This error can typically be resolved by restarting the Kafka service to re-establish the necessary network connections.

   To restart the Kafka container, run the following command:

   ```bash
   sudo docker restart kafka
   ```
---

## 3. Implementing the Kafka Producer for Wikimedia

### 3.1 Code Overview

Below is the code for the `WikimediaChangesProducer` class. This Java class sets up a Kafka producer that streams real-time data from Wikimedia's event stream and sends it to a Kafka topic.

```java
package org.example.kafka.wikimedia;

import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.EventSource;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import java.net.URI;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class WikimediaChangesProducer {

   public static void main(String[] args) throws InterruptedException {

      String bootstrapServers = "127.0.0.1:9092";

      // create Producer Properties
      Properties properties = new Properties();
      properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
      properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
      properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

      // create the Producer
      KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

      String topic = "wikimedia.recentchange";

      EventHandler eventHandler = new WikimediaChangeHandler(producer, topic);
      String url = "https://stream.wikimedia.org/v2/stream/recentchange";
      EventSource.Builder builder = new EventSource.Builder(eventHandler, URI.create(url));
      EventSource eventSource = builder.build();

      // start the producer in another thread
      eventSource.start();

      // we produce for 10 minutes and block the program until then
      TimeUnit.MINUTES.sleep(10);
   }
}
```

### 3.2 Explanation

#### 1. **Bootstrap Servers Configuration**
   ```java
   String bootstrapServers = "127.0.0.1:9092";
   ```
- **Bootstrap servers** are the addresses of the Kafka brokers that the producer will connect to. In this example, Kafka is running on `localhost` with the default port `9092`.

#### 2. **Wikimedia Change Handler**
   ```java
   EventHandler eventHandler = new WikimediaChangeHandler(producer, topic);
   ```
- The `WikimediaChangeHandler` class is implemented to handle the events received from the Wikimedia stream. It processes these events and sends them to the specified Kafka topic using the producer.

#### 3. **Event Source and Stream URL**
   ```java
   String url = "https://stream.wikimedia.org/v2/stream/recentchange";
   EventSource.Builder builder = new EventSource.Builder(eventHandler, URI.create(url));
   Event

Source eventSource = builder.build();
   ```
- **URL**: The `url` specifies the Wikimedia stream endpoint. The producer listens to this endpoint to capture real-time events.
- **EventSource**: The `EventSource` class manages the connection to the Wikimedia stream and forwards the received events to the `WikimediaChangeHandler`.

#### 4. **Blocking the Program**
   ```java
   TimeUnit.MINUTES.sleep(10);
   ```
- This line ensures that the producer keeps running for 10 minutes, during which it continuously processes and sends events to Kafka. After 10 minutes, the program will exit, and the producer will stop.

### 3.3 Key Takeaways

- **Event Streaming**: The producer continuously streams real-time events from Wikimedia, demonstrating how Kafka can be used to handle live data feeds.
- **Event Handling**: The `WikimediaChangeHandler` plays a crucial role in processing the incoming events and ensuring they are correctly forwarded to Kafka.
- **Multi-Threading**: The producer operates in its thread, allowing the program to handle the events asynchronously without blocking the main execution flow.

---

## 4. Implementing the WikimediaChangeHandler 

### 4.1 Code Overview

Below is the code for the `WikimediaChangeHandler` . This class is responsible for handling the events received from the Wikimedia event stream and sending them to the Kafka topic.

```java
package org.example.kafka.wikimedia;

import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.MessageEvent;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WikimediaChangeHandler implements EventHandler {

    KafkaProducer<String, String> kafkaProducer;
    String topic;
    private final Logger log = LoggerFactory.getLogger(WikimediaChangeHandler.class.getSimpleName());

    public WikimediaChangeHandler(KafkaProducer<String, String> kafkaProducer, String topic){
        this.kafkaProducer = kafkaProducer;
        this.topic = topic;
    }

    @Override
    public void onOpen() {
        // nothing here
    }

    @Override
    public void onClosed() {
        kafkaProducer.close();
    }

    @Override
    public void onMessage(String event, MessageEvent messageEvent) {
        log.info(messageEvent.getData());
        // asynchronous
        kafkaProducer.send(new ProducerRecord<>(topic, messageEvent.getData()));
    }

    @Override
    public void onComment(String comment) {
        // nothing here
    }

    @Override
    public void onError(Throwable t) {
        log.error("Error in Stream Reading", t);
    }
}
```

### 4.2 Explanation

#### 1. **onMessage Method**
   ```java
   @Override
   public void onMessage(String event, MessageEvent messageEvent) {
       log.info(messageEvent.getData());
       // asynchronous
       kafkaProducer.send(new ProducerRecord<>(topic, messageEvent.getData()));
   }
   ```
- **onMessage**: This method handles incoming messages from the Wikimedia event stream. The message data is logged and then asynchronously sent to the Kafka topic using the producer.

#### 2. **onComment Methods**
   ```java
   @Override
   public void onError(Throwable t) {
       log.error("Error in Stream Reading", t);
   }
   ```
- **onError**: This method is triggered when there’s an error in reading the stream. The error is logged for debugging purposes.

### 4.3 Key Takeaways

- **Event Handling**: The `WikimediaChangeHandler` class is crucial for processing and forwarding events from the Wikimedia stream to Kafka.
- **Error Handling**: Proper logging is implemented to track errors during the event streaming process.
- **Resource Management**: Ensures that the Kafka producer is properly closed when the stream is closed, preventing resource leaks.
---

## 5. Kafka Producer Acknowledgments

### 5.1 Overview of Acks Setting

Kafka producers write data to the current leader broker for a partition. To ensure the message is considered successfully written, producers must specify an acknowledgment level (`acks`). This determines whether the message must be written to a minimum number of replicas before being confirmed.

The `acks` setting has a default value that varies based on the Kafka version:

- **Kafka < v3.0**: Default is `acks=1`.
- **Kafka >= v3.0**: Default is `acks=all`.

### 5.2 Acks Configurations

1. **acks=0**:
   ![Producer acks=0](./images/producer_acks_0.png)
   - The producer considers the message as successfully sent once it is dispatched, without waiting for any broker acknowledgment.
   - **Use Case**: Scenarios where losing some messages is acceptable, such as metrics collection. This setting provides the highest throughput as it minimizes network overhead.

2. **acks=1**:
   ![Producer acks=1](./images/producer_acks_1.png)
   - The producer considers the message as successfully written when the leader broker acknowledges it.
   - **Use Case**: Standard operations where losing data is not critical but replication is not guaranteed. If the leader fails before the replicas replicate the data, data loss may occur.

3. **acks=all or -1**:
   ![Producer acks=0](./images/producer_acks_all.png)
   - The producer considers the message as successfully written when all in-sync replicas (ISR) have acknowledged it.
   - **Use Case**: Scenarios requiring high data integrity. This setting ensures that the message is replicated across all in-sync replicas before being acknowledged. The broker setting `min.insync.replicas` controls the minimum number of replicas that must acknowledge the message.
     
   ![min.insync.replicas](./images/producer_acks_all_min_insync_replicas.png)
   The `min.insync.replicas` can be configured at both the topic and broker levels. If the number of available replicas falls below this value, the broker will reject the write request, ensuring no data is lost even in the event of broker failures.
---

## 6. Kafka Topic Durability & Availability

### 6.1 Durability

For a topic with a replication factor of 3, topic data durability can withstand the loss of up to 2 brokers. As a general rule, with a replication factor of N, you can permanently lose up to N-1 brokers and still recover your data.

### 6.2 Availability

Availability depends on both the replication factor and the acknowledgment settings of the producer. To illustrate, consider a replication factor of 3:

- **Reads**: As long as one partition is up and considered an ISR (In-Sync Replica), the topic will be available for reads.

- **Writes**:
   - **acks=0 & acks=1**: As long as one partition is up and considered an ISR, the topic will be available for writes.
   - **acks=all**:
      - **min.insync.replicas=1 (default)**: The topic must have at least 1 ISR partition up (including the leader), allowing for two brokers to be down.
      - **min.insync.replicas=2**: The topic must have at least 2 ISR partitions up, so with a replication factor of 3, only one broker can be down. This guarantees that every write is replicated at least twice.
      - **min.insync.replicas=3**: This setting would not be practical for a replication factor of 3, as it would not tolerate any broker failures.

In summary, when `acks=all` with `replication.factor=N` and `min.insync.replicas=M`, you can tolerate N-M brokers going down while maintaining topic availability.

- **Note**:
The most popular combination for ensuring both data durability and availability is setting acks=all and min.insync.replicas=2 with a replication factor of 3. This configuration allows you to withstand the loss of one Kafka broker while maintaining good data durability and availability.
---

## 7. Producer Retries and Idempotent Producers

### 7.1 Understanding Producer Retries

When a Kafka producer sends messages to a broker, the broker can return either a success or an error code. These error codes fall into two categories:

- **Retriable Errors**: Errors that can be resolved by retrying the message. For example, if the broker returns a `NotEnoughReplicasException`, the producer can attempt to resend the message, as the replica brokers might come back online and the second attempt might succeed.

- **Non-Retriable Errors**: Errors that cannot be resolved by retrying. For example, if the broker returns an `INVALID_CONFIG` exception, retrying the same request will not change the outcome.

To ensure that no messages are dropped when sent to Kafka, it is advisable to enable retries. However, retries should be configured carefully to ensure message ordering and delivery guarantees.

#### 7.1.1 Producer Retry Configurations

```java
// create safe Producer
properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
properties.setProperty(ProducerConfig.ACKS_CONFIG, "all");
properties.setProperty(ProducerConfig.RETRIES_CONFIG, Integer.toString(Integer.MAX_VALUE));
properties.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5"); // Keep as 5 for Kafka 2.0 >= 1.1, use 1 otherwise.
```

1. **`retries`**:
   - This setting determines how many times the producer will attempt to send a message before marking it as failed.
   - **Default Values**:
      - `0` for Kafka ≤ 2.0
      - `MAX_INT` (2147483647) for Kafka ≥ 2.1
   - It's generally recommended to leave this configuration unset and use `delivery.timeout.ms` to control retry behavior.

2. **`delivery.timeout.ms`**:
   - If `retries > 0`, for example, `retries = 2147483647`, the producer won't retry forever; it is bounded by a timeout.
   - **Recommended Value**: `delivery.timeout.ms=120000` (2 minutes).
   - This ensures that records will fail if they can't be delivered within the specified timeout.

3. **`retry.backoff.ms`**:
   - This configuration controls how long the producer waits between retries.
   - **Default Value**: 100ms.

4. **`max.in.flight.requests.per.connection`**:
   - Allowing retries without setting `max.in.flight.requests.per.connection` to 1 can potentially change the order of records.
   - **Recommendation**: Set this to `1` if you rely on key-based ordering to guarantee that Kafka preserves message order even if retries are required.

The diagram below illustrates the Kafka producer retry process:

![Kafka Producer Retries Process](./images/kafka-producer-retries.png)

This diagram helps visualize the retry process and how various configurations interact:

- **Send**: The producer attempts to send a message.
- **Batching**: The message is batched according to the `linger.ms` setting.
- **Await Send**: The message awaits send confirmation.
- **Retries**: If an error occurs, the producer waits for `retry.backoff.ms` and retries sending the message.
- **Inflight**: Messages are in transit and the total time is controlled by `request.timeout.ms`.

Finally, the `delivery.timeout.ms` is an overarching timeout ensuring that the total time spent in retries, batching, and inflight does not exceed this value.

## 7.2 Idempotent Producers

When a producer sends a message to Kafka, there could be network errors that cause duplicate messages. Kafka introduced idempotent producers to handle such scenarios:

- **Idempotent Producer**: This ensures that even if the producer retries sending a message due to network issues, Kafka will recognize the duplicate and prevent it from being committed multiple times.

With Kafka 3.0 and later, the following defaults ensure a safe and reliable producer configuration:

- `acks = all`
- `enable.idempotence = true`
- `retries = MAX_INT`
- `max.in.flight.requests.per.connection = 5` (for Kafka >= 2.0) or `1` (for older versions).

These settings help maintain message order, ensure data integrity, and prevent duplicates.

In summary, for Kafka 3.0 and later, the producer is safe by default, and you do not need to make any changes. For earlier versions, it is recommended to manually set the configurations to ensure the reliability of your data pipeline.

---

# 8. Kafka Message Compression

## 8.1 Understanding Kafka Message Compression

When sending data to Kafka, producers often handle text-based data formats such as JSON, which can be quite large. To optimize performance, it’s crucial to apply compression at the producer level. By default, Kafka producer messages are sent uncompressed, which can lead to inefficiencies in both network utilization and storage.

Kafka supports two types of message compression: producer-side compression and broker-side compression. Each method has its benefits and specific use cases.

### 8.1.1 Producer-Level Message Compression

Producers can choose to compress messages before sending them to Kafka by configuring the `compression.type` setting. This setting has several options:

- **none**: No compression is applied (this is the default setting).
- **gzip**: Standard gzip compression.
- **lz4**: LZ4 compression, optimized for speed.
- **snappy**: Snappy compression, offering a good balance between speed and compression ratio.
- **zstd**: Zstandard compression, available in Kafka 2.1 and later, provides better compression ratios at a similar or faster speed compared to gzip.

When enabled, compression happens at the batch level, meaning all messages in a single producer batch are compressed together. This approach increases the efficiency of compression, particularly when dealing with large batches, and can significantly reduce the size of the messages being sent to Kafka.

**Advantages of Producer-Level Compression**:

- **Reduced Network Utilization**: Compressed messages are smaller, which reduces the amount of data sent over the network, leading to less latency and better throughput.
- **Improved Disk Utilization**: Smaller message sizes mean that Kafka can store more data on disk, which is particularly beneficial when dealing with large volumes of data.

**Trade-offs**:

- **Increased CPU Usage**: Both the producer and consumer must commit additional CPU resources to compress and decompress the messages. However, this overhead is often minimal compared to the benefits gained in network and storage efficiency.

### 8.1.2 Broker-Level Compression

Compression can also be configured at the broker or topic level. This setting allows Kafka to handle compression based on the `compression.type` defined for a specific topic.

- **`compression.type=producer`**: The broker takes the compressed batch from the producer and writes it directly to the topic’s log file without recompressing the data. This is the default setting and is optimal because it avoids unnecessary recompression.

- **Topic-Specific Compression**: If a topic is configured with a specific compression type (e.g., `lz4`), and this type matches the producer's compression setting, the broker will write the messages as they are. However, if the producer uses a different compression method, the broker will decompress the messages and then recompress them according to the topic's specified compression type.

**Advantages of Broker-Level Compression**:

- **Flexibility**: It allows central control over compression settings at the topic level, useful when you don’t have control over the producer’s settings or when you want uniform compression across all messages in a topic.

- **Trade-offs**: Similar to producer-level compression, broker-side compression consumes additional CPU cycles, particularly if the broker has to decompress and recompress messages.

---

**Best Practices for Kafka Message Compression**:

1. **Enable Compression in Production**: Always use compression in production environments, especially when dealing with high-throughput streams, to optimize network and storage efficiency.

2. **Optimize for Speed and Compression Ratio**: Consider using `snappy` or `lz4` for an optimal balance between speed and compression efficiency.

3. **Batch Size and Linger Time**: Adjusting the `batch.size` and `linger.ms` settings can help create larger batches of messages, which improves the efficiency of compression.

4. **Testing Compression Settings**: Always test different compression settings in your environment before applying them to production to ensure they meet your performance requirements.
