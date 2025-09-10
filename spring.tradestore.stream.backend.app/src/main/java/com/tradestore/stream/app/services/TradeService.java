package com.tradestore.stream.app.services;

import java.util.List;

import com.tradestore.stream.app.entities.TradeEntity;
import com.tradestore.stream.app.entities.TradeIdentity;

public interface TradeService {
	
	// save the trades into repository 
	
	TradeEntity save(TradeEntity trade);
	// get the trades from repository by counterparty Id
	TradeEntity getByCounterpartyId(String counterpartyId);
	
	//get video by (tradeId+Version) - tradeIdentity
	TradeEntity get(TradeIdentity tradeIdentity);
	
	List<TradeEntity> getAll();

}
