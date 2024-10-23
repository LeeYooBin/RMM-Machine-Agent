package com.agent.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.agent.service.WebSocketService;

@Component
public class WebSocketRunner implements CommandLineRunner {
  @Autowired
  private WebSocketService webSocketService;

  @Override
  public void run(String... args) throws Exception {
    webSocketService.connect();
  }
}
