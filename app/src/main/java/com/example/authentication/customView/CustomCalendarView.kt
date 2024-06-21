package com.example.authentication.customView

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.example.authentication.R
import java.text.SimpleDateFormat
import java.util.*

class CustomCalendarView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        orientation = HORIZONTAL

        val calendar = Calendar.getInstance()
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val weekDaysLayout = LinearLayout(context)
        weekDaysLayout.orientation = HORIZONTAL
        weekDaysLayout.gravity = Gravity.CENTER
        addView(weekDaysLayout)

        for (i in 0 until 7) {
            val dayLayout = LinearLayout(context)
            dayLayout.setBackgroundResource(R.drawable.btn_back)
            dayLayout.orientation = VERTICAL
            dayLayout.gravity = Gravity.CENTER

            dayLayout.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            dayLayout.setPadding(8, 8, 8, 8)
            val layoutParams = LayoutParams(150, 220)
            dayLayout.layoutParams = layoutParams
            val dayOfWeekTextView = TextView(context)
            dayOfWeekTextView.text = SimpleDateFormat("EE", Locale.getDefault()).format(calendar.time)
            dayOfWeekTextView.gravity = Gravity.CENTER
            dayOfWeekTextView.setPadding(8, 8, 8, 10)
            dayLayout.addView(dayOfWeekTextView)

            val dayOfMonthTextView = TextView(context)
            dayOfMonthTextView.text = calendar.get(Calendar.DAY_OF_MONTH).toString()
            dayOfMonthTextView.gravity = Gravity.CENTER
            dayLayout.addView(dayOfMonthTextView)

            if (calendar.get(Calendar.DAY_OF_MONTH) == currentDayOfMonth && calendar.get(Calendar.DAY_OF_WEEK) == currentDayOfWeek) {
                dayLayout.setBackgroundResource(R.drawable.today_back)
            }

            addView(dayLayout)

            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }
}
