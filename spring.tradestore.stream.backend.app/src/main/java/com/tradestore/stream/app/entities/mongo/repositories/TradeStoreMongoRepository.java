package com.tradestore.stream.app.entities.mongo.repositories;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.tradestore.stream.app.entities.mongo.documents.TradeStoreDocument;

	
	@Repository
	public interface TradeStoreMongoRepository extends MongoRepository<TradeStoreDocument, String> {
	    List<TradeStoreDocument> findByTradeId(String tradeId);
	}



