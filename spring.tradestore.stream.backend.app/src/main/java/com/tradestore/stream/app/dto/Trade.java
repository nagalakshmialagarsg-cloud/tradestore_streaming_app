package com.tradestore.stream.app.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



//lombok annotation
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Trade {
	
       
	private String tradeId;
	private String version;
	
	private String counterpartyId;
	
    
    private String bookId;
	
    
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
	private Date maturityDate;
    
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    private Date createdDate;
	
    
	private String expiry;
	
	
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

	public String toString() {
		return "tradeId: " + getTradeId() + "\n" + "version: " + getVersion() + "\n" + "counterpartyId: "
				+ getCounterpartyId() + "\n" + "bookId: " + getBookId() + "\n" + "maturityDate: " + getMaturityDate() + "\n"
				+ "createdDate: " + getCreatedDate() + "\n" + "expiry: " + getExpiry();
	}




	public static Object builder() {
		// TODO Auto-generated method stub
		return null;
	}




		
	
		
	
}
