package com.example.authentication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class LogIn : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.log_in)

        val logBtn = findViewById<Button>(R.id.log_in_btn)
        val editPassword = findViewById<EditText>(R.id.editPassword)
        val editEmail = findViewById<EditText>(R.id.editEmail)

        logBtn.setOnClickListener {
            val password = editPassword.text.toString()
            val email = editEmail.text.toString()

            lifecycleScope.launch {
                try {
                    val loginResponse = sendPostRequest(email, password)
                    loginResponse?.let {
//                        saveCredentials(this@LogIn, it.id, it.userName, email, password)
                        saveToken(it.token)
                        Toast.makeText(this@LogIn, "Авторизация успешна", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@LogIn, HomePage::class.java)
                        startActivity(intent)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this@LogIn, "Неправильный email или пароль", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun sendPostRequest(email: String, password: String): LoginResponse? {
        val client = OkHttpClient()

        val json = """
            {
                "email": "$email",
                "password": "$password"
            }
        """.trimIndent()

        val requestBody = json.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("http://192.168.0.13:8080/users/login")
            .post(requestBody)
            .build()

        return withContext(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IOException("Unexpected code $response")
                    }

                    val responseBody = response.body?.string()
                    responseBody?.let {
                        val gson = Gson()
                        return@withContext gson.fromJson(it, LoginResponse::class.java)
                    }
                    throw IOException("Empty response body")
                }
            } catch (e: IOException) {
                throw e
            }
        }
    }


    private fun readCredentials(context: Context): String? {
        val file = File(context.filesDir, "user_credentials.txt")
        if (file.exists()) {
            return file.readText()
        }
        return null
    }

    private fun saveToken(token: String) {
        val sharedPreferences = getSharedPreferences("my_app_pref", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("token", token)
        editor.apply()
    }
}

data class LoginResponse(
    val token: String,
    val id: String,
    val userName: String
)

