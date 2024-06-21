package com.example.authentication.fragment

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.authentication.R
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CalendarFragment : Fragment() {

    private lateinit var tasksContainer: LinearLayout
    private lateinit var showAllTasksCheckBox: CheckBox

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        tasksContainer = view.findViewById(R.id.tasksContainer)
        showAllTasksCheckBox = view.findViewById(R.id.showAllTasksCheckBox)

        loadAndDisplayTasks()

        showAllTasksCheckBox.setOnCheckedChangeListener { _, _ ->
            loadAndDisplayTasks()
        }

        return view
    }

    private fun loadAndDisplayTasks() {
        tasksContainer.removeAllViews()
        val tasks = loadTasksFromFile()
        tasks?.let {
            for (task in it) {
                if (showAllTasksCheckBox.isChecked || !task.completed) {
                    addTaskView(task)
                }
            }
        } ?: Log.e("CalendarFragment", "No tasks found or error reading the file.")
    }

    private fun addTaskView(task: Task) {
        val remainingDays = calculateRemainingDays(task.date)

        val taskItemView = LayoutInflater.from(requireContext()).inflate(R.layout.task_item, tasksContainer, false)

        val taskText: TextView = taskItemView.findViewById(R.id.taskText)
        val categoryText: TextView = taskItemView.findViewById(R.id.category)
        val taskImage: ImageView = taskItemView.findViewById(R.id.taskImage)
        val timeText: TextView = taskItemView.findViewById(R.id.time)

        taskText.text = "${task.taskName}"
        timeText.text = "Осталось $remainingDays дней"
        categoryText.text = task.category

        setPriorityColor(task.priority, taskImage)

        taskImage.setOnClickListener {
            changeTaskPriority(task, taskImage)
        }

        taskItemView.setOnClickListener {
            showCompletionDialog(task)
        }

        tasksContainer.addView(taskItemView)
    }


    private fun setPriorityColor(priority: Int, imageView: ImageView) {
        val colorResId = when (priority) {
            1 -> R.color.low_priority_color
            2 -> R.color.medium_priority_color
            3 -> R.color.high_priority_color
            else -> R.color.low_priority_color
        }
        imageView.setColorFilter(ContextCompat.getColor(requireContext(), colorResId))
    }

    private fun calculateRemainingDays(taskDate: String): Int {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val taskCalendar = Calendar.getInstance().apply {
            time = dateFormat.parse(taskDate) ?: Date()
        }

        val currentCalendar = Calendar.getInstance()
        currentCalendar.set(Calendar.HOUR_OF_DAY, 0)
        currentCalendar.set(Calendar.MINUTE, 0)
        currentCalendar.set(Calendar.SECOND, 0)
        currentCalendar.set(Calendar.MILLISECOND, 0)

        val diffInMillis = taskCalendar.timeInMillis - currentCalendar.timeInMillis
        val millisecondsInDay = 24 * 60 * 60 * 1000
        return (diffInMillis / millisecondsInDay).toInt()
    }

    private fun loadTasksFromFile(): MutableList<Task>? {
        val taskFile = File(requireContext().filesDir, "tasks.txt")
        if (!taskFile.exists()) {
            Log.e("CalendarFragment", "Task file does not exist.")
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
                    Log.e("CalendarFragment", "Invalid task format: $line")
                }
            }
        } catch (e: Exception) {
            Log.e("CalendarFragment", "Error reading tasks from file.", e)
            return null
        }
        return tasks
    }

    private fun saveTasksToFile(tasks: List<Task>) {
        val taskFile = File(requireContext().filesDir, "tasks.txt")
        try {
            val writer = FileWriter(taskFile)
            tasks.forEach { task ->
                writer.append("${task.taskId},${task.userId},${task.taskName},${task.category},${task.date},${task.priority},${task.completed}\n")
            }
            writer.close()
        } catch (e: Exception) {
            Log.e("CalendarFragment", "Error writing tasks to file.", e)
        }
    }

    private fun changeTaskPriority(task: Task, imageView: ImageView) {
        val tasks = loadTasksFromFile() ?: mutableListOf()

        val foundTask = tasks.find { it.taskId == task.taskId }
        foundTask?.let {
            it.priority = (it.priority % 3) + 1
        }

        saveTasksToFile(tasks)
        setPriorityColor(foundTask?.priority ?: 1, imageView)
    }

    private fun showCompletionDialog(task: Task) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Выполнение задачи")
            .setMessage("Выполнить задачу '${task.taskName}'?")
            .setPositiveButton("Да") { _, _ ->
                toggleTaskCompleted(task)
            }
            .setNegativeButton("Нет") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .show()
    }

    private fun toggleTaskCompleted(task: Task) {
        val tasks = loadTasksFromFile() ?: mutableListOf()

        val foundTask = tasks.find { it.taskId == task.taskId }
        foundTask?.let {
            it.completed = !it.completed
        }

        saveTasksToFile(tasks)
        loadAndDisplayTasks()
    }
}
