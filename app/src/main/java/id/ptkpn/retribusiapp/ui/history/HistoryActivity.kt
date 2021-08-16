package id.ptkpn.retribusiapp.ui.history

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import id.ptkpn.retribusiapp.R
import id.ptkpn.retribusiapp.databinding.ActivityHistoryBinding
import id.ptkpn.retribusiapp.ui.login.LoginActivity
import id.ptkpn.retribusiapp.utils.*
import id.ptkpn.retribusiapp.utils.FileUtils.generateFile
import id.ptkpn.retribusiapp.utils.Utils.formatPrice
import id.ptkpn.retribusiapp.utils.Utils.getCurrentDateTime
import id.ptkpn.retribusiapp.utils.Utils.toString
import net.lingala.zip4j.model.enums.AesKeyStrength
import net.lingala.zip4j.model.enums.CompressionMethod
import net.lingala.zip4j.model.enums.EncryptionMethod
import java.io.File
import java.util.*


class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var viewModel: HistoryViewModel
    private var isExported = false
    private var isReset = false
    private var jumlahBakulan = 0
    private var jumlahPakaiMeja = 0
    private var jumlahPakaiKios = 0
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "History"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

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
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Reset Transaksi")
            builder.setMessage("Anda yakin ingin mereset transaksi?")
            builder.setPositiveButton("Reset") { _, _ ->
                reset()
            }

            builder.setNegativeButton("Batalkan") { _, _ ->
                Toast.makeText(applicationContext, "batalkan print", Toast.LENGTH_SHORT).show()
            }
            builder.show()
        }
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
                val sharedPref =getSharedPreferences(PREF_KEY, MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putString(JURU_TAGIH_NAME, "")
                    putString(JURU_TAGIH_ID, "")
                    putString(TRANSAKSI_ID, "")
                    putBoolean(IS_UPLOADED, false)
                    apply()
                }
                isExported = false
                intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        auth.signOut()
    }

    private fun reset() {
        isReset = true
        binding.etTotalKeseluruhan.setText("")
        viewModel.deleteAllTransaksi()
    }

    private fun getFileName(format: String): String {
        val currDate = getCurrentDateTime().toString("dd_MM_yyyy")
        val userName = getSharedPreferences(PREF_KEY, MODE_PRIVATE).getString(JURU_TAGIH_NAME, "penagih_default_name")
        return "transaksi_${userName}_${currDate}.${format}"
    }

    private fun exportLocalDb() {
        if (getSharedPreferences(PREF_KEY, MODE_PRIVATE).getBoolean(IS_UPLOADED, false)) {
            val tarifBakulan = getSharedPreferences(PREF_KEY, MODE_PRIVATE).getInt(BAKULAN, 0)
            val tarifPakaiMeja = getSharedPreferences(PREF_KEY, MODE_PRIVATE).getInt(PAKAI_MEJA, 0)
            val tarifPakaiKios = getSharedPreferences(PREF_KEY, MODE_PRIVATE).getInt(PAKAI_KIOS, 0)
            val userName = getSharedPreferences(PREF_KEY, MODE_PRIVATE).getString(JURU_TAGIH_NAME, "penagih_default_name")
            val csvFile = generateFile(this, getFileName("csv"))
            if (csvFile != null) {
                viewModel.getAllTransaksi().observe(this@HistoryActivity, { transaksiList ->
                    if (!isReset) {
                        csvWriter().open(csvFile, append = false) {
                            // Header
                            writeRow("sep=,")
                            writeRow(
                                listOf(
                                    "NO",
                                    "TGL",
                                    "JAM",
                                    "USER NAME",
                                    "JENIS PEDAGANG",
                                    "JML BAYAR"
                                )
                            )
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
                            writeRow("*", "Total bakulan", "${tarifBakulan * jumlahBakulan}")
                            writeRow("*", "Total pakai meja", "${tarifPakaiMeja * jumlahPakaiMeja}")
                            writeRow("*", "Total pakai kios", "${tarifPakaiKios * jumlahPakaiKios}")
                            writeRow("*", "Total keseluruhan",
                                "${tarifBakulan * jumlahBakulan 
                                        + tarifPakaiMeja * jumlahPakaiMeja 
                                        + tarifPakaiKios * jumlahPakaiKios}")
                            writeRow("*", "Jumlah disetor",
                                binding.etJumlahDisetor.text.toString().replace(".", "")
                            )
                            writeRow("*", "Selisih",
                                binding.etSelisih.text.toString().replace(".", ""))
                            showMessage("CSV File has been generated")
                            isExported = true
                        }
                        compressAndEncryptFile(csvFile)
                    }
                })
            } else {
                showMessage("CSV not generated")
            }
        } else {
            showMessage("upload data terlebih dahulu")
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
            sendToWhatsapp(zipFile)
        } catch (e: Exception) {
            showMessage(e.localizedMessage ?: "unknown error")
        }
    }

    private fun sendToWhatsapp(zipFile: File) {
        val fileUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                    this,
                    "${packageName}.fileprovider",
                    zipFile
            )
        } else {
            Uri.fromFile(zipFile)
        }
        val number = "6285746156526"
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.type = "application/*"
        sendIntent.putExtra("jid", "$number@s.whatsapp.net")
        sendIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
        sendIntent.setPackage("com.whatsapp")
        sendIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

        startActivity(sendIntent)
    }

    private fun uploadTransaksiData() {
        val userId = getSharedPreferences(PREF_KEY, MODE_PRIVATE).getString(JURU_TAGIH_ID, "penagih_default_id")
        val jumlahDisetor = binding.etJumlahDisetor.text.toString().replace(".", "")
        val totalKeseluruhan = binding.etTotalKeseluruhan.text.toString().replace(".", "").toInt()
        val selisih = binding.etSelisih.text.toString().replace(".", "").toInt()
        if (userId != null && jumlahDisetor.isNotBlank()) {
            showLoadingState()
            val docData = hashMapOf(
                    JUMLAH_BAKULAN to jumlahBakulan,
                    JUMLAH_PAKAI_MEJA to jumlahPakaiMeja,
                    JUMLAH_PAKAI_KIOS to jumlahPakaiKios,
                    JUMLAH_DISETOR to jumlahDisetor.toInt(),
                    TOTAL_KESELURUHAN to totalKeseluruhan,
                    SELISIH to selisih,
                    USER_ID to userId
            )

            if (!getSharedPreferences(PREF_KEY, MODE_PRIVATE).getBoolean(IS_UPLOADED, false)) {
                db.collection(TRANSAKSI).add(docData).addOnSuccessListener { doc ->
                    val sharedPref = getSharedPreferences(PREF_KEY, MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putString(TRANSAKSI_ID, doc.id)
                        apply()
                    }
                    viewModel.getAllTransaksi().observe(this@HistoryActivity, { listTransaction ->
                        if (!isReset) {
                            listTransaction.forEach { transaksi ->
                                val transaksiData = hashMapOf(
                                    JENIS_PEDAGANG to transaksi.jenisPedagang,
                                    JUMLAH_BAYAR to transaksi.jumlahBayar,
                                    TANGGAL to transaksi.tanggal,
                                    WAKTU to transaksi.waktu
                                )
                                doc.collection(DETAIL_TRANSAKSI).add(transaksiData)
                            }
                            showMessage("Upload data berhasil")
                            getSharedPreferences(PREF_KEY, MODE_PRIVATE).edit()
                                .putBoolean(IS_UPLOADED, true)
                                .apply()
                        }
                    })
                } .addOnFailureListener {
                    showMessage(it.localizedMessage ?: "unknown message")
                } .addOnCompleteListener {
                    hideLoadingState()
                }
            } else {
                val transaksiId = getSharedPreferences(PREF_KEY, MODE_PRIVATE).getString(
                    TRANSAKSI_ID, "transaksi_id") ?: "transaksi_id"
                db.collection(TRANSAKSI).document(transaksiId).set(docData).addOnSuccessListener {
                    showMessage("Update data berhasil")
                } .addOnFailureListener {
                    showMessage(it.localizedMessage ?: "unknown error")
                } .addOnCompleteListener {
                    hideLoadingState()
                }
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

    private fun showLoadingState() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoadingState() {
        binding.progressBar.visibility = View.INVISIBLE
    }
}