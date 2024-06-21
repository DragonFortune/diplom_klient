package com.example.authentication.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.authentication.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class AddTaskFragment : BottomSheetDialogFragment() {

    private lateinit var calendarView: CalendarView
    private lateinit var taskInput: EditText
    private lateinit var categoryButton: Button
    private lateinit var saveButton: Button
    private var selectedDate: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.setCanceledOnTouchOutside(false)
        val view = inflater.inflate(R.layout.fragment_add_task, container, false)

        calendarView = view.findViewById(R.id.calendarView)
        taskInput = view.findViewById(R.id.multiAutoCompleteTextView)
        categoryButton = view.findViewById(R.id.button)
        saveButton = view.findViewById(R.id.button2)

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            selectedDate = format.format(calendar.time)
            Log.d("AddTaskFragment", "Selected date: $selectedDate")
        }

        saveButton.setOnClickListener {
            Log.d("AddTaskFragment", "Save button clicked")
            saveTask()
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.let { dialog ->
            val bottomSheet =
                dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            val behavior = BottomSheetBehavior.from(bottomSheet)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isFitToContents = false
            behavior.halfExpandedRatio = 0.5f
            behavior.peekHeight = resources.displayMetrics.heightPixels / 2

            behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        dismiss()
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                }
            })
        }
    }

    private fun saveTask() {
        val taskName = taskInput.text.toString()
        val category = categoryButton.text.toString()

        Log.d("AddTaskFragment", "Task name: $taskName, Category: $category, Selected date: $selectedDate")

        if (taskName.isEmpty() || selectedDate.isEmpty()) {
            Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
            Log.d("AddTaskFragment", "Missing task name or selected date")
            return
        }

        val task = Task(
            taskId = 0,
            userId = 1,
            taskName = taskName,
            category = category,
            date = selectedDate,
            priority = 1,
            completed = false
        )

        if (saveTaskToFile(task)) {
            Toast.makeText(requireContext(), "Задача сохранена", Toast.LENGTH_SHORT).show()
            Log.d("AddTaskFragment", "Task saved successfully")
            dismiss() // Закрытие фрагмента при успешном сохранении
        } else {
            Toast.makeText(requireContext(), "Ошибка сохранения задачи", Toast.LENGTH_SHORT).show()
            Log.d("AddTaskFragment", "Error saving task")
        }
    }

    private fun saveTaskToFile(task: Task): Boolean {
        val taskFile = File(requireContext().filesDir, "tasks.txt")
        return try {
            val writer = FileWriter(taskFile, true)
            writer.append("${task.taskId},${task.userId},${task.taskName},${task.category},${task.date},${task.priority},${task.completed}\n")
            writer.close()
            Log.d("AddTaskFragment", "Task written to file: $task")
            true
        } catch (e: Exception) {
            Log.e("AddTaskFragment", "Error writing task to file: $task", e)
            false
        }
    }

}

data class Task(
    val taskId: Int,
    val userId: Int,
    val taskName: String,
    val category: String = "",
    val date: String,
    var priority: Int = 0,
    var completed: Boolean
)
