package id.ptkpn.retribusiapp.ui.history

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import id.ptkpn.retribusiapp.databinding.ActivityHistoryBinding
import id.ptkpn.retribusiapp.utils.*
import id.ptkpn.retribusiapp.utils.Utils.formatPrice
import java.text.NumberFormat
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

        binding.etJumlahDisetor.addTextChangedListener(MyTextWatcher(binding.etSelisih, binding.etTotalKeseluruhan))

        binding.btnUpdate.setOnClickListener {
            uploadTransaksiData()
        }

        binding.btnReset.setOnClickListener {
            binding.etTotalKeseluruhan.setText("")
            viewModel.deleteAllTransaksi()
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