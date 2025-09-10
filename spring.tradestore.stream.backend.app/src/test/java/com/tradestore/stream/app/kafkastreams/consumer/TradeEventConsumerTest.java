package com.tradestore.stream.app.kafkastreams.consumer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.tradestore.stream.app.dto.Trade;
import com.tradestore.stream.app.dto.TradeEvent;
import com.tradestore.stream.app.entities.TradeEntity;
import com.tradestore.stream.app.entities.TradeIdentity;
import com.tradestore.stream.app.entities.mongo.documents.TradeStoreDocument;
import com.tradestore.stream.app.entities.mongo.repositories.TradeStoreMongoRepository;
import com.tradestore.stream.app.entities.repositories.TradeRepository;

// This is the regression test suite to test happy path and negative scenarios.
public class TradeEventConsumerTest {
	@Mock
    private TradeRepository tradeRepository;

    @Mock
    private TradeStoreMongoRepository tradeMongoRepository;

    @InjectMocks
    private TradeEventConsumer consumer;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    //This method creates the trade
    private Trade createTrade(String tradeId, String version, Date maturityDate, String expiry) {
        Trade trade = new Trade();
        trade.setTradeId(tradeId);
        trade.setVersion(version);
        trade.setCounterpartyId("CP-1");
        trade.setBookId("B1");
        trade.setMaturityDate(maturityDate);
        trade.setCreatedDate(new Date());
        trade.setExpiry(expiry);
        return trade;
    }

    //creates the trade event
    private TradeEvent createTradeEvent(Trade trade) {
        TradeEvent event = new TradeEvent();
        event.setTrade(trade);
        return event;
    }

    //creates table entity DTOs to set values to persist values into in-memory H2 db using Mockito
    private TradeEntity createTradeEntity(String tradeId, String version, Date maturityDate, String expiry) {
        TradeIdentity id = new TradeIdentity();
        id.setTradeId(tradeId);
        id.setVersion(version);

        TradeEntity entity = new TradeEntity();
        entity.setTradeIdentity(id);
        entity.setCounterpartyId("CP-1");
        entity.setBookId("B1");
        entity.setMaturityDate(maturityDate);
        entity.setCreatedDate(new Date());
        entity.setExpiry(expiry);
        return entity;
    }

    //This is the test method to verify if the trade is persisted successfully in both MySQL and MongoDB
    @Test
    void testConsume_SuccessfulSave() {
        Date futureDate = new Date(System.currentTimeMillis() + 10000000);
        Trade trade = createTrade("T1", "1", futureDate, "N");
        TradeEvent event = createTradeEvent(trade);

        // No existing trades in repo
        when(tradeRepository.findByTradeIdentityTradeId("T1")).thenReturn(Collections.emptyList());

        consumer.consume(event);

        // Verify save called on both repos
        verify(tradeRepository).save(any(TradeEntity.class));
        verify(tradeMongoRepository).save(any(TradeStoreDocument.class));
    }

    //This test is to validate the trade to reject the null trade
    @Test
    void testConsume_RejectsNullTrade() {
        TradeEvent event = new TradeEvent();
        event.setTrade(null);

        consumer.consume(event);

        verifyNoInteractions(tradeRepository);
        verifyNoInteractions(tradeMongoRepository);
    }

    //This test method is to update expiry flag to 'Y' if maturity date surpassed the date for the given trade.
    @Test
    void testUpdateExpiryFlag_SetsExpiryY_WhenMaturityPassed() {
        Date oldMaturity = new Date(System.currentTimeMillis() - 1000000);
        Date newMaturity = new Date(System.currentTimeMillis() + 1000000);

        Trade trade = createTrade("T1", "1", newMaturity, "N");
        TradeEntity entity = createTradeEntity("T1", "1", oldMaturity, "N");

        when(tradeRepository.findByTradeIdentityTradeId("T1")).thenReturn(Arrays.asList(entity));

        consumer.consume(createTradeEvent(trade));

        // Should set expiry "Y" because incoming maturityDate is after DB maturityDate
        assertEquals("Y", entity.getExpiry());
    }

    //This test method is to test if the incoming trade version is lower than the version persisted in database for the given trade.
    @Test
    void testValidateVersion_ThrowsForLowerVersion() {
        Trade trade = createTrade("T1", "1", new Date(System.currentTimeMillis() + 1000000), "N");
        TradeEntity existingEntity = createTradeEntity("T1", "2", new Date(), "N");

        when(tradeRepository.findByTradeIdentityTradeId("T1")).thenReturn(Arrays.asList(existingEntity));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            consumer.consume(createTradeEvent(trade));
        });
        assertTrue(ex.getMessage().contains("Rejected trade"));
    }

    //This test is to replace the trade in table if same trade comes from kafka topic.
    @Test
    void testValidateVersion_ReplacesExisting() {
        Trade trade = createTrade("T1", "1", new Date(System.currentTimeMillis() + 1000000), "N");
        TradeEntity existingEntity = createTradeEntity("T1", "1", new Date(), "N");

        when(tradeRepository.findByTradeIdentityTradeId("T1")).thenReturn(Arrays.asList(existingEntity));

        consumer.consume(createTradeEvent(trade));

        verify(tradeRepository).save(any(TradeEntity.class));
        verify(tradeMongoRepository).save(any(TradeStoreDocument.class));
    }

   //This test method is validating if maturity date is null. 
    @Test
    void testValidateMaturityDate_ThrowsForNullDate() {
        Trade trade = createTrade("T1", "1", null, "N");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            consumer.consume(createTradeEvent(trade));
        });
        assertTrue(ex.getMessage().contains("has null maturity date"));
    }

    @Test
    void testValidateMaturityDate_ThrowsForPastDate() {
        Trade trade = createTrade("T1", "1", new Date(System.currentTimeMillis() - 10000000), "N");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            consumer.consume(createTradeEvent(trade));
        });
        assertTrue(ex.getMessage().contains("Rejected trade"));
    }
}
