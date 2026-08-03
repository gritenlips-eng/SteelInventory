package com.example.steelinventory.util

import kotlin.math.round

/** گرد کردن به دو رقم اعشار */
fun round2(value: Double): Double = round(value * 100) / 100

/** حذف اعشار اضافی: 12.50 -> 12.5 و 12.00 -> 12 */
fun num(value: Double): String {
    val r = round2(value)
    val s = String.format("%.2f", r)
    return s.trimEnd('0').trimEnd('.', ',')
}

/** عدد همراه واحد کیلوگرم فارسی */
fun kg(value: Double): String = num(value) + " ک"

/** نمایش بازه وزن، مثلاً: 6.2 تا 6.5 ک */
fun kgRange(min: Double, max: Double): String =
    if (round2(min) == round2(max)) kg(min)
    else num(min) + " تا " + num(max) + " ک"
