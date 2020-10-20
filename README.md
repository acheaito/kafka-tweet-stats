# Twitter Stats

# Start Postgres
`docker-compose up -d`
## Create database (first time)
`CREATE DATABASE tweetstats`

# Install JDBC Postgres Sink
Upload SinkStatsTopicsToPostgres.json file to Confluent Connect via Control Center

# Start Producer
`mvn -P producer exec:java`

# Start Consumer 
`mvn -P consumer exec:java`