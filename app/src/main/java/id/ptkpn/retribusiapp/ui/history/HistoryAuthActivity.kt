package id.ptkpn.retribusiapp.ui.history

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import id.ptkpn.retribusiapp.databinding.ActivityHistoryAuthBinding

class HistoryAuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLanjut.setOnClickListener {
            intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }
    }
}