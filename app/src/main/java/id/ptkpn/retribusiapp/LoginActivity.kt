package id.ptkpn.retribusiapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import id.ptkpn.retribusiapp.databinding.ActivityKontribusiPasarBinding
import id.ptkpn.retribusiapp.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val auth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            showLoadingState()
            processLogin()
        }
    }

    private fun processLogin() {
        val username = binding.etUserName.text.toString()
        val password = binding.etPassword.text.toString()
        val email = "$username@ptkpn.id"

        if (username.isNotBlank() && password.isNotBlank()) {
            auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
                intent = Intent(this, MainMenuActivity::class.java)
                startActivity(intent)
            } .addOnFailureListener {
                showErrorMessage(it.localizedMessage ?: "unknown error")
            } .addOnCompleteListener {
                hideLoadingState()
            }
        } else {
            showErrorMessage("username dan password tidak boleh kosong")
        }
    }

    private fun showErrorMessage(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    private fun showLoadingState() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoadingState() {
        binding.progressBar.visibility = View.INVISIBLE
    }
}