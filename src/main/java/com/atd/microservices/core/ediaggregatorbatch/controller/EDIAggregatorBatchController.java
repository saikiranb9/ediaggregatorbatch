package com.atd.microservices.core.ediaggregatorbatch.controller;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;

import com.atd.microservices.core.ediaggregatorbatch.domain.ErrorDetails;
import com.atd.microservices.core.ediaggregatorbatch.exception.EDIAggregatorBatchException;
import com.atd.microservices.core.ediaggregatorbatch.service.EDIBatchService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(path="/")
public class EDIAggregatorBatchController {
	@Autowired
	private EDIBatchService ediBatchService;
	
	@ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(
            value = "Start grouping of EDI docs based on customer, type and version."
            		+ "If no param is provided, it start the grouping of all EDI documents present in the "
            		+ "EDIDocs collection - whose 'PROCESSED' attribute is false",
            notes = "JSON Supported", response = String.class
    )
    @ApiResponses({
			@ApiResponse(code = 400, message = "Fields are with validation errors", response = ErrorDetails.class),
			@ApiResponse(code = 404, message = "Data not found for given vendor products", response = ErrorDetails.class),
			@ApiResponse(code = 406, message = "Request not acceptable for EDI Data", response = ErrorDetails.class),
			@ApiResponse(code = 424, message = "Request not processed due to failed dependecy EDI Data", response = ErrorDetails.class)
    })
	@PostMapping(value = "/start", produces = MediaType.TEXT_PLAIN_VALUE)
	public String startBatch(
			@ApiParam(value = "Processed Flag value", required = true) @RequestParam(name = "processedFlag", required = false) Boolean processedFlag,
			@ApiParam(value = "Customer Name", required = true) @RequestParam(name = "customer", required = false) String customer,
			@ApiParam(value = "EDI Document Type", required = true) @RequestParam(name = "type", required = false) String type,
			@ApiParam(value = "EDI Document Version", required = true) @RequestParam(name = "version", required = false) String version) {
		ediBatchService.startBatch(processedFlag, customer, type, version);
		return "EDIDocs-Aggregation batch job started";
	}
	
	
	@ExceptionHandler({Exception.class,RuntimeException.class,Throwable.class})
	public final ResponseEntity<ErrorDetails> handleAllExceptions(Exception ex) {
		ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(), "");
		HttpStatus responseCode = HttpStatus.INTERNAL_SERVER_ERROR;
		
		if (ex instanceof EDIAggregatorBatchException) {
			responseCode = HttpStatus.INTERNAL_SERVER_ERROR;
		} else if (ex instanceof WebExchangeBindException) {
			responseCode = HttpStatus.BAD_REQUEST;
		}
		log.error("Response Code: {}, Message: {}", responseCode.value(), ex.getMessage(), ex);
		return new ResponseEntity<>(errorDetails, responseCode);
	}
}
