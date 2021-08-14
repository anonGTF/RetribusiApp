package id.ptkpn.retribusiapp.ui.kontribusipasar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.anggastudio.printama.Printama
import id.ptkpn.retribusiapp.R
import id.ptkpn.retribusiapp.databinding.ActivityKontribusiPasarBinding
import id.ptkpn.retribusiapp.localdb.Transaksi
import id.ptkpn.retribusiapp.ui.history.HistoryAuthActivity
import id.ptkpn.retribusiapp.utils.*
import id.ptkpn.retribusiapp.utils.Utils.getCurrentDateTime
import id.ptkpn.retribusiapp.utils.Utils.toString


class KontribusiPasarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKontribusiPasarBinding
    private lateinit var viewModel: KontribusiPasarViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKontribusiPasarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory = ViewModelFactory.getInstance(this)
        viewModel = ViewModelProvider(this, factory).get(KontribusiPasarViewModel::class.java)
        observeCount()

        binding.cetakBakulan.setOnClickListener {
            insertTransaksi(BAKULAN)
        }

        binding.cetakPakaiMeja.setOnClickListener {
            insertTransaksi(PAKAI_MEJA)
        }

        binding.cetakPakaiKios.setOnClickListener {
            insertTransaksi(PAKAI_KIOS)
        }

        binding.btnSelectPrinter.setOnClickListener{
            showPrinterList()
        }



        binding.btnHistory.setOnClickListener {
            intent = Intent(this, HistoryAuthActivity::class.java)
            startActivity(intent)
        }

        getSavedPrinter()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val printerName = Printama.getPrinterResult(resultCode, requestCode, data);
        showResult(printerName)
    }

    private fun getSavedPrinter() {
        val connectedPrinter = Printama.with(this).connectedPrinter
        if (connectedPrinter != null) {
            val text = "Connected to : " + connectedPrinter.name
            binding.btnSelectPrinter.text = text
        }
    }

    private fun showPrinterList() {
        Printama.showPrinterList(this, R.color.red) { printerName: String ->
            Toast.makeText(this, printerName, Toast.LENGTH_SHORT).show()
            val text = "Connected to : $printerName"
            binding.btnSelectPrinter.text = text
            if (!printerName.contains("failed")) {
                binding.btnTestPrint.visibility = View.VISIBLE
                binding.btnTestPrint.setOnClickListener { v: View? -> testPrinter() }
            }
        }
    }

    private fun testPrinter() {
        Printama.with(this).printTest()
    }

    private fun printReceipt(tanggal: String, tarif: Int) {
        val logo = Printama.getBitmapFromVector(this, R.drawable.print_logo)
        Printama.with(this).connect({ printama: Printama ->
            printama.printImage(logo, 256)
            printama.addNewLine(1)
            printama.setNormalText()
            printama.printTextlnBold(Printama.CENTER, "PRO System")
            printama.setSmallText()
            printama.printTextln(Printama.CENTER, "Perbup Lombok Barat no 26 Tahun 2019")
            printama.setNormalText()
            printama.printTextlnBold(Printama.CENTER, "RETRIBUSI Kebersihan PASAR")
            printama.addNewLine(1)
            printama.printTextlnWideBold(Printama.CENTER, "TGL : $tanggal")
            printama.addNewLine(1)
            printama.printTextlnWideTallBold(Printama.CENTER, "*RP.${tarif}*")
            printama.addNewLine(1)
            printama.printTextlnBold(Printama.CENTER, "*Bukti Sah Pembayaran")
            printama.printTextlnBold(Printama.CENTER, "Retribusi Kebersihan Pasar*")
            printama.addNewLine(1)
            printama.printDashedLine()
            printama.addNewLine(2)
            printama.close()
        }) { message: String? -> showMessage(message) }
    }

    private fun showMessage(message: String?) {
        Toast.makeText(this, message ?: "unknown error", Toast.LENGTH_SHORT).show()
    }

    private fun showResult(printerName: String) {
        showMessage(printerName)
        val text = "Connected to : $printerName"
        binding.btnSelectPrinter.text = text
        if (!printerName.contains("failed")) {
            binding.btnTestPrint.visibility = View.VISIBLE
            binding.btnTestPrint.setOnClickListener { v: View? -> testPrinter() }
        }
    }

    private fun insertTransaksi(type: String) {
        val tanggal = getCurrentDateTime().toString("dd/MM/yyyy")
        val tanggalPrint = tanggal.replace("/", "-")
        val waktu = getCurrentDateTime().toString("HH:mm:ss")
        val tarif = getSharedPreferences(PREF_KEY, MODE_PRIVATE).getInt(type, 0)
        val transaksi = Transaksi(
            jenisPedagang = type,
            jumlahBayar = tarif,
            tanggal = tanggal,
            waktu = waktu
        )

        Log.d(
            "coba", "insertTransaksi: " +
                    "${transaksi.jenisPedagang} ${transaksi.jumlahBayar} " +
                    "${transaksi.tanggal} ${transaksi.waktu}"
        )

        printReceipt(tanggalPrint, tarif)
        viewModel.insertTransaksi(transaksi)
    }

    private fun observeCount() {
        viewModel.countBakulan().observe(this, { count ->
            val countText = "${count}x"
            binding.countBakulan.text = countText
        })

        viewModel.countPakaiKios().observe(this, { count ->
            val countText = "${count}x"
            binding.countPakaiKios.text = countText
        })

        viewModel.countPakaiMeja().observe(this, { count ->
            val countText = "${count}x"
            binding.countPakaiMeja.text = countText
        })

        viewModel.countAll().observe(this, { count ->
            val countText = "${count}x"
            binding.countTotal.text = countText
        })
    }
}