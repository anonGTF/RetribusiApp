package id.ptkpn.retribusiapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import id.ptkpn.retribusiapp.databinding.ActivityKontribusiPasarBinding
import id.ptkpn.retribusiapp.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            intent = Intent(this, MainMenuActivity::class.java)
            startActivity(intent)
        }
    }
}