package com.example.searchgithub.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ResponseUsers(

    @SerializedName("total_count")
    @Expose
    val totalCount: Int?,

    @SerializedName("incomplete_results")
    @Expose
    val incompleteResults: Boolean?,

    @SerializedName("items")
    @Expose
    val items: List<Item>
)