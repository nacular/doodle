package io.nacular.doodle.controls.date

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus

internal val LocalDate.firstDayOfMonth: LocalDate get() = LocalDate(year = year, month = month, dayOfMonth = 1)
internal val LocalDate.numDaysOfMonth: Int get() {
    val startOfMonth = firstDayOfMonth

    return startOfMonth.daysUntil(startOfMonth + DatePeriod(months = 1))
}