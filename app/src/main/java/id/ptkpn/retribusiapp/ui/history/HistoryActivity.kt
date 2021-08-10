package id.ptkpn.retribusiapp.ui.history

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import id.ptkpn.retribusiapp.R
import id.ptkpn.retribusiapp.databinding.ActivityHistoryBinding
import id.ptkpn.retribusiapp.utils.ViewModelFactory

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var viewModel: HistoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory = ViewModelFactory.getInstance(this)
        viewModel = ViewModelProvider(this, factory).get(HistoryViewModel::class.java)

        binding.btnReset.setOnClickListener {
            viewModel.deleteAllTransaksi()
        }
    }
}