package com.ai.gemini_chat;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class QnAService {
	@Value("${gemini.api.url}")
	private String geminiApiUrl;

	@Value("${gemini.api.key}")
	private String geminiApiKey;

	private final WebClient webClient;

	// The webflux dependency from springboot auto-injects via this.
	public QnAService(WebClient.Builder webClient) {
		this.webClient = webClient.build();
	}

	public String getAnswer(String question) {
		/*
		 * Gemini expects the data in the following format:
		 * 
		 * { "contents": [ { "parts": [ { "text": "Is this a question?" } ] } ] }
		 * 
		 * Hence, the ugly formatting of the requestBody variable here.
		 */
		Map<String, Object> requestBody = Map.of("contents",
				new Object[] { Map.of("parts", new Object[] { Map.of("text", question) }) });

		String response = webClient.post()
				.uri(geminiApiUrl + geminiApiKey)
				.header("Content-Type", "application/json")
				.bodyValue(requestBody)
				.retrieve()
				.bodyToMono(String.class)
				.block();

		// Return Response
		return response;
	}

}
