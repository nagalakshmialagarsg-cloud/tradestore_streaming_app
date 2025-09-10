package com.tradestore.stream.app.entities.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tradestore.stream.app.entities.TradeEntity;
import com.tradestore.stream.app.entities.TradeIdentity;

@Repository
public interface TradeRepository extends JpaRepository<TradeEntity, TradeIdentity>{
	
	Optional<TradeEntity> findByCounterpartyId(String counterpartyId);
	Optional<TradeEntity> findByTradeIdentity(TradeIdentity tradeIdentity);
	List<TradeEntity> findByTradeIdentityTradeId(String tradeId);
	
}
