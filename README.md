# Twitter Stats
Collect tweets and run aggregations using Confluent Platform / Kafka

# Setup
## Start Postgres
`docker-compose up -d`

## Create database
`CREATE DATABASE tweetstats`

## Set up Confluent Platform
Clone the Confluent Platform repo from 

`https://github.com/confluentinc/cp-all-in-one.git`

`cd confluentinc/cp-all-in-one`
`docker-compose up -d`

## Create topics
Shell into broker container
 
`docker-compose exec broker bash`

Create **tweets** partitioned topic

`kafka-topics --bootstrap-server broker:9092 --create --topic tweets --partitions 5`

Create **tweets-sanitized** partitioned topic

`kafka-topics --bootstrap-server broker:9092 --create --topic tweets-sanitized --partitions 5`

## Install JDBC Postgres Sink
Upload SinkStatsTopicsToPostgres.json file to Confluent Connect via <a href='http://localhost:9021/'>Control Center</a>

# Building the project
`mvn clean install`

# Running the Application
## Start Producer
`mvn -pl tweet-producer exec:java`

## Start Language Fitler 
`mvn -pl language-filter exec:java`

## Start Aggregator 
`mvn -pl tweet-aggregator exec:java`
