package id.ptkpn.retribusiapp.utils

import android.content.Context
import android.util.DisplayMetrics
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.textparser.PrinterTextParserImg
import id.ptkpn.retribusiapp.R

object PrintUtils {

    fun getPrintText(printer: EscPosPrinter, context: Context, price: Int, date: String): String {
        return "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, context.resources.getDrawableForDensity(
            R.drawable.print_logo, DisplayMetrics.DENSITY_MEDIUM))+"</img>\n" +
                "[C]<b>PRO System</b>\n" +
                "[L]\n" +
                "[C]Perbup Lombok Barat no 26 Tahun 2019\n" +
                "[L]\n" +
                "[C]<b>RETRIBUSI Kebersihan PASAR</b>\n" +
                "[L]\n" +
                "[L]<font size='wide'><b>Tgl : $date</b></font>\n" +
                "[L]\n" +
                "[C]<font size='big'>*RP.$price*</font>\n" +
                "[C]*Bukti Sah Pembayaran\n" +
                "[C]Retribusi Keberihan Pasar*\n" +
                "[L]\n" +
                "[C]-------------------------------------\n"
    }
}