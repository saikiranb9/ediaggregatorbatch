package com.atd.microservices.core.ediaggregatorbatch.webclients;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.atd.microservices.core.ediaggregatorbatch.domain.EDIDoc;
import com.atd.microservices.core.ediaggregatorbatch.domain.EDIDocGroupedResult;
import com.atd.microservices.core.ediaggregatorbatch.exception.EDIAggregatorBatchException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class EDICoreDataClient {
	
	@Autowired
	private WebClient webClient;

	@Value("${spring.application.name}")
	private String applicationName;

	@Value("${ediaggregatorbatch.updateEDIDocProcessedFlagUrl}")
	private String updateEDIDocProcessedFlagUrl;
	
	@Value("${ediaggregatorbatch.getGroupedEDIDocsUrl}")
	private String getGroupedEDIDocsUrl;
	
	public Mono<Long> updateEDIDocProcessedFlag(List<String> mongoIds, Boolean processedFlag) {
		
		return webClient.put()
				.uri(updateEDIDocProcessedFlagUrl, processedFlag)
				.header("XATOM-CLIENTID", applicationName)
				.contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(mongoIds))
				.retrieve()
				.onStatus(HttpStatus::isError,
						exceptionFunction -> Mono.error(
								new EDIAggregatorBatchException("EDICoreData returned error attempting to call update EDIDocs")))
				.bodyToMono(Long.class)
				.onErrorResume(e -> Mono.error(new EDIAggregatorBatchException(
						"Error while invoking update EDIDocs API", e)));		
	}
	
	public Flux<EDIDocGroupedResult> getEDIDocsGroupedByCustomerTypeVersion(Boolean processedFlag, String customer,
			String type, String version) {
		MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
		if (processedFlag != null) {
			queryParams.add("processed", processedFlag.toString());
		}
		if (customer != null) {
			queryParams.add("customer", customer);
		}
		if (type != null) {
			queryParams.add("type", type);
		}
		if (version != null) {
			queryParams.add("version", version);
		}
		
		return webClient.get()
				.uri(getGroupedEDIDocsUrl, builder -> builder.queryParams(queryParams).build())
				.header("XATOM-CLIENTID", applicationName)
				.accept(MediaType.APPLICATION_JSON)				
				.retrieve()
				.onStatus(HttpStatus::isError,
						exceptionFunction -> Mono.error(
								new EDIAggregatorBatchException("EDICoreData returned error attempting to call Get Grouped EDIDocs")))
				.bodyToFlux(EDIDocGroupedResult.class)
				.onErrorResume(e -> Flux.error(new EDIAggregatorBatchException(
						"Error while invoking Get Grouped EDIDocs API", e)));
	}

}
