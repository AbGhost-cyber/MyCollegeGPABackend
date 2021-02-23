package com.crushtech.cgpa.data.collections


data class OneSignalConfig(
    val app_id: String,
    val contents: Contents,
    val headings: Headings,
    val channel_for_external_user_ids: String,
    val include_external_user_ids: List<String>
) {
    data class Contents(val en: String)
    data class Headings(val en: String)
}