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

            try {
                val url = "https://api.openai.com/v1/chat/completions"
                val requestBody = JSONObject()
                    .put("model", "gpt-3.5-turbo")
                    .put(
                        "messages", JSONArray().put(
                            JSONObject().put("role", "system").put("content", "Let's play chess. I will play as white and you will play as black. At the start of my turn I will send the PGN for the entire game so far. For your turn, just send the move that you would play as black in this format \"... [your move here]\" where [your move here] indicates any valid move for black. no extra text or explanation.")
                        ).put(
                            JSONObject().put("role", "user").put("content", boardState)
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

            suggestedMove
        }
    }


}
