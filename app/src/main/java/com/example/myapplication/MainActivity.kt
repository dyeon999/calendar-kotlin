package com.example.myapplication

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.children
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.databinding.CalendarDayLayoutBinding
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.previousMonth
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.WeekDayBinder
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private var selectedStartDate: LocalDate? = null
    private var selectedEndDate: LocalDate? = null
    private var rangeRe : Boolean = false
    var pageMonth = YearMonth.now()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(100)  // Adjust as needed
        val endMonth = currentMonth.plusMonths(100)  // Adjust as needed
        val firstDayOfWeek = firstDayOfWeekFromLocale() // Available from the library

        binding.calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.textView.text = data.date.dayOfMonth.toString()
                if (rangeRe) {
                    Log.d("true", "true")
                    container.textView.background = null
                    if (data.date == selectedStartDate) { //선택한 날짜와 현재 날짜가 같고, 끝라인 리아면
                        container.textView.setBackgroundResource(R.drawable.onclick)
                    }
                }
                else{
                    if (isDateInRange(data.date)) {
                        container.textView.setBackgroundResource(R.drawable.calender_box_2)
                    } else if (data.date == selectedStartDate) {
                        // 다른 날짜에 대한 배경 재설정
                        container.textView.setBackgroundResource(R.drawable.calender_box_1)
                    } else if (data.date == selectedEndDate) {
                        // 다른 날짜에 대한 배경 재설정
                        container.textView.setBackgroundResource(R.drawable.calender_box_3)
                    } else {
                        container.textView.background = null
                    }
                }
                if (data.position == DayPosition.MonthDate) {
                    container.textView.setTextColor(Color.BLACK)
                } else {
                    container.textView.setTextColor(Color.GRAY)
                    container.canClick = false
                }
            }

            override fun create(view: View): DayViewContainer {
                Log.d("pageMonth", pageMonth.toString())
                return DayViewContainer(view)
            }  // this refers to DaySelectionListener
        }
        binding.calendarView.setup(startMonth, endMonth, firstDayOfWeek)
        binding.calendarView.scrollToMonth(currentMonth)

        val daysOfWeek = daysOfWeek() // 요일 리스트

        // 요일 인것으로 추정
        val titlesContainer = findViewById<ViewGroup>(R.id.titlesContainer)
        titlesContainer.children
            .map {it as TextView}
            .forEachIndexed {index, textView ->
                val dayOfWeek = daysOfWeek[index] // 요일 리스트에서 index에 해당하는 값을 dayOfWeek 변수에 저장한다.
                val title = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH) // title에 요일을 저장한다. 짧게. 영어로
                textView.text = title // 텍뷰의 텍스트를 title로 설정
            }

        binding.calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                // Remember that the header is reused so this will be called for each month.
                // However, the first day of the week will not change so no need to bind
                // the same view every time it is reused.
                if (container.titlesContainer.tag == null) {
                    container.titlesContainer.tag = data.yearMonth
                    container.titlesContainer.children.map { it as TextView }
                        .forEachIndexed { index, textView ->
                            val dayOfWeek = daysOfWeek[index]
                            val title =
                                dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                            textView.text = title
                            // In the code above, we use the same `daysOfWeek` list
                            // that was created when we set up the calendar.
                            // However, we can also get the `daysOfWeek` list from the month data:
                            // val daysOfWeek = data.weekDays.first().map { it.date.dayOfWeek }
                            // Alternatively, you can get the value for this specific index:
                            // val dayOfWeek = data.weekDays.first()[index].date.dayOfWeek
                        }
                }
            }
        }
        binding.calendarView.monthScrollListener = { calendarMonth ->
            val pageMonth = calendarMonth.yearMonth
            val year = pageMonth.year.toString()
            val month = pageMonth.month.value
            binding.date.text = year +"." + month + "월"
        }
        binding.right.setOnClickListener {
            val nextMonth = pageMonth.nextMonth
            pageMonth = pageMonth.nextMonth
            Log.d("month", pageMonth.toString())
            binding.calendarView.smoothScrollToMonth(nextMonth)
        }
        binding.left.setOnClickListener {
            val preMonth = pageMonth.previousMonth
            pageMonth = pageMonth.previousMonth
            Log.d("month", pageMonth.toString())
            binding.calendarView.smoothScrollToMonth(preMonth)
        }
        binding.today.setOnClickListener{
            binding.calendarView.smoothScrollToMonth(currentMonth)
            pageMonth = currentMonth
        }
    }

    private fun onDaySelected(date: LocalDate) {
        rangeRe=false
        if (selectedStartDate == null) {
            // 시작 날짜를 선택한 경우
            selectedStartDate = date
            selectedEndDate = null
        } else if (selectedEndDate == null && date.isAfter(selectedStartDate)) {
            // 끝 날짜를 선택한 경우
            selectedEndDate = date
            Log.d("month", selectedEndDate.toString())
            binding.calendarView.notifyCalendarChanged()
        } else {
            // 다시 시작 날짜부터 선택한 경우
            selectedStartDate = date
            selectedEndDate = null
            rangeRe=true
            binding.calendarView.notifyCalendarChanged()
        }
    }

    private fun isDateInRange(date: LocalDate): Boolean {
        return selectedStartDate != null && selectedEndDate != null &&
                        (date.isAfter(selectedStartDate) && date.isBefore(selectedEndDate))
    }

    // 선택되었을 때
    inner class DayViewContainer(view: View): ViewContainer(view) {
        val textView = CalendarDayLayoutBinding.bind(view).calendarDayText
        var isSelected : Boolean = false
        private var selectedDate: LocalDate? = null
        var canClick : Boolean = true
        init {
            view.setOnClickListener {
                if (canClick) {
                    if (!isSelected) {
                        view.setBackgroundResource(R.drawable.onclick)
                        isSelected = true
                        val text = textView.text.toString()
                        val day = text.toInt()
                        selectedDate = pageMonth.atDay(day)
                        Log.d("selected", selectedDate.toString())
                        onDaySelected(selectedDate!!)
                    } else {
                        isSelected = false
                    }
                }
            }
        }
    }
}