package id.ptkpn.retribusiapp.ui.mainmenu

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import id.ptkpn.retribusiapp.databinding.ActivityMainMenuBinding
import id.ptkpn.retribusiapp.ui.kontribusipasar.KontribusiPasarActivity

class MainMenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.menuKebersihanPasar.setOnClickListener {
            intent = Intent(this, KontribusiPasarActivity::class.java)
            startActivity(intent)
        }
    }
}