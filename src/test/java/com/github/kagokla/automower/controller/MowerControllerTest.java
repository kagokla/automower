package com.github.kagokla.automower.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.github.kagokla.automower.model.dto.CommandRequestDTO;
import com.github.kagokla.automower.model.dto.CommandResponseDTO;
import com.github.kagokla.automower.service.MowingService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MowerController.class)
class MowerControllerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MowingService mowingService;

    @BeforeAll
    static void setUp() {
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    @Test
    void shouldReturn200OkWhenRequestIsValid() throws Exception {
        // Given
        final var area = "55";
        final var initialPosition = "12N";
        final var instructions = "LFLFLFLFF";
        final var commandRequest = buildCommandRequestDTO(area, initialPosition, instructions);
        final var commandResponse = buildCommandResponseDTO(area, initialPosition, instructions, "13N");

        Mockito.when(mowingService.processCommand(any())).thenReturn(commandResponse);

        // When
        this.mockMvc
                .perform(post("/auto-mowers")
                        .content(objectMapper.writeValueAsString(commandRequest))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    void shouldReturn400BadRequestWhenRequestIsInvalid() throws Exception {
        // Given
        final var area = "area";
        final var initialPosition = "33E";
        final var instructions = "FFRFFRFRRF";
        final var commandRequest = buildCommandRequestDTO(area, initialPosition, instructions);
        final var commandResponse = buildCommandResponseDTO(area, initialPosition, instructions, "51E");

        Mockito.when(mowingService.processCommand(any())).thenReturn(commandResponse);

        // Then
        this.mockMvc
                .perform(post("/auto-mowers")
                        .content(objectMapper.writeValueAsString(commandRequest))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest());
    }

    private CommandRequestDTO buildCommandRequestDTO(
            final String area, final String initialPosition, final String instructions) {
        final var mower = new CommandRequestDTO.Mower();
        mower.setInitialPosition(initialPosition);
        mower.setInstructions(instructions);
        final var commandRequest = new CommandRequestDTO();
        commandRequest.setArea(area);
        commandRequest.setMowers(List.of(mower));

        return commandRequest;
    }

    private CommandResponseDTO buildCommandResponseDTO(
            final String area, final String initialPosition, final String instructions, final String finalPosition) {
        final var mower = new CommandResponseDTO.Mower();
        mower.setInitialPosition(initialPosition);
        mower.setInstructions(instructions);
        mower.setFinalPosition(finalPosition);
        final var commandResponse = new CommandResponseDTO();
        commandResponse.setArea(area);
        commandResponse.setMowers(List.of(mower));

        return commandResponse;
    }
}