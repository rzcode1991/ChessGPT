package com.example.chessgpt


import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONException
import java.io.IOException
import java.net.SocketTimeoutException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatGPTAIPlayer(private val apiKey: String = Constants.API) {
    private val httpClient = OkHttpClient()

    suspend fun suggestMove(boardState: String): String? {
        return withContext(Dispatchers.IO) {
            var suggestedMove: String? = null
            var retryCount = 0
            val maxRetries = 3 // Set the maximum number of retries

            while (suggestedMove == null && retryCount < maxRetries) {
                try {
                    val url = "https://api.openai.com/v1/chat/completions"
                    val requestBody = JSONObject()
                        .put("model", "gpt-3.5-turbo")
                        .put(
                            "messages", JSONArray().put(
                                JSONObject().put("role", "system").put("content", "Assistant")
                            ).put(
                                JSONObject().put("role", "user").put("content", "I will provide you the current state of a chess game where I am playing white and you are playing black. Analyze the game state and respond with a one line comma-separated message containing: \n" +
                                        "\n" +
                                        "1. Your suggested move for black in algebraic chess notation (e.g. Nf6)\n" +
                                        "\n" +
                                        "2. The source square for that move (e.g. g8)\n" +
                                        "\n" +
                                        "3. The destination square for that move (e.g. f6)\n" +
                                        "\n" +
                                        "The format should be: \n" +
                                        "\n" +
                                        "Move,Source Square,Destination Square\n" +
                                        "\n" +
                                        "Do not include any additional text or explanations. Only provide the move, source square, and destination square separated by commas on one line. Here is the current game state: $boardState")
                            )
                        )

                    val mediaType = "application/json".toMediaType()
                    val request = Request.Builder()
                        .url(url)
                        .header("Authorization", "Bearer $apiKey")
                        .post(RequestBody.create(mediaType, requestBody.toString()))
                        .build()

                    val response = httpClient.newCall(request).execute()
                    val responseBody = response.body?.string()

                    if (response.isSuccessful && responseBody != null) {
                        val responseJson = JSONObject(responseBody)
                        val choices = responseJson.getJSONArray("choices")
                        if (choices.length() > 0) {
                            suggestedMove = choices.getJSONObject(0).getJSONObject("message").getString("content").trim()
                        }
                    } else {
                        Log.e("AI move request failed", "${response.code} ${response.message}")
                    }
                } catch (e: JSONException) {
                    Log.e("AI move request failed", "JSON parsing error: ${e.message}")
                } catch (e: SocketTimeoutException) {
                    Log.e("AI move request failed", "Socket timeout error: ${e.message}")
                } catch (e: IOException) {
                    Log.e("AI move request failed", "IO error: ${e.message}")
                } catch (e: Exception) {
                    Log.e("AI move request failed", "Unknown error: ${Log.getStackTraceString(e)}")
                }

                retryCount++
            }

            suggestedMove
        }
    }


}
