package com.ravi.eventmind.query.controller;

import com.ravi.eventmind.query.queries.GetSymptomsQuery;
import com.ravi.eventmind.shared.dto.SymptomRestModel;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * The front door for symptom queries. Takes HTTP calls, asks the query gateway,
 * and sends the answers back as JSON.
 */
@RestController
@RequestMapping("/symptoms")
@Slf4j
public class SymptomQueryContoller {
    private final QueryGateway queryGateway;

    public SymptomQueryContoller(QueryGateway queryGateway) {
        this.queryGateway = queryGateway;
    }

    @GetMapping
    public List<SymptomRestModel> getAllSymptoms(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        GetSymptomsQuery getSymptomsQuery = new GetSymptomsQuery(name, page, size);

        List<SymptomRestModel> productRestModels = queryGateway.query(getSymptomsQuery, ResponseTypes.multipleInstancesOf(SymptomRestModel.class)).join();

        return productRestModels;
    }
}
