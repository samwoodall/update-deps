package main

import com.fasterxml.jackson.annotation.JsonProperty

data class TagName(@JsonProperty("tag_name") val tagName: String? = null)