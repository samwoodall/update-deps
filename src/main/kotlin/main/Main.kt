package main

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

class Main {
    fun handler() {
        runBlocking {
            val dependencyGist = service.getDependenciesList().await()
            val dependencyList = jacksonMapper.readValue<Dependencies>(
                dependencyGist.files.dependencyFile.content,
                Dependencies::class.java
            )
            val list = dependencyList.dependencies?.map {
                async {
                    UpdateContent(
                        it!!.repoOwner,
                        it.path,
                        getLatestRelease(it)
                    )
                }
            }

            val pathsToRelease = list!!.awaitAll()
            val groupsByOwner = pathsToRelease.groupBy { it.owner }
            val groupContent =
                groupsByOwner.map { "//${it.key}\n${it.value.fold("") { acc, updateContent -> "${acc}implementation ${updateContent.path}${updateContent.release}\n" }}" }

            val updateBody = UpdateRequest(
                RequestFiles(
                    DependenciesGradle(
                        "dependencies.gradle",
                        groupContent.reduce { acc, path -> "$acc \n$path" })
                )
            )
            service.updateDependencies(updateBody = updateBody).await()
        }
    }
}

data class UpdateContent(val owner: String, val path: String, val release: String)

suspend fun getLatestRelease(dependenciesItem: DependenciesItem): String {
    return when (ReleaseType.fromCode(dependenciesItem.trackingType)) {
        ReleaseType.TAG -> {
            val tagNames = service.getLatestTags(dependenciesItem.repoOwner, dependenciesItem.repoName).await()
            extractVersionFromTag(tagNames)
        }
        ReleaseType.RELEASE -> {
            val releaseName =
                service.getLatestRelease(dependenciesItem.repoOwner, dependenciesItem.repoName).await().tagName!!
            extractVersionFromReleaseString(releaseName)
        }
    }
}

fun extractVersionFromReleaseString(releaseName: String) = releaseName.fromFirstToLastNumber()

fun String.fromFirstToLastNumber() = substring(indexOfFirst { it.isDigit() }, indexOfLast { it.isDigit() } + 1)

fun extractVersionFromTag(tagNames: List<TagsResponse>): String {
    val filteredTagNames =
        tagNames.filter { !it.ref!!.contains("rc") || !it.ref.contains("beta") || !it.ref.contains("alpha") }
    val versionNumbers = filteredTagNames.map { it.ref!!.fromFirstToLastNumber() }

    return versionNumbers.maxBy { getOnlyDigits(it) }!!
}

private fun getOnlyDigits(it: String) = it.filter { char -> char.isDigit() }
