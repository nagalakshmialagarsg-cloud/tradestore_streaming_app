package com.tradestore.stream.app.kafkastreams.producer;

import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import com.tradestore.stream.app.dto.TradeEvent;

@Service
public class TradeEventProducer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TradeEventProducer.class);
	
	private NewTopic topic;
	private KafkaTemplate<String, TradeEvent> kafkaTemplate;
	
	public TradeEventProducer(NewTopic topic, KafkaTemplate<String, TradeEvent> kafkaTemplate) {
		
		this.topic = topic;
		this.kafkaTemplate = kafkaTemplate;
	}
	
	public void sendMessage(TradeEvent trade) {
		LOGGER.info(String.format("Trade event => %s", trade.toString()));
		
		//create a trade message
		Message<TradeEvent> message = MessageBuilder
				.withPayload(trade)
				.setHeader(KafkaHeaders.TOPIC, topic.name())
				.build();
		
		kafkaTemplate.send(message);
		
		
	}
	
	

}
