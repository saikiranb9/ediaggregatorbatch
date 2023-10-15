package com.atd.microservices.core.ediaggregatorbatch.webclients;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.atd.microservices.core.ediaggregatorbatch.domain.EDIConfigResult;
import com.atd.microservices.core.ediaggregatorbatch.exception.EDIAggregatorBatchException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class EDIConfigClient {

	@Autowired
	private WebClient webClient;

	@Value("${spring.application.name}")
	private String applicationName;

	@Value("${ediaggregatorbatch.getEdiConfigByAttrPatternUrl}")
	private String getEdiConfigByAttrPatternUrl;

	public Flux<EDIConfigResult> getEdiGroupSize(List<String> grpSizeAttributeNamePattern) {

		return webClient.get()
				.uri(getEdiConfigByAttrPatternUrl, StringUtils.join(grpSizeAttributeNamePattern, ","))
				.header("XATOM-CLIENTID", applicationName).retrieve()
				.onStatus(HttpStatus::isError,
						exceptionFunction -> Mono
								.error(new EDIAggregatorBatchException("EDIReader returned error attempting to call EdiConfig")))
				.bodyToFlux(EDIConfigResult.class)
				.onErrorResume(e -> Mono.error(new EDIAggregatorBatchException("Error while invoking EDIConfig API", e)));
	}
}