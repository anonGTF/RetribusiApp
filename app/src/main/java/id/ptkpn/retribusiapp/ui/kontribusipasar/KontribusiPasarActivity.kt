package id.ptkpn.retribusiapp.ui.kontribusipasar

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import id.ptkpn.retribusiapp.databinding.ActivityKontribusiPasarBinding
import id.ptkpn.retribusiapp.localdb.Transaksi
import id.ptkpn.retribusiapp.ui.history.HistoryAuthActivity
import id.ptkpn.retribusiapp.utils.BAKULAN
import id.ptkpn.retribusiapp.utils.PAKAI_KIOS
import id.ptkpn.retribusiapp.utils.PAKAI_MEJA
import id.ptkpn.retribusiapp.utils.Utils.getCurrentDateTime
import id.ptkpn.retribusiapp.utils.Utils.toString
import id.ptkpn.retribusiapp.utils.ViewModelFactory

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
        val transaksi = Transaksi(
            jenisPedagang = type,
            jumlahBayar = 750,
            tanggal = tanggal,
            waktu = waktu
        )
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