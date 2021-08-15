package id.ptkpn.retribusiapp.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object Utils {
    fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
        val formatter = SimpleDateFormat(format, locale)
        return formatter.format(this)
    }

    fun getCurrentDateTime(): Date {
        return Calendar.getInstance().time
    }

    fun formatPrice(price: Int): String {
        return NumberFormat.getNumberInstance(Locale.US)
            .format(price)
            .replace(",", ".")
    }

    fun getTypeGoodName(type: String) = when (type) {
        BAKULAN -> "Bakulan"
        PAKAI_MEJA -> "Pakai Meja"
        PAKAI_KIOS -> "Pakai Kios"
        else -> "Other"
    }
}