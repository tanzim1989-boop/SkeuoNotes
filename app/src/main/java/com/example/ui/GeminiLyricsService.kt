package com.example.ui

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiLyricsService {
    private const val TAG = "GeminiLyricsService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Generates lyrics for a song based on its title and artist using Gemini.
     */
    suspend fun generateLyrics(title: String, artist: String): String? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API Key is missing or default placeholder")
            return@withContext null
        }

        val prompt = """
            You are a helpful assistant specialized in music and song lyrics.
            Please write or find the lyrics for the song: "$title" by "$artist".
            Since this might be an instrumental or custom song, if you don't know the exact real lyrics, feel free to creatively write poetic, beautifully fitting lyrics that match the title and artist.
            
            IMPORTANT:
            1. Return ONLY the plain text lyrics.
            2. Format them with section titles like [Verse 1], [Chorus], [Verse 2], etc.
            3. Do not include any introductory text, concluding notes, or friendly conversation. Just return the lyrics directly.
            4. If it's a completely instrumental song, write descriptive acoustic atmosphere cues, for example: [00:00] (Opening Chimes), [01:00] (Synth Build-up), etc., so the user can enjoy it as a visual sync.
        """.trimIndent()

        try {
            // Build Request JSON using org.json
            val requestBodyJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
            }

            val requestBody = requestBodyJson.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaType())

            val url = "$BASE_URL?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Request failed with code: ${response.code}")
                    return@withContext null
                }

                val responseBodyString = response.body?.string() ?: return@withContext null
                val jsonResponse = JSONObject(responseBodyString)
                val text = jsonResponse.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")

                return@withContext text.trim()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating lyrics", e)
            return@withContext null
        }
    }
}
