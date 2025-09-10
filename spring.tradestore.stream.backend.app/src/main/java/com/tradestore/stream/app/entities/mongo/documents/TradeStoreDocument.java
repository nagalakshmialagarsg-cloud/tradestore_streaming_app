package com.tradestore.stream.app.entities.mongo.documents;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;


@Document(collection = "trades")
public class TradeStoreDocument {
	
	    @Id
	    private String id;
	    private String tradeId;
	    private String version;
	    private String counterpartyId;
	    private String bookId;
	    private Date maturityDate;
	    private Date createdDate;
	    private String expiry;

	    public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getTradeId() {
			return tradeId;
		}
		public void setTradeId(String tradeId) {
			this.tradeId = tradeId;
		}
		public String getVersion() {
			return version;
		}
		public void setVersion(String version) {
			this.version = version;
		}
		public String getCounterpartyId() {
			return counterpartyId;
		}
		public void setCounterpartyId(String counterpartyId) {
			this.counterpartyId = counterpartyId;
		}
		public String getBookId() {
			return bookId;
		}
		public void setBookId(String bookId) {
			this.bookId = bookId;
		}
		public Date getMaturityDate() {
			return maturityDate;
		}
		public void setMaturityDate(Date maturityDate) {
			this.maturityDate = maturityDate;
		}
		public Date getCreatedDate() {
			return createdDate;
		}
		public void setCreatedDate(Date createdDate) {
			this.createdDate = createdDate;
		}
		public String getExpiry() {
			return expiry;
		}
		public void setExpiry(String expiry) {
			this.expiry = expiry;
		}
}
