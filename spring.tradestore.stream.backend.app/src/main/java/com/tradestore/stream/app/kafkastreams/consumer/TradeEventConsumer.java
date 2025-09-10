package com.tradestore.stream.app.kafkastreams.consumer;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.tradestore.stream.app.dto.Trade;
import com.tradestore.stream.app.dto.TradeEvent;
import com.tradestore.stream.app.entities.TradeEntity;
import com.tradestore.stream.app.entities.TradeIdentity;
import com.tradestore.stream.app.entities.mongo.documents.TradeStoreDocument;
import com.tradestore.stream.app.entities.mongo.repositories.TradeStoreMongoRepository;
import com.tradestore.stream.app.entities.repositories.TradeRepository;

@Service
public class TradeEventConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TradeEventConsumer.class);

    private final TradeRepository tradeRepository; //for MySQL database
    
    private final TradeStoreMongoRepository tradeMongoRepository; //for NoSQL MongoDB

    public TradeEventConsumer(TradeRepository tradeRepository, TradeStoreMongoRepository tradeMongoRepository) {
        this.tradeRepository = tradeRepository;
        this.tradeMongoRepository = tradeMongoRepository;
    }

    @KafkaListener(topics = "${spring.kafka.topic.name}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(TradeEvent tradeEvent) {
        LOGGER.info("Trade Event received => {}", tradeEvent);

        Trade tradeDto = tradeEvent.getTrade();
        if (tradeDto == null) {
            LOGGER.warn("Received tradeEvent with null trade. so, skipping...");
            return;
        }

        // Map DTO to entity - for SQL database (MySQL)
        TradeEntity tradeEntity = mapToEntity(tradeDto);
        
        

        try {
            
        	// Validation Rule #1: Trade expiry flag update if maturity date surpassed
        	 updateExpiryFlag(tradeDto, tradeEntity);
        	
        	// Validation Rule #2: Trade Version validation and replace if same trade comes
            validateVersion(tradeDto, tradeEntity);         
            
            // Validation Rule #3: Trade Maturity date validation
            validateMaturityDate(tradeDto);

            // Save to DB - MySQL database
            tradeRepository.save(tradeEntity);
            
            //for NoSQL MongoDB
            TradeStoreDocument mongoDoc = mapToDocument(tradeEntity);
            tradeMongoRepository.save(mongoDoc);
            
            LOGGER.info("Trade {} saved successfully", tradeDto.getTradeId());

        } catch (IllegalArgumentException ex) {
            LOGGER.error("Validation failed for trade {}: {}", tradeDto.getTradeId(), ex.getMessage());
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while processing trade {}: {}", tradeDto.getTradeId(), ex.getMessage(), ex);
        }
    }

    // ---------------------------------------------------------------------------
    // Validation Rule #1: Expiry flag update if maturity date surpasses
    // -------------------------------------------------------------------------
    private void updateExpiryFlag(Trade tradeDto, TradeEntity tradeEntity) {
        List<TradeEntity> existingTrades = tradeRepository.findByTradeIdentityTradeId(tradeDto.getTradeId());

        int incomingVersion = Integer.parseInt(tradeDto.getVersion());

        for (TradeEntity existing : existingTrades) {
            int dbVersion = Integer.parseInt(existing.getTradeIdentity().getVersion());            
            
            
            Date  dbMaturityDate = existing.getMaturityDate();
            Date incomingMaturityDt = tradeDto.getMaturityDate();
            
            String dbTradeId = existing.getTradeIdentity().getTradeId();
            String incomingTradeId = tradeDto.getTradeId();
            
            String dbCp = existing.getCounterpartyId();
            String incomingCp = tradeDto.getCounterpartyId();
                                   
            if ((incomingTradeId.equals(dbTradeId)) && (incomingVersion == dbVersion) && (incomingCp.equals(dbCp)) && (incomingMaturityDt.after(dbMaturityDate))) {
            	//update expiry flag to Y as maturity date surpassed for the given trade
            	tradeEntity.setExpiry("Y");
                LOGGER.info("Marked trade {} as expired", tradeEntity.getTradeIdentity().getTradeId());
            }else {
                tradeEntity.setExpiry("N");
            }           
            
        }
    }

        
    
    // --------------------------------------------------------------------------------------------------------------
    // Validation Rules : 2) Rejects trade if incoming version is less than existing version for the incoming trade
    //                    3) Replace existing record if same trade comes
    //
    // --------------------------------------------------------------------------------------------------------------
    private void validateVersion(Trade tradeDto, TradeEntity tradeEntity) {
        List<TradeEntity> existingTrades = tradeRepository.findByTradeIdentityTradeId(tradeDto.getTradeId());

        int incomingVersion = Integer.parseInt(tradeDto.getVersion());

        for (TradeEntity existing : existingTrades) {
            int dbVersion = Integer.parseInt(existing.getTradeIdentity().getVersion());

            if (incomingVersion < dbVersion) {
                throw new IllegalArgumentException("Rejected trade " + tradeDto.getTradeId()
                        + " because incoming version " + incomingVersion
                        + " is less than existing version " + dbVersion);
            }

            if (incomingVersion == dbVersion) {
                // Replace existing trade (overwrite)
               	tradeEntity.setTradeIdentity(existing.getTradeIdentity()); // preserve same trade identity of DB for update     	
                              
                LOGGER.info("Incoming trade {} with version {} will replace existing record",
                        tradeDto.getTradeId(), incomingVersion);
            }            
            
        }
    }
    
 
    
    // --------------------------------------------------------
    // Validation Rule #4: Maturity date validation
    // ------------------------------------------------------
    private void validateMaturityDate(Trade tradeDto) {
        Date today = new Date();
        Date maturityDate = tradeDto.getMaturityDate();

        if (maturityDate == null) {
            throw new IllegalArgumentException("Trade " + tradeDto.getTradeId() + " has null maturity date");
        }

        if (today.after(maturityDate)) {
            throw new IllegalArgumentException("Rejected trade " + tradeDto.getTradeId()
                    + " because maturity date " + maturityDate + " is before today " + today);
        }
    }
   

    // -------------------------------------------------
    // Mapping between DTO and Entity classes
    // -------------------------------------------------
    private TradeEntity mapToEntity(Trade tradeDto) {
        TradeIdentity identity = new TradeIdentity();
        identity.setTradeId(tradeDto.getTradeId());
        identity.setVersion(tradeDto.getVersion());

        TradeEntity entity = new TradeEntity();
        entity.setTradeIdentity(identity);
        entity.setCounterpartyId(tradeDto.getCounterpartyId());
        entity.setBookId(tradeDto.getBookId());
        entity.setMaturityDate(tradeDto.getMaturityDate());
        entity.setCreatedDate(tradeDto.getCreatedDate());
        entity.setExpiry(tradeDto.getExpiry());

        return entity;
    }
    
    //Map to NoSQL MongoDB 
    private TradeStoreDocument mapToDocument(TradeEntity entity) {
        TradeStoreDocument doc = new TradeStoreDocument();
        doc.setTradeId(entity.getTradeIdentity().getTradeId());
        doc.setVersion(entity.getTradeIdentity().getVersion());
        doc.setCounterpartyId(entity.getCounterpartyId());
        doc.setBookId(entity.getBookId());
        doc.setMaturityDate(entity.getMaturityDate());
        doc.setCreatedDate(entity.getCreatedDate());
        doc.setExpiry(entity.getExpiry());        
        return doc;
    }

    
}
