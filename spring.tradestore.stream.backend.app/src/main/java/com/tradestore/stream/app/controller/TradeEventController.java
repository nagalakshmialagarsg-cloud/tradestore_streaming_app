package com.tradestore.stream.app.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradestore.stream.app.dto.Trade;
import com.tradestore.stream.app.dto.TradeEvent;
import com.tradestore.stream.app.kafkastreams.producer.TradeEventProducer;

@RestController
@RequestMapping("/api/v1")
public class TradeEventController {
	
	private TradeEventProducer tradeEventProducer;

	//constructor based dependency injection
	
	public TradeEventController(TradeEventProducer tradeEventProducer) {
		
		this.tradeEventProducer = tradeEventProducer;
	}
	
	
	//create REST endpoints
	@PostMapping("/tradeEvents")
	public String placeTheTrade(@RequestBody Trade trade) {
		trade.toString();
		TradeEvent tradeEvent = new TradeEvent();
		tradeEvent.setStatus("PENDING");
		tradeEvent.setMessage("trade event is in pending status");
		tradeEvent.setTrade(trade);
		
		//kafka producer publishes the trade message into Kafka topic	
		tradeEventProducer.sendMessage(tradeEvent);
		return "trade event published to topic successfully!";
	}
	

}
