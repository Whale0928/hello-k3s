package com.example.skk3s

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthController {
	@GetMapping("/ping")
	fun ping(): String {
		return "pong"
	}

	@GetMapping("/health")
	fun health(): String {
		return "ok"
	}
}
