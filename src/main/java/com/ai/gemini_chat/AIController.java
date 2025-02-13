package com.ai.gemini_chat;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/qna")
public class AIController {
	private static final Gson gson = new Gson();

	private final QnAService qnaService;

	@PostMapping("/ask")
	public ResponseEntity<String> askQuestion(@RequestBody Map<String, String> payload) {
		String question = payload.get("question");
		String answer = qnaService.getAnswer(question);
		return ResponseEntity.ok(answer);
	};

	@GetMapping(value = "/algebraists", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getAlgebraists() {
		JsonObject answer = qnaService.getSpecialists("mathematicians", "abstract algebra");
		return ResponseEntity.ok(gson.toJson(answer));
	};
	
	@GetMapping(value = "/analysts", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getAnalysts() {
		JsonObject answer = qnaService.getSpecialists("mathematicians", "analysis");
		return ResponseEntity.ok(gson.toJson(answer));
	};
}
