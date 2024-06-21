package com.example.authentication.fragment

import android.app.AlertDialog
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.authentication.R

class ClockFragment : Fragment() {

    private lateinit var numberPicker1: NumberPicker
    private lateinit var numberPicker2: NumberPicker
    private lateinit var numberPicker3: NumberPicker
    private lateinit var btnStart: Button
    private var countDownTimer: CountDownTimer? = null
    private var mediaPlayer: MediaPlayer? = null
    private var isTimerRunning: Boolean = false

    private var exitConfirmationDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_clock, container, false)

        numberPicker1 = view.findViewById(R.id.numberPicker1)
        numberPicker2 = view.findViewById(R.id.numberPicker2)
        numberPicker3 = view.findViewById(R.id.numberPicker3)
        btnStart = view.findViewById(R.id.btn_start)

        numberPicker1.minValue = 0
        numberPicker1.maxValue = 23
        numberPicker1.wrapSelectorWheel = true

        numberPicker2.minValue = 0
        numberPicker2.maxValue = 59
        numberPicker2.wrapSelectorWheel = true

        numberPicker3.minValue = 0
        numberPicker3.maxValue = 59
        numberPicker3.isEnabled = false

        numberPicker3.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS

        btnStart.setOnClickListener {
            val selectedHours = numberPicker1.value
            val selectedMinutes = numberPicker2.value
            val selectedSeconds = numberPicker3.value
            val totalTimeInMillis = (selectedHours * 3600 + selectedMinutes * 60 + selectedSeconds) * 1000L
            startCountDownTimer(totalTimeInMillis)
            numberPicker1.isEnabled = false
            numberPicker2.isEnabled = false
        }

        if (isFirstVisit(requireContext())) {
            showFocusModeNotification()
            setFirstVisit(requireContext(), false)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isTimerRunning) {
                    showExitConfirmationDialog()
                } else {
                    isEnabled = false
                    requireActivity().onBackPressed()
                }
            }
        })

        val textView1 = view.findViewById<TextView>(R.id.textView1)
        val textView2 = view.findViewById<TextView>(R.id.textView2)
        val textView3 = view.findViewById<TextView>(R.id.textView3)

        textView1.setOnClickListener {
            val text = textView1.text.toString()
            setNumberPickerFromText(text)
        }

        textView2.setOnClickListener {
            val text = textView2.text.toString()
            setNumberPickerFromText(text)
        }

        textView3.setOnClickListener {
            val text = textView3.text.toString()
            setNumberPickerFromText(text)
        }

        return view
    }

    override fun onPause() {
        super.onPause()
        if (isTimerRunning) {
            countDownTimer?.cancel()
            showExitConfirmationDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        if (isTimerRunning) {
            val hours = numberPicker1.value
            val minutes = numberPicker2.value
            val seconds = numberPicker3.value
            val totalTimeInMillis = (hours * 3600 + minutes * 60 + seconds) * 1000L
            startCountDownTimer(totalTimeInMillis)
        }
    }

    private fun setNumberPickerFromText(timeText: String) {
        val parts = timeText.split(":")
        if (parts.size == 2) {
            val hours = parts[0].toIntOrNull() ?: 0
            val minutes = parts[1].toIntOrNull() ?: 0
            numberPicker1.value = hours
            numberPicker2.value = minutes
        }
    }

    private fun startCountDownTimer(timeInMillis: Long) {
        countDownTimer?.cancel()

        countDownTimer = object : CountDownTimer(timeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val hours = millisUntilFinished / 3600000
                val minutes = (millisUntilFinished % 3600000) / 60000
                val seconds = (millisUntilFinished % 60000) / 1000

                numberPicker1.value = hours.toInt()
                numberPicker2.value = minutes.toInt()
                numberPicker3.value = seconds.toInt()

                isTimerRunning = true
            }

            override fun onFinish() {
                numberPicker1.value = 0
                numberPicker2.value = 0
                numberPicker3.value = 0

                isTimerRunning = false
                Toast.makeText(requireContext(), "Режим концентрации завершен", Toast.LENGTH_SHORT).show()
                playAlarmSound()
                numberPicker1.isEnabled = true
                numberPicker2.isEnabled = true
            }
        }.start()
    }

    private fun playAlarmSound() {
        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.alarm_sound)
        mediaPlayer?.start()
    }

    private fun showExitConfirmationDialog() {
        exitConfirmationDialog = AlertDialog.Builder(requireContext())
            .setTitle("Выход")
            .setMessage("Вы уверены, что хотите выйти? Прогресс будет потерян.")
            .setPositiveButton("Да") { _, _ ->
                countDownTimer?.cancel()
                isTimerRunning = false
                requireActivity().onBackPressed()
            }
            .setNegativeButton("Нет") { dialog, _ ->
                dialog.dismiss()
                exitConfirmationDialog = null
            }
            .show()
    }

    private fun showFocusModeNotification() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.first_time_timer, null)
        val builder = AlertDialog.Builder(requireContext(), R.style.TransparentAlertDialog)
            .setView(dialogView)
        val alertDialog = builder.create()

        dialogView.findViewById<Button>(R.id.btn_ok).setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }


    private fun isFirstVisit(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences("FocusModePrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("isFirstVisit", true)
    }

    private fun setFirstVisit(context: Context, isFirst: Boolean) {
        val sharedPreferences = context.getSharedPreferences("FocusModePrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean("isFirstVisit", isFirst)
            apply()
        }
    }
}
