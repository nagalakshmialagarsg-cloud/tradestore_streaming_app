package com.tradestore.stream.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TradeEvent {
	private String message;
	private String status;
	private Trade trade;
	
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Trade getTrade() {
		return trade;
	}
	public void setTrade(Trade trade1) {
		this.trade = trade1;
	}
	
	
	public String toString() {
	    return "message: " + getMessage() + "\n" + "status: "
	            + getStatus() + "\n" + "trade: "
	            + getTrade();
	}
	
	
	


}
