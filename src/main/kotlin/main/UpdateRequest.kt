package main

import com.fasterxml.jackson.annotation.JsonProperty

data class UpdateRequest(val files: RequestFiles)

data class DependenciesGradle(
	val filename: String?,
	val content: String
)

data class RequestFiles(
	@JsonProperty("dependencies.Gradle")val dependenciesGradle: DependenciesGradle
)
