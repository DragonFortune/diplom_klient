package com.example.authentication.fragment

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.authentication.R
import com.example.authentication.SubTask
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var tasksContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val todayText: TextView = view.findViewById(R.id.today)
        val currentDate = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("dd MMMM", Locale.getDefault())
        val formattedDate = dateFormat.format(currentDate)
        todayText.text = formattedDate

        tasksContainer = view.findViewById(R.id.tasksContainer)

        loadTasksAndSubTasks()

        return view
    }

    private fun loadTasksAndSubTasks() {
        val tasks = loadTasksFromFile()
        val subTasks = loadSubTasksFromFile()
        tasksContainer.removeAllViews()
        if (tasks != null) {
            for (task in tasks) {
                if (!task.completed) {
                    addTaskView(task, subTasks.filter { it.taskId == task.taskId })
                }
            }
        } else {
            Log.e("HomeFragment", "No tasks found or error reading the file.")
        }
    }

    private fun addTaskView(task: Task, allSubTasks: List<SubTask>) {
        val remainingDays = calculateRemainingDays(task.date)
        val taskItemView = layoutInflater.inflate(R.layout.task_item, tasksContainer, false)

        val taskText: TextView = taskItemView.findViewById(R.id.taskText)
        val categoryText: TextView = taskItemView.findViewById(R.id.category)
        val taskImage: ImageView = taskItemView.findViewById(R.id.taskImage)
        val timeText: TextView = taskItemView.findViewById(R.id.time)

        taskText.text = task.taskName
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
            time = dateFormat.parse(taskDate)
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
            Log.e("HomeFragment", "Task file does not exist.")
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
                    Log.e("HomeFragment", "Invalid task format: $line")
                }
            }
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error reading tasks from file.", e)
            return null
        }
        return tasks
    }

    private fun loadSubTasksFromFile(): MutableList<SubTask> {
        val subTaskFile = File(requireContext().filesDir, "subtasks.txt")
        if (!subTaskFile.exists()) {
            Log.e("HomeFragment", "Subtask file does not exist.")
            return mutableListOf()
        }

        val subTasks = mutableListOf<SubTask>()
        try {
            subTaskFile.forEachLine { line ->
                val parts = line.split(",")
                if (parts.size == 4) {
                    val subTask = SubTask(
                        taskId = parts[0].toInt(),
                        subTaskId = parts[1].toInt(),
                        subTaskName = parts[2],
                        completed = parts[3].toBoolean()
                    )
                    subTasks.add(subTask)
                } else {
                    Log.e("HomeFragment", "Invalid subtask format: $line")
                }
            }
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error reading subtasks from file.", e)
        }
        return subTasks
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
            Log.e("HomeFragment", "Error writing tasks to file.", e)
        }
    }

    private fun changeTaskPriority(task: Task, imageView: ImageView) {
        val tasks = loadTasksFromFile() ?: mutableListOf()
        val foundTask = tasks.find { it.taskId == task.taskId }
        foundTask?.apply {
            priority += 1
            if (priority > 3) {
                priority = 1
            }
        }
        saveTasksToFile(tasks)
        setPriorityColor(foundTask?.priority ?: 1, imageView)
        loadTasksAndSubTasks()
    }

    private fun showCompletionDialog(task: Task) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Выполнение задачи")
            .setMessage("Выполнить задачу '${task.taskName}'?")
            .setPositiveButton("Да") { dialogInterface: DialogInterface, i: Int ->
                toggleTaskCompleted(task)
            }
            .setNegativeButton("Нет") { dialogInterface: DialogInterface, i: Int ->
                dialogInterface.dismiss()
            }
            .show()
    }

    private fun toggleTaskCompleted(task: Task) {
        val tasks = loadTasksFromFile() ?: mutableListOf()
        val foundTask = tasks.find { it.taskId == task.taskId }
        foundTask?.apply {
            completed = !completed
        }
        saveTasksToFile(tasks)
        loadTasksAndSubTasks()
    }

    private fun saveSubTasksToFile(subTasks: List<SubTask>) {
        val subTaskFile = File(requireContext().filesDir, "subtasks.txt")
        try {
            val writer = FileWriter(subTaskFile)
            subTasks.forEach { subTask ->
                writer.append("${subTask.taskId},${subTask.subTaskId},${subTask.subTaskName},${subTask.completed}\n")
            }
            writer.close()
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error writing subtasks to file.", e)
        }
    }

    private fun changeSubTaskCompletion(subTask: SubTask) {
        val subTasks = loadSubTasksFromFile()
        val foundSubTask = subTasks.find { it.taskId == subTask.taskId && it.subTaskId == subTask.subTaskId }
        foundSubTask?.apply {
            completed = subTask.completed
        }
        saveSubTasksToFile(subTasks)
    }
}
