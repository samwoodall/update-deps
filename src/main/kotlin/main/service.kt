package main

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.*

val jacksonMapper = ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)!!
private var retrofit = Retrofit.Builder()
    .baseUrl("https://api.github.com/")
    .addConverterFactory(JacksonConverterFactory.create(jacksonMapper))
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .build()

var service: DependencyService = retrofit.create(DependencyService::class.java)

interface DependencyService {
    @GET("gists/{gist_id}")
    fun getDependenciesList(@Path("gist_id") gistId: String = Gists.DEP_GIST_ID, @Query("access_token") accessToken: String = Keys.ACCESS_TOKEN): Deferred<Response>

    @GET("repos/{owner}/{repo}/releases/latest")
    fun getLatestRelease(@Path("owner") owner: String, @Path("repo") repo: String): Deferred<TagName>

    @GET("repos/{owner}/{repo}/git/refs/tags")
    fun getLatestTags(@Path("owner") owner: String, @Path("repo") repo: String): Deferred<List<TagsResponse>>

    @PATCH("gists/{gist_id}")
    fun updateDependencies(@Path("gist_id") gistId: String = Gists.DEP_GIST_ID, @Query("access_token") accessToken: String = Keys.ACCESS_TOKEN, @Body updateBody: UpdateRequest): Deferred<retrofit2.Response<UpdateResponse>>
}

data class TagsResponse(val ref: String? = null)