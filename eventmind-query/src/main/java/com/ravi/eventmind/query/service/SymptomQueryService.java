package com.ravi.eventmind.query.service;

import com.ravi.eventmind.query.entity.Symptom;
import com.ravi.eventmind.query.repository.QuerySymptomRepository;
import com.ravi.eventmind.shared.constants.enums.OriginType;
import com.ravi.eventmind.shared.dto.SymptomRestModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Answers the "give me symptoms" questions straight from the local read model.
 * The Kafka event processor already hydrated the projection, so no query bus or
 * gateway round-trip is needed - just filters, pagination, and entity mapping.
 */
@Service
public class SymptomQueryService {

    private final QuerySymptomRepository repository;

    public SymptomQueryService(QuerySymptomRepository repository) {
        this.repository = repository;
    }

    public List<SymptomRestModel> findSymptoms(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Symptom> symptoms = (name == null || name.isBlank())
                ? repository.findAll(pageable)
                : repository.findByNameContainingIgnoreCase(name, pageable);

        return symptoms.getContent().stream()
                .map(entity -> new SymptomRestModel(
                        entity.getId(),
                        entity.getName(),
                        OriginType.toNames(entity.getOrigin()),
                        entity.getNumberOfOccurance()))
                .toList();
    }
}