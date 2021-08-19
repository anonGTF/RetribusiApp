package id.ptkpn.retribusiapp.ui.mainmenu

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import id.ptkpn.retribusiapp.databinding.ActivityMainMenuBinding
import id.ptkpn.retribusiapp.ui.kontribusipasar.KontribusiPasarActivity
import id.ptkpn.retribusiapp.ui.login.LoginActivity

class MainMenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainMenuBinding
    private val auth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        actionBar?.title = "Main Menu"

        checkLogin()

        binding.menuKebersihanPasar.setOnClickListener {
            intent = Intent(this, KontribusiPasarActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkLogin() {
        if (auth.currentUser == null) {
            intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            Toast.makeText(this,"Anda harus login terlebih dahulu", Toast.LENGTH_LONG).show()
        }
    }
}