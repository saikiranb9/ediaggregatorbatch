package com.atd.microservices.core.ediaggregatorbatch.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EDIConfigResult {
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private String id;
	private String partnerName;
	private List<GroupedField> groupedFields;	
}
