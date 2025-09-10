package com.tradestore.stream.app.entities;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//class contain more than 1 primary key columns.

//lombok annotations
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
//spring jpa annotation
@Embeddable
public class TradeIdentity implements Serializable{
	@Column(nullable = false)
	String tradeId;
    
	@Column(name = "Version", nullable = false)
    String version;
	
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
}
