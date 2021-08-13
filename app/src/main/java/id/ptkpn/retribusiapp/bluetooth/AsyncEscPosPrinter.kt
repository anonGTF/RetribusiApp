package id.ptkpn.retribusiapp.bluetooth

import com.dantsu.escposprinter.EscPosPrinterSize
import com.dantsu.escposprinter.connection.DeviceConnection

class AsyncEscPosPrinter(
    val printerConnection: DeviceConnection?,
    printerDpi: Int,
    printerWidthMM: Float,
    printerNbrCharactersPerLine: Int
) :
    EscPosPrinterSize(printerDpi, printerWidthMM, printerNbrCharactersPerLine) {
    var textToPrint = ""
}