package org.cardanofoundation.explorer.rewards.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cardanofoundation.explorer.rewards.concurrent.EpochStakeConcurrentFetching;
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
@WebMvcTest(EpochStakeController.class)
class EpochStakeControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private EpochStakeConcurrentFetching mockEpochStakeConcurrentFetching;

  @Test
  void fetchEpochStakes_Success_ReturnsTrue() throws Exception {
    List<String> stakeAddressList = List.of(
        "stake1uyrx65wjqjgeeksd8hptmcgl5jfyrqkfq0xe8xlp367kphsckq250",
        "stake1uxpdrerp9wrxunfh6ukyv5267j70fzxgw0fr3z8zeac5vyqhf9jhy");
    when(mockEpochStakeConcurrentFetching.fetchDataConcurrently(stakeAddressList))
        .thenReturn(true);
    mockMvc.perform(post("/api/v1/epoch-stake/fetch")
            .content(asJsonString(stakeAddressList))
            .contentType(MediaType.APPLICATION_JSON)
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
