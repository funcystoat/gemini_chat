package com.ai.gemini_chat;

import java.util.regex.*;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

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

		String response = webClient.post().uri(geminiApiUrl + geminiApiKey).header("Content-Type", "application/json")
				.bodyValue(requestBody).retrieve().bodyToMono(String.class).block();

		return response;
	}

	public JsonObject getSpecialists(String smes, String subject) {
		String question = "Give me a list of " + smes + " who made significant contributions to " + subject + ". "
				+ "Please provide as many results as possible, with NO duplicates. "
				+ "Please format your response as as an array of objects with the following structure: "
				+ "{full_name: full_name, summary_of_accomplishments: summary_of_accomplishments}";

		Map<String, Object> requestBody = Map.of("contents",
				new Object[] { Map.of("parts", new Object[] { Map.of("text", question) }) });

		String firstResponse = webClient.post().uri(geminiApiUrl + geminiApiKey)
				.header("Content-Type", "application/json").bodyValue(requestBody).retrieve().bodyToMono(String.class)
				.block();

		Pattern pattern = Pattern.compile(
				"(\\\\\"full_name\\\\\": \\\\\"[^\\\\]+\\\\\")|(\\\\\"summary_of_accomplishments\\\\\": \\\\\"[^\\\\]+\\\\\")");
		Matcher matcher = pattern.matcher(firstResponse);

		// Track number of responses
		long groupCount = 0;

		// Set up String to represent the JSON result
		String response = "{results: [";

		// These will always be the initial format of each entry
		String full_name_formatting = "\"full_name\":  \"";
		String summary_of_accomplishments_formatting = "\"summary_of_accomplishments\":  \"";

		// While we have more results, keep formatting them
		while (matcher.find()) {
			// If this is not the first result, we need to add a trailing comma to the
			// previous result
			if (groupCount > 0)
				response += ",";

			groupCount++;

			int start = matcher.start();
			int end = matcher.end();
			String full_name = firstResponse.substring(start + full_name_formatting.length() + 2, end - 2);

			// Move to the next match, the summary_of_accomplishments
			matcher.find();
			start = matcher.start();
			end = matcher.end();

			String summary_of_accomplishments = firstResponse
					.substring(start + summary_of_accomplishments_formatting.length() + 2, end - 2);

			response += "{" + full_name_formatting + full_name + "\", " + summary_of_accomplishments_formatting
					+ summary_of_accomplishments + "\"}";
		}
		response += "], \"results_count\": " + groupCount + "}";

		return (new Gson()).fromJson(response, JsonObject.class);
	}
}
