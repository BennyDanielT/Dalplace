package com.example.dalplace.utilities

import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.net.URLEncoder
import kotlin.concurrent.thread

/**
 * This is a singleton class with utilities for the push notification feature.
 */
object PushNotification {
    private fun String.utf8(): String = URLEncoder.encode(this, "UTF-8")

    /**
     * The [sendNotificationToAllUsers] function sends an in-app push notification
     * to all the users of the app
     */
    fun sendNotificationToAllUsers(notificationTitle: String, notificationMessage: String) {
        thread {
            val client = OkHttpClient()

            val mediaType = MediaType.parse("application/json")
            val body = RequestBody.create(
                mediaType,
                "{\"included_segments\":[\"Subscribed Users\"],\"headings\":{\"en\":\"$notificationTitle\"},\"contents\":{\"en\":\"$notificationMessage\"},\"name\":\"INTERNAL_CAMPAIGN_NAME\"}"
            )
            val bodyParams = mapOf("app_id" to "0e4ed0e5-0146-477f-9714-de644edd346b")
            val urlParams = bodyParams.map { (k, v) -> "${(k.utf8())}=${v.utf8()}" }
                .joinToString("&")

            val request = Request.Builder()
                .url("https://onesignal.com/api/v1/notifications?${urlParams}")
                .post(body)
                .addHeader("accept", "application/json")
                .addHeader(
                    "Authorization",
                    "Basic NzFkNTNlODUtNDMwOC00NjZiLWE2NzItNGIwZWYwNzhhZDNk"
                )
                .addHeader("content-type", "application/json")
                .build()

            val response = client.newCall(request).execute()
            print(response)
        }
    }
}