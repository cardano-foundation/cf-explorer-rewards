package org.cardanofoundation.explorer.rewards.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cardanofoundation.explorer.rewards.concurrent.AdaPostsConcurrentFetching;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AdaPotsController.class)
class AdaPotsControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private AdaPostsConcurrentFetching adaPostsConcurrentFetching;

  @Test
  void fetchAdaPots_Success_ReturnsTrue() throws Exception {
    List<Integer> epochs = List.of(314, 315);
    when(adaPostsConcurrentFetching.fetchDataConcurrently(epochs)).thenReturn(true);

    mockMvc.perform(post("/api/v1/ada-pots/fetch")
            .content(asJsonString(epochs)).contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().string(
            containsString("true")));
  }

  public static String asJsonString(final Object obj) {
    try {
      return new ObjectMapper().writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
