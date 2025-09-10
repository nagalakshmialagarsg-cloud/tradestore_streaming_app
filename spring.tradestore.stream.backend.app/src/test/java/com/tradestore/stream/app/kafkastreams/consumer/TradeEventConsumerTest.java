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

    private TradeEvent createTradeEvent(Trade trade) {
        TradeEvent event = new TradeEvent();
        event.setTrade(trade);
        return event;
    }

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

    @Test
    void testConsume_RejectsNullTrade() {
        TradeEvent event = new TradeEvent();
        event.setTrade(null);

        consumer.consume(event);

        verifyNoInteractions(tradeRepository);
        verifyNoInteractions(tradeMongoRepository);
    }

    @Test
    void testUpdateExpiryFlag_SetsExpiryY_WhenMaturityPassed() {
        Date oldMaturity = new Date(System.currentTimeMillis() - 1000000);
        Date newMaturity = new Date(System.currentTimeMillis() + 1000000);

        Trade trade = createTrade("T1", "1", newMaturity, "N");
        TradeEntity entity = createTradeEntity("T1", "1", oldMaturity, "N");

        when(tradeRepository.findByTradeIdentityTradeId("T1")).thenReturn(Arrays.asList(entity));

        // Use reflection or make updateExpiryFlag package-private for direct call if needed
        consumer.consume(createTradeEvent(trade));

        // Should set expiry "Y" because incoming maturityDate is after DB maturityDate
        assertEquals("Y", entity.getExpiry());
    }

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

    @Test
    void testValidateVersion_ReplacesExisting() {
        Trade trade = createTrade("T1", "1", new Date(System.currentTimeMillis() + 1000000), "N");
        TradeEntity existingEntity = createTradeEntity("T1", "1", new Date(), "N");

        when(tradeRepository.findByTradeIdentityTradeId("T1")).thenReturn(Arrays.asList(existingEntity));

        consumer.consume(createTradeEvent(trade));

        verify(tradeRepository).save(any(TradeEntity.class));
        verify(tradeMongoRepository).save(any(TradeStoreDocument.class));
    }

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
