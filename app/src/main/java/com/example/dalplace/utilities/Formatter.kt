package com.example.dalplace.utilities

import android.telephony.PhoneNumberUtils
import android.text.format.DateUtils
import java.text.NumberFormat
import java.util.*

object Formatter {

    fun currency(amount: Double?, precision: Int = 2): String {
        val locale = Locale.getDefault()
        val numberFormatter = NumberFormat.getCurrencyInstance(locale)
        numberFormatter.maximumFractionDigits = precision

        amount?.let {
            if (it <= 0.0) return "Free"
            return numberFormatter.format(it)
        }
        return "Unavailable"
    }

    fun phone(phoneNumber: String?): String {
        val locale = Locale.getDefault()
        val phone = PhoneNumberUtils.formatNumber(phoneNumber, locale.country.uppercase())
        return "(${phone.subSequence(0, 3)}) ${phone.subSequence(3, 6)}-${phone.subSequence(6, 10)}"

    }

    fun friendlyDate(date: Date?): String {

        date?.let {
            val lastUpdated = it.time
            val currentTime = Calendar.getInstance().timeInMillis

            return DateUtils.getRelativeTimeSpanString(
                lastUpdated,
                currentTime,
                DateUtils.SECOND_IN_MILLIS
            ).toString()
        }

        return "Unavailable"
    }
}