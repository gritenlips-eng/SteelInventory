package com.example.steelinventory.util

import java.util.Calendar

/** تاریخ امروز به شمسی، قالب 1405/05/12 */
fun todayJalali(): String {
    val c = Calendar.getInstance()
    val (jy, jm, jd) = gregorianToJalali(
        c.get(Calendar.YEAR),
        c.get(Calendar.MONTH) + 1,
        c.get(Calendar.DAY_OF_MONTH)
    )
    return "%04d/%02d/%02d".format(jy, jm, jd)
}

fun gregorianToJalali(gy: Int, gm: Int, gd: Int): Triple<Int, Int, Int> {
    val gDaysInMonth = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    val jDaysInMonth = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

    var gy2 = gy - 1600
    var gm2 = gm - 1
    val gd2 = gd - 1

    var gDayNo = 365 * gy2 + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400
    for (i in 0 until gm2) gDayNo += gDaysInMonth[i]
    if (gm2 > 1 && ((gy % 4 == 0 && gy % 100 != 0) || gy % 400 == 0)) gDayNo++
    gDayNo += gd2

    var jDayNo = gDayNo - 79
    val jNp = jDayNo / 12053
    jDayNo %= 12053

    var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
    jDayNo %= 1461

    if (jDayNo >= 366) {
        jy += (jDayNo - 366) / 365
        jDayNo = (jDayNo - 366) % 365
    }

    var jm = 0
    while (jm < 11 && jDayNo >= jDaysInMonth[jm]) {
        jDayNo -= jDaysInMonth[jm]
        jm++
    }

    return Triple(jy, jm + 1, jDayNo + 1)
}
