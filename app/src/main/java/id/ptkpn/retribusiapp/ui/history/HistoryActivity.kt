package id.ptkpn.retribusiapp.ui.history

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import id.ptkpn.retribusiapp.databinding.ActivityHistoryBinding
import id.ptkpn.retribusiapp.utils.*
import id.ptkpn.retribusiapp.utils.FileUtils.generateFile
import id.ptkpn.retribusiapp.utils.FileUtils.goToFileIntent
import id.ptkpn.retribusiapp.utils.Utils.formatPrice
import id.ptkpn.retribusiapp.utils.Utils.getCurrentDateTime
import id.ptkpn.retribusiapp.utils.Utils.toString
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.AesKeyStrength
import net.lingala.zip4j.model.enums.CompressionMethod
import net.lingala.zip4j.model.enums.EncryptionMethod
import java.io.File
import java.lang.Exception
import java.util.*


class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var viewModel: HistoryViewModel
    private var jumlahBakulan = 0
    private var jumlahPakaiMeja = 0
    private var jumlahPakaiKios = 0
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory = ViewModelFactory.getInstance(this)
        viewModel = ViewModelProvider(this, factory).get(HistoryViewModel::class.java)

        populateRingkasan()

        binding.etJumlahDisetor.addTextChangedListener(
            MyTextWatcher(
                binding.etSelisih,
                binding.etTotalKeseluruhan
            )
        )

        binding.btnUpdate.setOnClickListener {
            uploadTransaksiData()
        }

        binding.btnExportExcel.setOnClickListener {
            exportLocalDb()
        }

        binding.btnReset.setOnClickListener {
            binding.etTotalKeseluruhan.setText("")
            viewModel.deleteAllTransaksi()
        }
    }

    private fun getFileName(format: String): String {
        val currDate = getCurrentDateTime().toString("dd_MM_yyyy")
        val userName = auth.currentUser?.email?.split('@')?.get(0) ?: "penagih_default_name"
        return "transaksi_${userName}_${currDate}.${format}"
    }

    private fun exportLocalDb() {
        val userName = auth.currentUser?.email?.split('@')?.get(0) ?: "penagih_default_name"
        val csvFile = generateFile(this, getFileName("csv"))
        if (csvFile != null) {
            viewModel.getAllTransaksi().observe(this@HistoryActivity, { transaksiList ->
                csvWriter().open(csvFile, append = false) {
                    // Header
                    writeRow("sep=,")
                    writeRow(listOf("NO", "TGL", "JAM", "USER NAME", "JENIS PEDAGANG", "JML BAYAR"))
                    // Data
                    transaksiList.forEachIndexed { index, transaksi ->
                        val data = listOf(
                            index + 1,
                            transaksi.tanggal,
                            transaksi.waktu,
                            userName,
                            transaksi.jenisPedagang,
                            transaksi.jumlahBayar
                        )
                        writeRow(data)
                    }
                    showMessage("CSV File has been generated")
                }
                compressAndEncryptFile(csvFile)
            })
        } else {
            showMessage("CSV not generated")
        }
    }

    private fun compressAndEncryptFile(csvFile: File) {
        try {
            val zos = MyZipOutputStream()
            val zipFile = zos.initialize(generateFile(this, getFileName("zip")),
                    listOf(csvFile),
                    "test123".toCharArray(),
                    CompressionMethod.STORE,
                    true,
                    EncryptionMethod.AES,
                    AesKeyStrength.KEY_STRENGTH_256
            )
            showMessage("file encrypted")
            val intent = goToFileIntent(this@HistoryActivity, zipFile)
            startActivity(intent)
        } catch (e: Exception) {
            showMessage(e.localizedMessage ?: "unknown error")
        }
    }

    private fun uploadTransaksiData() {
        val userId = auth.currentUser?.uid
        val jumlahDisetor = binding.etJumlahDisetor.text.toString().replace(".", "")
        val totalKeseluruhan = binding.etTotalKeseluruhan.text.toString().replace(".", "").toInt()
        val selisih = binding.etSelisih.text.toString().replace(".", "").toInt()
        if (userId != null && jumlahDisetor.isNotBlank()) {
            val docData = hashMapOf(
                JUMLAH_BAKULAN to jumlahBakulan,
                JUMLAH_PAKAI_MEJA to jumlahPakaiMeja,
                JUMLAH_PAKAI_KIOS to jumlahPakaiKios,
                JUMLAH_DISETOR to jumlahDisetor.toInt(),
                TOTAL_KESELURUHAN to totalKeseluruhan,
                SELISIH to selisih,
                USER_ID to userId
            )

            db.collection(TRANSAKSI).add(docData).addOnSuccessListener { doc ->
                viewModel.getAllTransaksi().observe(this@HistoryActivity, { listTransaction ->
                    listTransaction.forEach { transaksi ->
                        val transaksiData = hashMapOf(
                            JENIS_PEDAGANG to transaksi.jenisPedagang,
                            JUMLAH_BAYAR to transaksi.jumlahBayar,
                            TANGGAL to transaksi.tanggal,
                            WAKTU to transaksi.waktu
                        )
                        doc.collection(DETAIL_TRANSAKSI).add(transaksiData)
                    }
                    showMessage("Update data berhasil")
                })
            } .addOnFailureListener {
                showMessage(it.localizedMessage ?: "unknown message")
            }
        } else {
            showMessage("Jumlah disetor harus diisi")
        }
    }

    private fun populateRingkasan() {
        viewModel.countBakulan().observe(this, {
            val tarif = getSharedPreferences(PREF_KEY, MODE_PRIVATE).getInt(BAKULAN, 0)
            val total = it * tarif
            jumlahBakulan = it
            updateTotal(total)

            val tarifText = "Rp${formatPrice(tarif)}"
            val totalText = "Total : Rp${formatPrice(total)}"
            val countText = "Jumlah Transaksi : $it"

            binding.tvBakulanCount.text = countText
            binding.tvBakulanPrice.text = tarifText
            binding.tvBakulanTotal.text = totalText
        })

        viewModel.countPakaiMeja().observe(this, {
            val tarif = getSharedPreferences(PREF_KEY, MODE_PRIVATE).getInt(PAKAI_MEJA, 0)
            val total = it * tarif
            jumlahPakaiMeja = it
            updateTotal(total)

            val tarifText = "Rp${formatPrice(tarif)}"
            val totalText = "Total : Rp${formatPrice(total)}"
            val countText = "Jumlah Transaksi : $it"

            binding.tvPakaiMejaCount.text = countText
            binding.tvPakaiMejaPrice.text = tarifText
            binding.tvPakaiMejaTotal.text = totalText
        })

        viewModel.countPakaiKios().observe(this, {
            val tarif = getSharedPreferences(PREF_KEY, MODE_PRIVATE).getInt(PAKAI_KIOS, 0)
            val total = it * tarif
            jumlahPakaiKios = it
            updateTotal(total)

            val tarifText = "Rp${formatPrice(tarif)}"
            val totalText = "Total : Rp${formatPrice(total)}"
            val countText = "Jumlah Transaksi : $it"

            binding.tvPakaiKiosCount.text = countText
            binding.tvPakaiKiosPrice.text = tarifText
            binding.tvPakaiKiosTotal.text = totalText
        })
    }

    private fun updateTotal(price: Int) {
        val curr = binding.etTotalKeseluruhan.text.toString()
        val currTotal = if (curr.isNotEmpty()) {
            curr.replace(".", "").toInt()
        } else {
            0
        }
        val updatedTotal = currTotal + price
        binding.etTotalKeseluruhan.setText(formatPrice(updatedTotal))
    }

    private fun showMessage(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}