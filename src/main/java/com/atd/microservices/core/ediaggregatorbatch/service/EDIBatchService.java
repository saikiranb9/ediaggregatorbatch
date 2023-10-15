package com.atd.microservices.core.ediaggregatorbatch.service;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.atd.microservices.core.ediaggregatorbatch.configuration.KafkaConfigConstants;
import com.atd.microservices.core.ediaggregatorbatch.domain.EDIConfigResult;
import com.atd.microservices.core.ediaggregatorbatch.domain.EDIDocGroupedResult;
import com.atd.microservices.core.ediaggregatorbatch.webclients.EDIConfigClient;
import com.atd.microservices.core.ediaggregatorbatch.webclients.EDICoreDataClient;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

@Service
@Slf4j
public class EDIBatchService {
	@Autowired
	private EDICoreDataClient ediCoreDataClient;
	@Autowired
	private EDIConfigClient ediConfigClient; 
	@Autowired
	private KafkaConfigConstants kafkaConfigConstants;

	@Autowired
	@Qualifier("ediAggregatorKafkaTemplate")
	private KafkaTemplate<String, EDIDocGroupedResult> ediAggregatorKafkaTemplate;
	
	private final static int DEFAULT_GRP_SIZE = 100;
	private final static String GROUP_SIZE_PATTERN = "groupSize";
	private final static String FILENAME_PATTERN = "fileName";
	
	public void startBatch(Boolean processedFlag, String customer, String type, String version) {
		
		// Get customer config
		Flux<EDIConfigResult> ediConfigResults = ediConfigClient.getEdiGroupSize(Arrays.asList(GROUP_SIZE_PATTERN, FILENAME_PATTERN));
		Map<String, String> ediConfigResultMap = ediConfigResults
	            .flatMap(configResult ->
	                Flux.fromIterable(configResult.getGroupedFields())
	                	.filter(groupedField -> groupedField.getK() != null && groupedField.getV() != null)
	                    .map(groupedField ->
	                    	Tuples.of(configResult.getPartnerName() + "-" + groupedField.getK(), groupedField.getV())
	                    )
	            )
	            .collect(Collectors.toMap(
	                tuple -> tuple.getT1(),
	                tuple -> tuple.getT2()
	            )).block();
		if(log.isDebugEnabled()) {
			log.debug("Group Configs: {}", ediConfigResultMap);
		}
		
		// Get grouped EDIDoc data		
		ediCoreDataClient.getEDIDocsGroupedByCustomerTypeVersion(processedFlag, customer, type, version)
				.flatMap(g -> {
					// For each grp of mongo Ids, update the PROCESSED flag to 'true'
					return ediCoreDataClient.updateEDIDocProcessedFlag(g.getMongoIds(), true)
							.filter(l -> l != null && l.longValue() > 0)
							.map(l -> Tuples.of(g, l));							
				})
				.flatMap(t -> {					
					// Get Group size for customer
					String grpSizeStr = ediConfigResultMap
							.get(t.getT1().getCustomer() + "-" + GROUP_SIZE_PATTERN + t.getT1().getType());
					Integer grpSize = grpSizeStr != null ? Integer.parseInt(grpSizeStr) : DEFAULT_GRP_SIZE;
					String fileName = ediConfigResultMap
							.get(t.getT1().getCustomer() + "-" + FILENAME_PATTERN);
					if (log.isDebugEnabled()) {
						log.debug("grpSize & fileName of {}-{}: {}, {}", t.getT1().getCustomer(), t.getT1().getType(),
								grpSize, fileName);
					}
					
					// Partition the EDIDocGroupedResult by group size
					return Flux.fromStream(
							ListUtils.partition(t.getT1().getMongoIds(), grpSize).stream()
							.map(grp -> new EDIDocGroupedResult(t.getT1().getCustomer(), t.getT1().getType(),
									t.getT1().getVersion(), grp, fileName, t.getT1().getReceiverCode())));
				})
				// Push to kafka in batches
				.flatMap(ediGrp -> Mono.fromFuture(ediAggregatorKafkaTemplate
						.send(kafkaConfigConstants.KAFKA_TOPIC_OUTBOUND, ediGrp).completable()))
				.subscribe();
	}

}
