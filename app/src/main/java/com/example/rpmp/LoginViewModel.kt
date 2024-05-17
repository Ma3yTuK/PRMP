package com.example.rpmp

import android.util.JsonReader
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.json.JSONObject
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.log


const val REGISTER_URL = "http://192.168.0.105:5000/auth/register"
const val LOGIN_URL = "http://192.168.0.105:5000/auth/login"

data class LoginState (
    val isLoggedIn: Boolean = false
)

class LoginViewModel : ViewModel() {
    var loginState by mutableStateOf(LoginState())
        private set

    fun signUp(username: String, password: String) {
        var ex: Exception? = null
        val thread = Thread {
            var urlConnection: HttpURLConnection? = null
            try {
                val postData = JSONObject()
                postData.accumulate("username", username)
                postData.accumulate("password", password)
                val url = URL(REGISTER_URL)
                urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.connectTimeout = 1000
                urlConnection.setRequestProperty("Content-Type", "application/json")
                urlConnection.setRequestMethod("POST")
                urlConnection.setDoOutput(true)
                urlConnection.setDoInput(true)
                urlConnection.setChunkedStreamingMode(0)
                val out: OutputStream = BufferedOutputStream(urlConnection.outputStream)
                val writer = BufferedWriter(
                    OutputStreamWriter(
                        out, "UTF-8"
                    )
                )
                writer.write(postData.toString())
                writer.flush()
                val code: Int = urlConnection.getResponseCode()
                if (code != 201) {
                    throw IOException("Invalid response from server: $code")
                }

                val rd = BufferedReader(
                    InputStreamReader(
                        urlConnection.inputStream
                    )
                )
                //result = JsonReader(rd).nextString()
            } catch (e: Exception) {
                ex = e
            } finally {
                urlConnection?.disconnect()
            }
        }
        thread.start()
        thread.join()
        if (ex != null)
            throw ex!!
    }

    fun logIn(username: String, password: String) {
        var ex: Exception? = null
        val thread = Thread {
            var urlConnection: HttpURLConnection? = null
            try {
                val postData = JSONObject()
                postData.accumulate("username", username)
                postData.accumulate("password", password)
                val url = URL(LOGIN_URL)
                urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.connectTimeout = 1000
                urlConnection.setRequestProperty("Content-Type", "application/json")
                urlConnection.setRequestMethod("POST")
                urlConnection.setDoOutput(true)
                urlConnection.setDoInput(true)
                urlConnection.setChunkedStreamingMode(0)
                val out: OutputStream = BufferedOutputStream(urlConnection.outputStream)
                val writer = BufferedWriter(
                    OutputStreamWriter(
                        out, "UTF-8"
                    )
                )
                writer.write(postData.toString())
                writer.flush()
                val code: Int = urlConnection.getResponseCode()
                if (code != 200) {
                    throw IOException("Invalid response from server: $code")
                }
                loginState = loginState.copy(isLoggedIn = true)
            } catch (e: Exception) {
                ex = e
            } finally {
                urlConnection?.disconnect()
            }
        }
        thread.start()
        thread.join()
        if (ex != null)
            throw ex!!
    }
}