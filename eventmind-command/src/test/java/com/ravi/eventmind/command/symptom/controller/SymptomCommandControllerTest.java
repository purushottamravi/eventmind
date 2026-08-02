package com.ravi.eventmind.command.symptom.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ravi.eventmind.command.symptom.commands.CreateSymptomCommand;
import com.ravi.eventmind.shared.exceptions.ProblemDetailAdvice;
import com.ravi.eventmind.shared.validation.JsonSchemaValidator;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.command.AggregateStreamCreationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SymptomCommandControllerTest {

    private static final String CLIENT_ID = "3f0c3f3f-4f4f-4f4f-8f8f-3f3f3f3f3f3f";

    @Mock
    private CommandGateway commandGateway;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonSchemaValidator validator;
        try (InputStream in = new ClassPathResource("schema/symptom-command-request.schema.json").getInputStream()) {
            validator = JsonSchemaValidator.from(in, objectMapper);
        }
        SymptomCommandController controller = new SymptomCommandController(commandGateway, validator, objectMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ProblemDetailAdvice())
                .build();
    }

    @Test
    void addSymptoms_shouldGenerateIdAndSendCommandWhenIdOmitted() throws Exception {
        when(commandGateway.sendAndWait(any())).thenReturn(123L);

        MvcResult result = mockMvc.perform(post("/symptoms")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "High memory usage",
                                  "origin": ["A", "C"],
                                  "numberOfOccurance": 2
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        ArgumentCaptor<CreateSymptomCommand> captor = ArgumentCaptor.forClass(CreateSymptomCommand.class);
        verify(commandGateway).sendAndWait(captor.capture());

        CreateSymptomCommand sent = captor.getValue();
        assertTrue(sent.id() != null && !sent.id().isBlank());
        assertEquals("High memory usage", sent.name());
        assertEquals(5L, sent.origin());
        assertEquals(2, sent.numberOfOccurance());
        assertEquals("Created Symptom with Id : " + sent.id(),
                result.getResponse().getContentAsString());
    }

    @Test
    void addSymptoms_shouldUseClientSuppliedIdAsIdempotencyKey() throws Exception {
        when(commandGateway.sendAndWait(any())).thenReturn(456L);

        mockMvc.perform(post("/symptoms")
                        .contentType("application/json")
                        .content("""
                                {
                                  "id": "3f0c3f3f-4f4f-4f4f-8f8f-3f3f3f3f3f3f",
                                  "name": "High memory usage",
                                  "origin": ["A", "C"],
                                  "numberOfOccurance": 2
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("Created Symptom with Id : " + CLIENT_ID));

        ArgumentCaptor<CreateSymptomCommand> captor = ArgumentCaptor.forClass(CreateSymptomCommand.class);
        verify(commandGateway).sendAndWait(captor.capture());
        assertEquals(CLIENT_ID, captor.getValue().id());
    }

    @Test
    void addSymptoms_shouldTreatDuplicateCreateAsIdempotentSuccess() throws Exception {
        when(commandGateway.sendAndWait(any()))
                .thenThrow(new AggregateStreamCreationException("stream already exists"));

        mockMvc.perform(post("/symptoms")
                        .contentType("application/json")
                        .content("""
                                {
                                  "id": "3f0c3f3f-4f4f-4f4f-8f8f-3f3f3f3f3f3f",
                                  "name": "High memory usage",
                                  "origin": ["A", "C"],
                                  "numberOfOccurance": 2
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("Created Symptom with Id : " + CLIENT_ID));

        ArgumentCaptor<CreateSymptomCommand> captor = ArgumentCaptor.forClass(CreateSymptomCommand.class);
        verify(commandGateway).sendAndWait(captor.capture());
        assertEquals(CLIENT_ID, captor.getValue().id());
    }

    @Test
    void addSymptoms_shouldConvertGatewayFailureToProblemDetail() throws Exception {
        when(commandGateway.sendAndWait(any())).thenThrow(new IllegalStateException("command failed"));

        mockMvc.perform(post("/symptoms")
                        .contentType("application/json")
                        .content("""
                                  {
                                    "name": "API error",
                                    "origin": ["A"],
                                    "numberOfOccurance": 1
                                  }
                                  """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("C008"))
                .andExpect(jsonPath("$.detail").value("command failed"));
    }

    @Test
    void addSymptoms_shouldRejectMissingName() throws Exception {
        mockMvc.perform(post("/symptoms")
                        .contentType("application/json")
                        .content("""
                                {
                                  "origin": ["A", "C"],
                                  "numberOfOccurance": 2
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("C003"))
                .andExpect(jsonPath("$.key").value("invalid.request.body"))
                .andExpect(jsonPath("$.detail").value("$: name is required"));
    }

    @Test
    void addSymptoms_shouldRejectWrongType() throws Exception {
        mockMvc.perform(post("/symptoms")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "High memory usage",
                                  "origin": "five",
                                  "numberOfOccurance": 2
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("C003"))
                .andExpect(jsonPath("$.detail").value("$.origin: must be of type array"));
    }

    @Test
    void addSymptoms_shouldCumulateOriginNamesIntoFlagMask() throws Exception {
        when(commandGateway.sendAndWait(any())).thenReturn(789L);

        mockMvc.perform(post("/symptoms")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "High memory usage",
                                  "origin": ["A", "B", "D"],
                                  "numberOfOccurance": 2
                                }
                                """))
                .andExpect(status().isOk());

        ArgumentCaptor<CreateSymptomCommand> captor = ArgumentCaptor.forClass(CreateSymptomCommand.class);
        verify(commandGateway).sendAndWait(captor.capture());
        assertEquals(11L, captor.getValue().origin());
    }

    @Test
    void addSymptoms_shouldRejectUnknownOriginName() throws Exception {
        mockMvc.perform(post("/symptoms")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "High memory usage",
                                  "origin": ["A", "Z"],
                                  "numberOfOccurance": 2
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("C003"))
                .andExpect(jsonPath("$.detail").value("$.origin[]: must be one of [\"A\",\"B\",\"C\",\"D\",\"E\"]"));
    }

    @Test
    void addSymptoms_shouldRejectAdditionalProperty() throws Exception {
        mockMvc.perform(post("/symptoms")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "High memory usage",
                                  "origin": ["A", "C"],
                                  "numberOfOccurance": 2,
                                  "extra": true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("C003"))
                .andExpect(jsonPath("$.detail").value("$.extra: additional property not allowed"));
    }

    @Test
    void addSymptoms_shouldRejectMalformedId() throws Exception {
        mockMvc.perform(post("/symptoms")
                        .contentType("application/json")
                        .content("""
                                {
                                  "id": "not-a-uuid",
                                  "name": "High memory usage",
                                  "origin": ["A", "C"],
                                  "numberOfOccurance": 2
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("C003"));
    }
}
