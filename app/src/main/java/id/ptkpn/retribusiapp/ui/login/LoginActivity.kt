package id.ptkpn.retribusiapp.ui.login

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import id.ptkpn.retribusiapp.databinding.ActivityLoginBinding
import id.ptkpn.retribusiapp.ui.mainmenu.MainMenuActivity
import id.ptkpn.retribusiapp.utils.JENIS_PEDAGANG
import id.ptkpn.retribusiapp.utils.NAMA_JENIS
import id.ptkpn.retribusiapp.utils.PREF_KEY
import id.ptkpn.retribusiapp.utils.TARIF

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val auth = Firebase.auth
    private val db = Firebase.firestore

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
                getNewewstPrice()
            } .addOnFailureListener {
                showErrorMessage(it.localizedMessage ?: "unknown error")
            } .addOnCompleteListener {
                hideLoadingState()
            }
        } else {
            showErrorMessage("username dan password tidak boleh kosong")
        }
    }

    private fun getNewewstPrice() {
        db.collection(JENIS_PEDAGANG).get().addOnSuccessListener {
            val sharedPref = getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE)
            it.documents.forEach { doc ->
                val namaJenis = doc.data?.get(NAMA_JENIS).toString()
                val tarif = doc.data?.get(TARIF).toString().toInt()

                with(sharedPref.edit()) {
                    putInt(namaJenis, tarif)
                    apply()
                }
            }

            intent = Intent(this, MainMenuActivity::class.java)
            startActivity(intent)
        } .addOnFailureListener {
            showErrorMessage(it.localizedMessage ?: "unknown error")
        } .addOnCompleteListener {
            hideLoadingState()
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