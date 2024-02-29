package org.cardanofoundation.explorer.rewards.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.explorer.common.entity.ledgersync.Epoch;
import org.cardanofoundation.explorer.rewards.concurrent.EpochConcurrentFetching;

@ExtendWith(SpringExtension.class)
@WebMvcTest(EpochController.class)
class EpochControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private EpochConcurrentFetching epochConcurrentFetching;

  @Test
  void fetchEpochsSuccess_ReturnsTrue() throws Exception {
    List<Integer> epochs = List.of(314, 315);
    when(epochConcurrentFetching.fetchDataConcurrently(epochs))
        .thenReturn(List.of(Epoch.builder().no(314).build(), Epoch.builder().no(315).build()));

    mockMvc
        .perform(
            post("/api/v1/epochs/fetch")
                .content(asJsonString(epochs))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(
            content()
                .json(
                    asJsonString(
                        List.of(
                            Epoch.builder().no(314).build(), Epoch.builder().no(315).build()))));
  }

  public static String asJsonString(final Object obj) {
    try {
      return new ObjectMapper().writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
