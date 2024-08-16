# Kafka Wikimedia Data Pipeline

This project is a hands-on guide to building a Kafka data pipeline that streams live data from Wikimedia into Apache Kafka, processes it, and sends it to OpenSearch for indexing and analysis. You'll gain practical experience with Kafka producers, consumers, and advanced Kafka features through this real-world application.

### Additional Resources

For a deeper understanding of Apache Kafka and related concepts, refer to the following resource:

- [What is Apache Kafka?](https://www.conduktor.io/kafka/what-is-apache-kafka/) - A comprehensive guide by Conduktor covering Kafka's architecture, use cases, and more.

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
      2. [onComment and onError Methods](#422-oncomment-and-onerror-methods)
   3. [Key Takeaways](#43-key-takeaways)
---

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

#### 2. **onComment and onError Methods**
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

