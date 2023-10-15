package com.atd.microservices.core.ediaggregatorbatch.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EDIDocGroupedResult {	
	private String customer;	
	private String type;	
	private String version;	
	private List<String> mongoIds;
	private String fileName;
	private String receiverCode;
}
