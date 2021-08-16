package id.ptkpn.retribusiapp.ui.history

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import id.ptkpn.retribusiapp.databinding.ActivityHistoryAuthBinding
import id.ptkpn.retribusiapp.ui.login.LoginActivity
import id.ptkpn.retribusiapp.utils.*

class HistoryAuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryAuthBinding
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "History"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.btnLanjut.setOnClickListener {
            login()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun login() {
        val juruTagihName = auth.currentUser?.email?.split("@")?.get(0) ?: "penagih_default_name"
        val juruTagihUid = auth.currentUser?.uid ?: "0"
        val email = "${binding.etUsername.text}@ptkpn.id"
        val password = binding.etPassword2.text.toString()

        binding.progressBar.visibility = View.VISIBLE

        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
            if (it.user != null) {
                db.collection(USERS).document(it.user!!.uid).get().addOnSuccessListener { doc ->
                    val role = doc.data?.get(ROLE).toString()
                    if (role == SUPERVISOR || role == ADMIN) {
                        val sharedPref = getSharedPreferences(PREF_KEY, MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putString(JURU_TAGIH_NAME, juruTagihName)
                            putString(JURU_TAGIH_ID, juruTagihUid)
                            apply()
                        }
                        intent = Intent(this, HistoryActivity::class.java)
                        startActivity(intent)
                    } else {
                        auth.signOut()
                        showMessage("Fitur history hanya untuk admin atau supervisor")
                        intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
        } .addOnFailureListener {
            showMessage(it.localizedMessage ?: "unknown error")
        } .addOnCompleteListener {
            binding.progressBar.visibility = View.INVISIBLE
        }
    }

    private fun showMessage(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}