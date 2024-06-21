package com.example.authentication

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.io.FileWriter

class AddTask : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var taskInput: EditText
    private lateinit var categoryButton: Button
    private lateinit var saveButton: Button
    private lateinit var addSubTaskButton: ImageButton
    private lateinit var subTasksContainer: LinearLayout
    private var selectedDate: String = ""
    private var subTasks = mutableListOf<SubTask>()
    private var subTaskCounter = 1
    private var categories = arrayOf("Работа", "Личное", "Учеба", "Здоровье", "Прочее") // категорий

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        calendarView = findViewById(R.id.calendarView)
        taskInput = findViewById(R.id.multiAutoCompleteTextView)
        categoryButton = findViewById(R.id.category_btn)
        saveButton = findViewById(R.id.button2)
        addSubTaskButton = findViewById(R.id.subtask)
        subTasksContainer = findViewById(R.id.subTasksContainer)

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            selectedDate = format.format(calendar.time)
        }

        // Настройка диалога выбора категории
        categoryButton.setOnClickListener {
            showCategoryDialog()
        }

        addSubTaskButton.setOnClickListener {
            addSubTaskView()
        }

        saveButton.setOnClickListener {
            saveTask()
        }
    }

    private fun showCategoryDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Выберите категорию")

        val adapter = CategoryAdapter(this, R.layout.category_item, categories)
        builder.setAdapter(adapter) { dialog, which ->
            categoryButton.text = categories[which]
            dialog.dismiss()
        }

        builder.setNegativeButton("Отмена") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }



    private fun addSubTaskView() {
        val subTaskView = LayoutInflater.from(this).inflate(R.layout.sub_task_item, subTasksContainer, false)
        val subTaskInput: EditText = subTaskView.findViewById(R.id.subTaskInput)

        subTasksContainer.addView(subTaskView)
        subTasks.add(SubTask(0, subTaskCounter++, subTaskInput.text.toString(), false))
    }

    private fun saveTask() {
        val taskName = taskInput.text.toString()
        val category = categoryButton.text.toString()

        if (taskName.isEmpty() || selectedDate.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        val task = Task(
            taskId = 0,
            userId = 1,
            taskName = taskName,
            category = category,
            date = selectedDate,
            completed = false
        )

        if (saveTaskToFile(task)) {
            saveSubTasksToFile(task.taskId)
            Toast.makeText(this, "Задача сохранена", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Ошибка сохранения задачи", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveTaskToFile(task: Task): Boolean {
        val taskFile = File(filesDir, "tasks.txt")
        return try {
            val writer = FileWriter(taskFile, true)
            writer.append("${task.taskId},${task.userId},${task.taskName},${task.category},${task.date},${task.priority},${task.completed}\n")
            writer.close()
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun saveSubTasksToFile(taskId: Int) {
        val subTaskFile = File(filesDir, "subtasks.txt")
        try {
            val writer = FileWriter(subTaskFile, true)
            subTasks.forEach { subTask ->
                writer.append("$taskId,${subTask.subTaskId},${subTask.subTaskName},${subTask.completed}\n")
            }
            writer.close()
        } catch (e: Exception) {
            // Обработка исключений
        }
    }
}

data class Task(
    val taskId: Int,
    val userId: Int,
    val taskName: String,
    val category: String = "",
    val date: String,
    val priority: Int = 1,
    val completed: Boolean
)

data class SubTask(
    val taskId: Int,
    val subTaskId: Int,
    val subTaskName: String,
    var completed: Boolean
)
class CategoryAdapter(context: Context, private val resource: Int, private val categories: Array<String>)
    : ArrayAdapter<String>(context, resource, categories) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(resource, parent, false)
        }

        val categoryTextView: TextView = view!!.findViewById(R.id.categoryName)
        categoryTextView.text = categories[position]

        return view
    }
}

