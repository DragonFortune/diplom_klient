package com.example.authentication.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toolbar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.authentication.ChangePasswordActivity
import com.example.authentication.ChangeUsername
import com.example.authentication.MainActivity
import com.example.authentication.R
import java.io.File

class AccountFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)
        val menu: androidx.appcompat.widget.Toolbar = view.findViewById(R.id.toolbar)

        menu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_logout -> {
                    removeToken()
                    navigateToMainPage()
                    true
                }
                else -> false
            }
        }

        val usernameTextView: TextView = view.findViewById(R.id.name)
        val emailEditText: EditText = view.findViewById(R.id.emailEditText)
        val passwordEditText: EditText = view.findViewById(R.id.passwordEditText)
        val avatarImageView: ImageView = view.findViewById(R.id.avatar_account)
        val allTasksTextView: TextView = view.findViewById(R.id.allTask)
        val completedTasksTextView: TextView = view.findViewById(R.id.completedTask)

        emailEditText.setOnClickListener {
            // Запускаем активность для изменения имени пользователя
            startActivity(Intent(requireContext(), ChangeUsername::class.java))
        }

        passwordEditText.setOnClickListener {
            // Запускаем активность для изменения пароля
            startActivity(Intent(requireContext(), ChangePasswordActivity::class.java))
        }

        val userFile = File(requireContext().filesDir, "user_credentials.txt")
        if (userFile.exists()) {
            val userCredentials = userFile.readText().split(",")
            Log.d("AccountFragment", "User credentials read: $userCredentials")
            if (userCredentials.size == 5) {
                usernameTextView.text = userCredentials[1] // userName
                emailEditText.setText(userCredentials[2]) // email
                passwordEditText.setText(userCredentials[3]) // password
            } else {
                Log.e("AccountFragment", "Invalid user credentials format: $userCredentials")
            }
        } else {
            Log.e("AccountFragment", "User credentials file not found")
        }

        val avatarFile = File(requireContext().filesDir, "avatar_image.jpg")
        if (avatarFile.exists()) {
            val bitmap = BitmapFactory.decodeFile(avatarFile.absolutePath)
            setImageWithCircleCrop(bitmap, avatarImageView)
        } else {
            Log.e("AccountFragment", "Avatar image file not found")
        }

        // Загрузка задач и подсчет количества задач и выполненных задач
        val tasks = loadTasksFromFile()
        if (tasks != null) {
            val allTasksCount = tasks.size
            val completedTasksCount = tasks.count { it.completed }

            // Отображение количества задач и выполненных задач
            allTasksTextView.text = "$allTasksCount"
            completedTasksTextView.text = "$completedTasksCount"
        } else {
            Log.e("AccountFragment", "No tasks found or error reading the file.")
        }

        return view
    }

    private fun loadTasksFromFile(): MutableList<Task>? {
        val taskFile = File(requireContext().filesDir, "tasks.txt")
        if (!taskFile.exists()) {
            Log.e("AccountFragment", "Task file does not exist.")
            return null
        }

        val tasks = mutableListOf<Task>()
        try {
            taskFile.forEachLine { line ->
                val parts = line.split(",")
                if (parts.size == 7) {
                    val task = Task(
                        taskId = parts[0].toInt(),
                        userId = parts[1].toInt(),
                        taskName = parts[2],
                        category = parts[3],
                        date = parts[4],
                        priority = parts[5].toInt(),
                        completed = parts[6].toBoolean()
                    )
                    tasks.add(task)
                } else {
                    Log.e("AccountFragment", "Invalid task format: $line")
                }
            }
        } catch (e: Exception) {
            Log.e("AccountFragment", "Error reading tasks from file.", e)
            return null
        }
        return tasks
    }

    private fun setImageWithCircleCrop(bitmap: Bitmap, imageView: ImageView) {
        Glide.with(this)
            .load(bitmap)
            .apply(RequestOptions().transform(CircleCrop()))
            .into(imageView)
    }

    private fun removeToken() {
        val sharedPreferences = requireContext().getSharedPreferences("my_app_pref", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("token")
        editor.apply()
    }

    private fun navigateToMainPage() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        requireActivity().finish()
    }



}

