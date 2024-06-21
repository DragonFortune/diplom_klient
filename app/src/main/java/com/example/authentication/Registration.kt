package com.example.authentication

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

class Registration : AppCompatActivity() {

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_IMAGE_PICK = 2
    }

    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registration)

        val editName = findViewById<EditText>(R.id.editName)
        val editPassword = findViewById<EditText>(R.id.editPassword)
        val editEmail = findViewById<EditText>(R.id.editEmail)
        val regBtn = findViewById<Button>(R.id.log_in_btn)
        imageView = findViewById(R.id.imageView)

        imageView.setOnClickListener {
            showImagePickerDialog()
        }

        editPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()

                if (validatePassword(password)) {
                    editPassword.setTextColor(Color.BLACK)
                } else {
                    editPassword.setTextColor(Color.RED)
                }
            }
        })

        regBtn.setOnClickListener {
            val name = editName.text.toString()
            val password = editPassword.text.toString()
            val email = editEmail.text.toString()

            if (name.isBlank() || password.isBlank() || email.isBlank()) {
                Toast.makeText(this, "Все поля должны быть заполнены", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Введите корректный email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val id = sendPostRequest(name, email, password)

                    Toast.makeText(this@Registration, "Регистрация успешна", Toast.LENGTH_SHORT).show()
                    saveAvatarToInternalStorage(imageView.drawable.toBitmap(), "avatar_image.jpg")
                    saveCredentials(this@Registration, id.toString(), name, email, password)

                    val intent = Intent(this@Registration, LogIn::class.java)
                    startActivity(intent)
                    finish()
                } catch (e: IOException) {
                    e.printStackTrace()

                    Toast.makeText(this@Registration, "Произошла ошибка, повторите попытку", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun sendPostRequest(username: String, email: String, password: String): Int {
        val client = OkHttpClient()

        val json = """
        {
            "user_name": "$username",
            "email": "$email",
            "password": "$password"
        }
    """.trimIndent()

        val requestBody = json.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("http://192.168.0.13:8080/users/reg")
            .post(requestBody)
            .build()

        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    throw IOException("Unexpected code ${response.code}")
                }

                val responseBody = response.body?.string()
                println(responseBody)

                if (responseBody.isNullOrEmpty()) {
                    throw IOException("Empty response body")
                }

                val userId = try {
                    responseBody.toInt()
                } catch (e: NumberFormatException) {
                    throw IOException("Error parsing ID as integer", e)
                }

                userId
            } catch (e: IOException) {

                throw e
            }
        }
    }


    fun validatePassword(password: String): Boolean {
        val minLength = 8
        val containsDigit = password.any { it.isDigit() }
        val containsUpperCase = password.any { it.isUpperCase() }
        val containsLowerCase = password.any { it.isLowerCase() }
        val containsSpecialChar = password.any { it !in '0'..'9' && it !in 'a'..'z' && it !in 'A'..'Z' }

        return password.length >= minLength && containsDigit && containsUpperCase && containsLowerCase && containsSpecialChar
    }

    private fun showImagePickerDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_image_picker, null)
        val builder = AlertDialog.Builder(this, R.style.TransparentAlertDialog)
            .setView(dialogView)
        val alertDialog = builder.create()
        dialogView.findViewById<Button>(R.id.btn_take_photo).setOnClickListener {
            alertDialog.dismiss()
            dispatchTakePictureIntent()
        }
        dialogView.findViewById<Button>(R.id.btn_pick_from_gallery).setOnClickListener {
            alertDialog.dismiss()
            dispatchPickImageIntent()
        }
        alertDialog.show()
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    private fun dispatchPickImageIntent() {
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).also { pickPhotoIntent ->
            pickPhotoIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    setImageWithCircleCrop(imageBitmap)
                }
                REQUEST_IMAGE_PICK -> {
                    data?.data?.let { uri ->
                        val imageBitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
                        setImageWithCircleCrop(imageBitmap)
                    }
                }
            }
        }
    }

    private fun setImageWithCircleCrop(bitmap: Bitmap) {
        Glide.with(this)
            .load(bitmap)
            .apply(RequestOptions().transform(CircleCrop()))
            .into(imageView)
    }

    private fun saveAvatarToInternalStorage(bitmap: Bitmap, fileName: String) {
        try {
            openFileOutput(fileName, Context.MODE_PRIVATE).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveCredentials(context: Context, id: String, username: String, email: String, password: String) {
        val file = File(context.filesDir, "user_credentials.txt")
        try {
            FileOutputStream(file).use { outputStream ->
                val data = "$id,$username,$email,$password"
                outputStream.write(data.toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

