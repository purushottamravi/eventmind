package com.ravi.eventmind.query.handler;

import com.ravi.eventmind.shared.dto.SymptomRestModel;
import com.ravi.eventmind.query.entity.Symptom;
import com.ravi.eventmind.query.queries.GetSymptomsQuery;
import com.ravi.eventmind.query.repository.QuerySymptomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Proves the query handler filters, pages, and maps symptoms properly, so the read
 * model answers actually make sense.
 */
@ExtendWith(MockitoExtension.class)
class SymptomQueryHandlerTest {

    @Mock
    private QuerySymptomRepository repository;

    @Test
    void handle_shouldFilterByNameWhenNameProvided() {
        SymptomQueryHandler handler = new SymptomQueryHandler(repository);

        Pageable pageable = PageRequest.of(0, 20);
        Symptom headache = view("symptom-1", "Headache from memory pressure", 10L, 5);
        when(repository.findByNameContainingIgnoreCase("headache", pageable))
                .thenReturn(new PageImpl<>(List.of(headache), pageable, 1));

        List<SymptomRestModel> result = handler.handle(new GetSymptomsQuery("headache", 0, 20));

        assertEquals(1, result.size());
        assertEquals("symptom-1", result.get(0).id());
        assertEquals("Headache from memory pressure", result.get(0).name());
        assertEquals(List.of("B", "D"), result.get(0).origin());
        assertEquals(5, result.get(0).numberOfOccurance());
    }

    @Test
    void handle_shouldReturnAllWhenNoNameProvided() {
        SymptomQueryHandler handler = new SymptomQueryHandler(repository);

        Pageable pageable = PageRequest.of(0, 20);
        when(repository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(
                        view("symptom-1", "Headache from memory pressure", 10L, 5),
                        view("symptom-2", "Backache from failed command", 20L, 2)
                ), pageable, 2));

        List<SymptomRestModel> result = handler.handle(new GetSymptomsQuery(null, 0, 20));

        assertEquals(2, result.size());
    }

    @Test
    void handle_shouldReturnEmptyListWhenNothingMatches() {
        SymptomQueryHandler handler = new SymptomQueryHandler(repository);

        Pageable pageable = PageRequest.of(0, 20);
        when(repository.findByNameContainingIgnoreCase("zzz", pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        List<SymptomRestModel> result = handler.handle(new GetSymptomsQuery("zzz", 0, 20));

        assertTrue(result.isEmpty());
    }

    @Test
    void handle_shouldApplyPagination() {
        SymptomQueryHandler handler = new SymptomQueryHandler(repository);

        Pageable pageable = PageRequest.of(0, 1);
        when(repository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(view("symptom-1", "Headache", 10L, 5)), pageable, 3));

        List<SymptomRestModel> result = handler.handle(new GetSymptomsQuery(null, 0, 1));

        assertEquals(1, result.size());
        assertEquals("Headache", result.get(0).name());
    }

    private Symptom view(String id, String name, Long origin, Integer occurrences) {
        Symptom view = new Symptom();
        view.setId(id);
        view.setName(name);
        view.setOrigin(origin);
        view.setNumberOfOccurance(occurrences);
        return view;
    }
}
