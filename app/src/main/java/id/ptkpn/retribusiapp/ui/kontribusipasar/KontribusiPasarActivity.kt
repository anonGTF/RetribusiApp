package id.ptkpn.retribusiapp.ui.kontribusipasar

import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.anggastudio.printama.Printama
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import id.ptkpn.retribusiapp.R
import id.ptkpn.retribusiapp.databinding.ActivityKontribusiPasarBinding
import id.ptkpn.retribusiapp.localdb.Transaksi
import id.ptkpn.retribusiapp.ui.history.HistoryAuthActivity
import id.ptkpn.retribusiapp.ui.login.LoginActivity
import id.ptkpn.retribusiapp.utils.*
import id.ptkpn.retribusiapp.utils.Utils.getCurrentDateTime
import id.ptkpn.retribusiapp.utils.Utils.getTypeGoodName
import id.ptkpn.retribusiapp.utils.Utils.toString
import kotlinx.coroutines.*


class KontribusiPasarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKontribusiPasarBinding
    private lateinit var viewModel: KontribusiPasarViewModel
    private var connectedPrinter: BluetoothDevice? = null
    private val auth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKontribusiPasarBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Kontribusi Pasar"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        checkLogin()

        val factory = ViewModelFactory.getInstance(this)
        viewModel = ViewModelProvider(this, factory).get(KontribusiPasarViewModel::class.java)

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

        binding.btnTestPrint.setOnClickListener {
            testPrinter()
        }

        binding.btnHistory.setOnClickListener {
            intent = Intent(this, HistoryAuthActivity::class.java)
            startActivity(intent)
        }

        getSavedPrinter()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.action_logout -> {
                auth.signOut()
                intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val name = Printama.with(this).connectedPrinter.alias ?: "default printer alias"
        showResult(name)
    }

    override fun onResume() {
        super.onResume()
        checkLogin()
        getSavedPrinter()
    }

    override fun onRestart() {
        super.onRestart()
        checkLogin()
        getSavedPrinter()
    }

    private fun checkLogin() {
        if (auth.currentUser == null) {
            intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            showMessage("Anda harus login terlebih dahulu")
        }
    }

    private fun getSavedPrinter() {
        connectedPrinter = Printama.with(this).connectedPrinter
        if (connectedPrinter != null) {
            Log.d("coba", "getSavedPrinter: ${connectedPrinter!!.alias}")
            val text = "Connected to : " + connectedPrinter!!.alias
            binding.btnSelectPrinter.text = text
        }
    }

    private fun showPrinterList() {
        Printama.showPrinterList(this, R.color.red) { printerName: String ->
            val name = if (Printama.with(this).connectedPrinter != null)
                Printama.with(this).connectedPrinter.alias
            else printerName
            Toast.makeText(this, name, Toast.LENGTH_SHORT).show()
            val text = "Connected to : $name"
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

    private fun printReceipt(tanggal: String, tarif: Int, transaksi: Transaksi) {
        showLoadingState()
        val logo = BitmapFactory.decodeResource(resources, R.drawable.print_logo)
        if (connectedPrinter != null) {
            Printama.with(this@KontribusiPasarActivity).connect({ printama: Printama ->
                if (printama.isConnected) {
                    Log.d("coba", "printReceipt: ${printama.isConnected}")
                    printama.printImage(logo, 256)
                    printama.addNewLine(1)
                    printama.setNormalText()
                    printama.printTextlnBold(Printama.CENTER, "PRO System")
                    printama.setSmallText()
                    printama.printTextln(
                        Printama.CENTER,
                        "Perbup Lombok Barat no 26 Tahun 2019"
                    )
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
                    viewModel.insertTransaksi(transaksi)
                    Log.d("coba", "printReceipt: $transaksi")
                } else {
                    showMessage("Printer gagal terhubung")
                }
                hideLoadingState()
            }) { message: String? -> showMessage(message) }
        } else {
            showMessage("Printer belum terhubung")
        }
    }

    private fun showMessage(message: String?) {
        Toast.makeText(applicationContext, message ?: "unknown error", Toast.LENGTH_SHORT).show()
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

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Cetak Transaksi ${getTypeGoodName(type)}")
        builder.setMessage("Anda yakin ingin mencetak transaksi ${getTypeGoodName(type)}?")
        builder.setPositiveButton("Cetak") { _, _ ->
            printReceipt(tanggalPrint, tarif, transaksi)
        }

        builder.setNegativeButton("Batalkan") { _, _ ->
            Toast.makeText(applicationContext, "batalkan print", Toast.LENGTH_SHORT).show()
        }
        builder.show()
    }

    private fun showLoadingState() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoadingState() {
        binding.progressBar.visibility = View.INVISIBLE
    }
}