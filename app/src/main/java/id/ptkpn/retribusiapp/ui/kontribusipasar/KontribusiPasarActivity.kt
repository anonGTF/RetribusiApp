package id.ptkpn.retribusiapp.ui.kontribusipasar

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
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

        binding.btnHistory.setOnClickListener {
            intent = Intent(this, HistoryAuthActivity::class.java)
            startActivity(intent)
        }
    }

    private fun insertTransaksi(type: String) {
        val tanggal = getCurrentDateTime().toString("dd/MM/yyyy")
        val waktu = getCurrentDateTime().toString("HH:mm:ss")
        val tarif = getSharedPreferences(PREF_KEY, MODE_PRIVATE).getInt(type, 0)
        val transaksi = Transaksi(
            jenisPedagang = type,
            jumlahBayar = tarif,
            tanggal = tanggal,
            waktu = waktu
        )

        Log.d("coba", "insertTransaksi: " +
                "${transaksi.jenisPedagang} ${transaksi.jumlahBayar} " +
                "${transaksi.tanggal} ${transaksi.waktu}")
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