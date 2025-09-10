**tradestore_streaming_app**
-------------------------------

This is the Github repository to store the code for "tradestore_streaming_app" - a springboot application (spring.tradestore.stream.backend.app) posted under Github repository "tradestore_streaming_app".
This application is integrated with the following tools and technologies,

**Technology Stack used for this project:**
  a) Springboot version : 3.5.5 [running on the port: 8080]
  b) Java version: 17
  c) Apache Kafka : 2.13 (uses zookeeper version: 3.6.3) - for streaming as (pub/sub) messaging platform.
  d) MySQL server and UI workbench version 8.0 (community version)
  e) NoSQL MongoDB compass version 1.46.9
  f) Postman - REST API Client to post the trades as "raw" json content type.
  g) PlantUML to draw sequence, class diagrams to show the interations between different objects.
  h) Created Github account, integrated with eGit plugin in Eclipse to push/pull the code into Github repository (https://github.com/nagalakshmialagarsg-cloud/tradestore_streaming_app.git).
  i) CodeQL as scanning and vulnerability tool added as Github workflow actions "codeql.yml"
  j) CI/CD pipeline workflow github action created using "ci.yml"
  k) test cases wrote for few positive and negative test cases.
  l) Implemented all 4 validations before storing the trade into MySQL and MongoDB(NoSQL)

Step 1: Postman
--------------------
Have used "Postman" REST API Client to post the trades which will be received by the Kafka producer, parses the data using Kafka template sends/publishes this message to the Kafka topic.
Here's the sample trade posted using "POST" method as "raw" json message content type.
      {
      "tradeId": "T9",
      "version": "1",
      "counterpartyId": "CP-2",
      "bookId": "B1",
      "maturityDate": "2025-09-13",
      "createdDate": "2025-09-09",
      "expiry": "N"
      }

Here's the REST URI triggered via Postman:  http://localhost:8080/api/v1/tradeEvents
The same trade is getting persisted in both MySQL (SQL db) and NoSQL (MongoDB Compass).

Step 2: Apache Kafka
----------------------
Apache Kafka as a messaging technology (pub/sub) to publish the message to the topic "topic-tradestore" [ Refer: application.properties ] 
and this message is consumed (listening on the port 9092) from the topic and store it in both MySQL and NoSQL (MOngoDB compass).
    a) starting the zookeeper server using batch command--> .\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties

   b) Starting the kafka server using the batch command--> .\bin\windows\kafka-server-start.bat .\config\server.properties
   c) topic name: "topic-tradestore"
   d) consumer group name: "tradestoreGroup"

Step3: MySQL and MongoDB
-------------------------
   a) Have created the MySQL database: "tradestore" using the credentials root as username and password.[datasource url: jdbc:mysql://localhost:3306/tradestore]
   b) Have created MongoDB database: "mongotradestore" [uri: mongodb://localhost:27017/mongotradestore]
   c) MySQL and MongoDB connection strings can be referred at "application.properties".
Here's the DDL script for MySQL table: 

CREATE TABLE `tradestore` (
	  `trade_id` VARCHAR(5) NOT NULL,
	  `version` VARCHAR(5) NOT NULL,
	  `counterparty_id` VARCHAR(5) DEFAULT NULL,
	  `book_id` VARCHAR(5) DEFAULT NULL,
	  `maturity_date` datetime NOT NULL,
	  `created_date` datetime NOT NULL,
	  `expiry` VARCHAR(5) NULL,
  PRIMARY KEY (`trade_id`,`version`)

  All persisted documents can be seen from the MongoDb database: "mongotradestore"
  All documents can be seen at the collection: "trades" 

Step 4: About springboot application
-----------------------------------
a) Have added entity, DTO, repositories(for both MySQL, MongoDB), Kafka Controller, Kafka producer, Kafka consumer, Kafka topic configuration java classes.
b) All 4 validations are written in the consumer class "TradeEventConsumer"

           // Validation #1: Trade expiry flag update if maturity date surpassed
        	 updateExpiryFlag(tradeDto, tradeEntity);
        	
        	// Validation #2: Trade lower version validation and #3: replace if same trade comes
            validateVersion(tradeDto, tradeEntity);         
            
            // Validation #4: Trade Maturity date validation
            validateMaturityDate(tradeDto);

Step 5: 
----------
Installed "eGit" plugin in eclipse to connect to my remote github repository--> https://github.com/nagalakshmialagarsg-cloud/tradestore_streaming_app.git
a) created "feature" branch and uploaded all the code.
b) have created CI pipeline and workflow for github actions -  "ci.yml" for (continuous integration) and "codeql.yml"(for code scanning and vulnerabilities).

Step 6:
---------
Used PlantUML to create class, sequence diagrams to show sequence of behaviour of the tradestore application.
