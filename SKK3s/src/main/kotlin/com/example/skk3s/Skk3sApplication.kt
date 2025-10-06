package com.example.skk3s

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class Skk3sApplication

fun main(args: Array<String>) {
	runApplication<Skk3sApplication>(*args)
}
