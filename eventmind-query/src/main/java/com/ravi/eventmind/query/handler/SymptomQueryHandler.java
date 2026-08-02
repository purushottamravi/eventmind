package com.ravi.eventmind.query.handler;

import com.ravi.eventmind.query.entity.Symptom;
import com.ravi.eventmind.query.queries.GetSymptomsQuery;
import com.ravi.eventmind.query.repository.QuerySymptomRepository;
import com.ravi.eventmind.shared.constants.enums.OriginType;
import com.ravi.eventmind.shared.dto.SymptomRestModel;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Answers the "give me symptoms" questions. Pokes the repository, filters by name
 * when asked, pages the results, and turns entities into the API-friendly model.
 */
@Component
public class SymptomQueryHandler {
    private final QuerySymptomRepository repository;

    public SymptomQueryHandler(QuerySymptomRepository repository) {
        this.repository = repository;
    }

    @QueryHandler
    public List<SymptomRestModel> handle(GetSymptomsQuery query) {
        Pageable pageable = PageRequest.of(query.page(), query.size());
        Page<Symptom> symptoms = (query.name() == null || query.name().isBlank())
                ? repository.findAll(pageable)
                : repository.findByNameContainingIgnoreCase(query.name(), pageable);

        return symptoms.getContent().stream()
                .map(entity -> new SymptomRestModel(
                        entity.getId(),
                        entity.getName(),
                        OriginType.toNames(entity.getOrigin()),
                        entity.getNumberOfOccurance()))
                .toList();
    }
}
