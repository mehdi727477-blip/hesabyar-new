package ir.hesabyar.app

import java.util.Calendar

object Jalali {
    fun now(): String {
        val c = Calendar.getInstance()
        val (jy, jm, jd) = toJalali(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
        val hh = c.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')
        val mm = c.get(Calendar.MINUTE).toString().padStart(2, '0')
        return "%04d/%02d/%02d  %s:%s".format(jy, jm, jd, hh, mm)
    }

    private fun toJalali(gy0: Int, gm: Int, gd: Int): Triple<Int, Int, Int> {
        val gdm = intArrayOf(0,31, if (isLeap(gy0)) 29 else 28,31,30,31,30,31,31,30,31,30,31)
        var gy = gy0 - 1600; var jy = 979
        val gYearDays = 365 * gy + (gy + 3) / 4 - (gy + 99) / 100 + (gy + 399) / 400
        var gDayNo = gYearDays
        for (i in 1 until gm) gDayNo += gdm[i]
        gDayNo += gd - 1
        var jDayNo = gDayNo - 79
        val jNp = jDayNo / 12053
        jDayNo %= 12053
        jy += 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461
        if (jDayNo >= 366) { jy += (jDayNo - 1) / 365; jDayNo = (jDayNo - 1) % 365 }
        val jm: Int; val jd: Int
        if (jDayNo < 186) { jm = 1 + (jDayNo / 31); jd = 1 + (jDayNo % 31) }
        else { jm = 7 + ((jDayNo - 186) / 30); jd = 1 + ((jDayNo - 186) % 30) }
        return Triple(jy, jm, jd)
    }
    private fun isLeap(y: Int) = y % 4 == 0 && (y % 100 != 0 || y % 400 == 0)
}
