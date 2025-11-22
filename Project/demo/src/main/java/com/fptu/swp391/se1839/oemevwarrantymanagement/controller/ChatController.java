package com.fptu.swp391.se1839.oemevwarrantymanagement.controller;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

  @Value("${CHAT_GEMINI_API_KEY}")
  String API_KEY;

  @PostMapping
  public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> payload) {
    String userMessage = payload.get("message");
    String reply = callGeminiAPI(userMessage);
    return ResponseEntity.ok(Map.of("reply", reply));
  }

  private String callGeminiAPI(String userMessage) {
    try {
      String context = """
          You are an assistant that helps users understand and use the OEM E-Vehicle Warranty Management System.
          You must ONLY answer questions about:
          - Creating or managing warranty claims
          - Adding or removing parts in a claim or repair order
          - Uploading images to Cloudinary
          - Viewing or verifying repair orders
          - Roles: EVM Staff, SC Staff, Technician
          - The workflow of claim approval, verification, and completion

          If the user asks something outside this topic, reply:
          Xin lỗi, tôi chỉ hỗ trợ các thao tác trong hệ thống quản lý bảo hành xe (EVM Warranty Management).

          Respond in Vietnamese.
          """;

      // ✅ Escape dấu " để JSON không bị vỡ
      String finalPrompt = (context + "\nUser: " + userMessage).replace("\"", "\\\"");

      String apiUrl = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key="
          + API_KEY;

      String body = """
          {
            "contents": [
              {
                "parts": [
                  { "text": "%s" }
                ]
              }
            ]
          }
          """.formatted(finalPrompt);

      HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
      conn.setDoOutput(true);

      try (OutputStream os = conn.getOutputStream()) {
        os.write(body.getBytes(StandardCharsets.UTF_8));
      }

      int status = conn.getResponseCode();
      InputStream is = (status >= 200 && status < 300)
          ? conn.getInputStream()
          : conn.getErrorStream();

      try (is) { // ✅ dùng luôn biến is ở trên
        String response = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        System.out.println("\n🔍 Gemini raw response:\n" + response); // Log response

        JsonObject json = JsonParser.parseString(response).getAsJsonObject();

        // Nếu trả lỗi từ API, in ra luôn
        if (json.has("error")) {
          return "❌ Gemini API trả lỗi: " + json.get("error").toString();
        }

        return json.getAsJsonArray("candidates")
            .get(0).getAsJsonObject()
            .get("content").getAsJsonObject()
            .getAsJsonArray("parts")
            .get(0).getAsJsonObject()
            .get("text").getAsString();
      }

    } catch (Exception e) {
      e.printStackTrace();
      return "❌ Lỗi kết nối tới Gemini. Kiểm tra API key hoặc log server.";
    }
  }

}