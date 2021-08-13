package id.ptkpn.retribusiapp.bluetooth

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import com.dantsu.escposprinter.EscPosCharsetEncoding
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.DeviceConnection
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException
import com.dantsu.escposprinter.exceptions.EscPosConnectionException
import com.dantsu.escposprinter.exceptions.EscPosEncodingException
import com.dantsu.escposprinter.exceptions.EscPosParserException
import java.lang.ref.WeakReference

class AsyncBluetoothEscPosPrint(context: Context) :
    AsyncTask<AsyncEscPosPrinter?, Int?, Int>() {

    private var dialog: ProgressDialog? = null
    private var weakContext: WeakReference<Context> = WeakReference(context)

    override fun doInBackground(vararg printersData: AsyncEscPosPrinter?): Int {
        if (printersData.isEmpty()) {
            return FINISH_NO_PRINTER
        }

        val printerData = printersData[0]
        val deviceConnection: DeviceConnection? = printerData?.printerConnection

        publishProgress(PROGRESS_CONNECTING)

        if (deviceConnection == null) {
            if (printerData != null) {
                return doInBackgroundReal(printerData)
            }
        } else {
            try {
                deviceConnection.connect()
            } catch (e: EscPosConnectionException) {
                e.printStackTrace()
            }
        }
        return FINISH_NO_PRINTER
    }
    private fun doInBackgroundReal(vararg printersData: AsyncEscPosPrinter): Int {
        if (printersData.isEmpty()) {
            return FINISH_NO_PRINTER
        }
        publishProgress(PROGRESS_CONNECTING)
        val printerData = printersData[0]
        try {
            val deviceConnection: DeviceConnection? = printerData.printerConnection
            val printer = EscPosPrinter(
                deviceConnection,
                printerData.printerDpi,
                printerData.printerWidthMM,
                printerData.printerNbrCharactersPerLine,
                EscPosCharsetEncoding("windows-1252", 16)
            )
            publishProgress(PROGRESS_PRINTING)
            printer.printFormattedTextAndCut(printerData.textToPrint)
            publishProgress(PROGRESS_PRINTED)
        } catch (e: EscPosConnectionException) {
            e.printStackTrace()
            return FINISH_PRINTER_DISCONNECTED
        } catch (e: EscPosParserException) {
            e.printStackTrace()
            return FINISH_PARSER_ERROR
        } catch (e: EscPosEncodingException) {
            e.printStackTrace()
            return FINISH_ENCODING_ERROR
        } catch (e: EscPosBarcodeException) {
            e.printStackTrace()
            return FINISH_BARCODE_ERROR
        }
        return FINISH_SUCCESS
    }

    override fun onPreExecute() {
        if (dialog == null) {
            val context = weakContext.get() ?: return
            dialog = ProgressDialog(context)
            dialog!!.setTitle("Printing in progress...")
            dialog!!.setMessage("...")
            dialog!!.setProgressNumberFormat("%1d / %2d")
            dialog!!.setCancelable(false)
            dialog!!.isIndeterminate = false
            dialog!!.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            dialog!!.show()
        }
    }

    fun onProgressUpdate(vararg progress: Int) {
        when (progress[0]) {
            PROGRESS_CONNECTING -> dialog!!.setMessage("Connecting printer...")
            PROGRESS_CONNECTED -> dialog!!.setMessage("Printer is connected...")
            PROGRESS_PRINTING -> dialog!!.setMessage("Printer is printing...")
            PROGRESS_PRINTED -> dialog!!.setMessage("Printer has finished...")
        }
        dialog!!.progress = progress[0]
        dialog!!.max = 4
    }

    override fun onPostExecute(result: Int) {
        dialog!!.dismiss()
        dialog = null
        val context = weakContext.get() ?: return
        when (result) {
            FINISH_SUCCESS -> AlertDialog.Builder(context)
                .setTitle("Success")
                .setMessage("Congratulation ! The text is printed !")
                .show()
            FINISH_NO_PRINTER -> AlertDialog.Builder(context)
                .setTitle("No printer")
                .setMessage("The application can't find any printer connected.")
                .show()
            FINISH_PRINTER_DISCONNECTED -> AlertDialog.Builder(context)
                .setTitle("Broken connection")
                .setMessage("Unable to connect the printer.")
                .show()
            FINISH_PARSER_ERROR -> AlertDialog.Builder(context)
                .setTitle("Invalid formatted text")
                .setMessage("It seems to be an invalid syntax problem.")
                .show()
            FINISH_ENCODING_ERROR -> AlertDialog.Builder(context)
                .setTitle("Bad selected encoding")
                .setMessage("The selected encoding character returning an error.")
                .show()
            FINISH_BARCODE_ERROR -> AlertDialog.Builder(context)
                .setTitle("Invalid barcode")
                .setMessage("Data send to be converted to barcode or QR code seems to be invalid.")
                .show()
        }
    }

    companion object {
        private const val FINISH_SUCCESS = 1
        private const val FINISH_NO_PRINTER = 2
        private const val FINISH_PRINTER_DISCONNECTED = 3
        private const val FINISH_PARSER_ERROR = 4
        private const val FINISH_ENCODING_ERROR = 5
        private const val FINISH_BARCODE_ERROR = 6
        private const val PROGRESS_CONNECTING = 1
        private const val PROGRESS_CONNECTED = 2
        private const val PROGRESS_PRINTING = 3
        private const val PROGRESS_PRINTED = 4
    }
}