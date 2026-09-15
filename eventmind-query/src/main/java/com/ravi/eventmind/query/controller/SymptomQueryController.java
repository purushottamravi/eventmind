package com.ravi.eventmind.query.controller;

import com.ravi.eventmind.query.service.SymptomQueryService;
import com.ravi.eventmind.shared.dto.SymptomRestModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * The front door for symptom queries. Serves the locally-hydrated read model
 * directly - no query gateway involved.
 */
@RestController
@RequestMapping("/symptoms")
public class SymptomQueryController {

    private final SymptomQueryService symptomQueryService;

    public SymptomQueryController(SymptomQueryService symptomQueryService) {
        this.symptomQueryService = symptomQueryService;
    }

    @GetMapping
    public List<SymptomRestModel> getAllSymptoms(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return symptomQueryService.findSymptoms(name, page, size);
    }
}