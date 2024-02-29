package org.cardanofoundation.explorer.rewards.controller;

import static org.hamcrest.core.StringContains.containsString;
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

import org.cardanofoundation.explorer.rewards.concurrent.RewardConcurrentFetching;

@ExtendWith(SpringExtension.class)
@WebMvcTest(RewardController.class)
class RewardControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private RewardConcurrentFetching mockRewardConcurrentFetching;

  @Test
  void fetchRewards_Success_ReturnsTrue() throws Exception {
    List<String> stakeAddressList =
        List.of(
            "stake1uyrx65wjqjgeeksd8hptmcgl5jfyrqkfq0xe8xlp367kphsckq250",
            "stake1uxpdrerp9wrxunfh6ukyv5267j70fzxgw0fr3z8zeac5vyqhf9jhy");
    when(mockRewardConcurrentFetching.fetchDataConcurrently(stakeAddressList)).thenReturn(true);
    mockMvc
        .perform(
            post("/api/v1/rewards/fetch")
                .content(asJsonString(stakeAddressList))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("true")));
  }

  public static String asJsonString(final Object obj) {
    try {
      return new ObjectMapper().writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
