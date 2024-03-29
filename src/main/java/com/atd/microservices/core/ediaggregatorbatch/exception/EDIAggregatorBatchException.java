package com.atd.microservices.core.ediaggregatorbatch.exception;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EDIAggregatorBatchException extends RuntimeException{
	
	private static final long serialVersionUID = -2589421071244904734L;
	
	private Date timestamp;
	private String message;
	
	public EDIAggregatorBatchException(String message) {
		super(message);
		this.message = message;
	}
	
	public EDIAggregatorBatchException(Date timestamp, String message) {
		super();
		this.timestamp = timestamp;
		this.message = message;
	}
	
	public EDIAggregatorBatchException(String message, Throwable e) {
		super(message, e);
		this.message = message;
	}

}
