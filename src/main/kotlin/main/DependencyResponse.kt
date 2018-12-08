package main

import com.fasterxml.jackson.annotation.JsonProperty

data class Response(
    val files: Files
)

data class DependencyFile(
    @JsonProperty("filename") val filename: String,
    @JsonProperty("content") val content: String
)

data class Files(
    @JsonProperty("dependencies.json") val dependencyFile: DependencyFile
)

data class DependenciesItem(
    @JsonProperty("path") val path: String,
    @JsonProperty("repo") val repo: String,
    @JsonProperty("tracking_type") val trackingType: String
) {
    val repoOwner = repo.substringBefore("/")
    val repoName = repo.substringAfter("/")
}

data class Dependencies(
    @JsonProperty("dependencies") val dependencies: List<DependenciesItem?>? = null
)

enum class ReleaseType { TAG, RELEASE;
    companion object {
        fun fromCode(code: String): ReleaseType = values().find { it.name == code.toUpperCase() } ?: TAG
    }
}



