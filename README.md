# Kafka Wikimedia Data Pipeline

This repository is a hands-on project designed to enhance your Kafka skills through real-world application. Created by Stephane from Conduktor, this project involves streaming live data from Wikimedia into Apache Kafka topics using a Kafka Producer. Once the data is in Kafka, a Kafka Consumer will process this data and send it to OpenSearch for indexing and analysis.

## Table of Contents
- [Project Overview](#project-overview)
- [Advanced Kafka Applications](#advanced-kafka-applications)
- [Installing Docker Compose on Debian](#installing-docker-compose-on-debian)
   - [Step 1: Installing Docker Engine](#step-1-installing-docker-engine)
   - [Step 2: Installing Docker Compose](#step-2-installing-docker-compose)
   - [Step 3: Running Docker Compose](#step-3-running-docker-compose)
- [Running the Kafka Wikimedia Data Pipeline](#running-the-kafka-wikimedia-data-pipeline)
- [Accessing the Conduktor Platform](#accessing-the-conduktor-platform)

## Project Overview

In this project, you'll learn how to:

- **Stream Data from Wikimedia**: Use a Kafka Producer to capture real-time data from the Wikimedia stream and send it to Apache Kafka.
- **Consume and Process Data**: Develop a Kafka Consumer that retrieves data from Kafka topics and forwards it to OpenSearch for advanced search and analytics.
- **Explore Kafka Concepts**: Gain practical knowledge of Kafka, including producers, consumers, topics, partitions, and rebalancing strategies.
- **Integration with OpenSearch**: Understand how to integrate Kafka with OpenSearch, leveraging it for powerful search and analytics on the streamed data.

## Advanced Kafka Applications

After completing the initial implementation with Kafka Producers and Consumers, you'll take your skills to the next level by exploring more advanced Kafka concepts:

- **Kafka Connect**: Use the Kafka Connect SSE Source Connector to stream data from Wikimedia directly into Apache Kafka.
- **Kafka Streams**: Implement a Kafka Streams application to perform real-time processing and compute statistics on the data stream.
- **Kafka Connect ElasticSearch Sink**: Finally, utilize the Kafka Connect ElasticSearch Sink to send processed data into OpenSearch, which is an open-source fork of ElasticSearch.

## Installing Docker Compose on Debian

### Step 1: Installing Docker Engine

To use Docker Compose, Docker Engine must be installed first. Follow these steps to install Docker Engine on Debian:

1. **Update the package list**:

    ```bash
    sudo apt update
    ```

2. **Install required dependencies**:

    ```bash
    sudo apt install apt-transport-https ca-certificates curl software-properties-common
    ```

3. **Add Docker's official GPG key**:

    ```bash
    curl -fsSL https://download.docker.com/linux/debian/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
    ```

4. **Add Docker's official repository**:

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

### Step 2: Installing Docker Compose

With Docker Engine installed, the next step is to install Docker Compose:

1. **Install Docker Compose as a plugin**:

    ```bash
    sudo apt install docker-compose-plugin
    ```

2. **Verify the installation**:

    ```bash
    docker compose version
    ```

   Alternatively, you can install Docker Compose as a standalone binary if the above doesn't work:

    ```bash
    sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
    ```

### Step 3: Running Docker Compose

After successfully installing Docker Compose, you can now run your Kafka Wikimedia Data Pipeline using Docker Compose:

1. **Navigate to your project directory**:

    ```bash
    cd ~/Documents/projects/kafka-wikimedia-data-pipeline/conduktor-platform
    ```

2. **Run the Docker Compose command**:

    ```bash
    sudo docker compose up -d
    ```

   This will pull the necessary Docker images and start the services as defined in your `docker-compose.yml` file.

3. **Monitor the output**:

   You should see output similar to the following, indicating that the services are starting:

    ```bash
    [+] Running 58/6
    ✔ schema-registry Pulled                                                                                       344.3s 
    ✔ conduktor-monitoring Pulled                                                                                   73.4s 
    ✔ postgresql Pulled                                                                                             48.3s 
    ✔ conduktor-console Pulled                                                                                     280.1s 
    ✔ zookeeper Pulled                                                                                             165.3s 
    ✔ kafka Pulled                                                                                                 165.3s 
    [+] Running 7/7
    ✔ Network conduktor-platform_default  Created                                                                    0.2s 
    ✔ Container postgresql                Started                                                                    2.2s 
    ✔ Container zookeeper                 Started                                                                    2.2s 
    ✔ Container conduktor-monitoring      Started                                                                    2.5s 
    ✔ Container kafka                     Started                                                                    1.5s 
    ✔ Container conduktor-console         Started                                                                    1.4s 
    ✔ Container schema-registry           Started                                                                    2.1s 
    ```

## Accessing the Conduktor Platform

Once the Docker containers are running, you can access the Conduktor platform to manage and monitor your Kafka cluster.

1. **Open your web browser** and navigate to:

    ```
    http://localhost:8080
    ```

2. **Log in to Conduktor** using the predefined credentials specified in the `platform-config.yml` file:

   - **Email:** `admin@conduktor.io`
   - **Password:** `admin`

3. **Explore the Platform:**
   - Once logged in, you can manage your Kafka cluster, monitor data streams, and utilize other features provided by Conduktor.
