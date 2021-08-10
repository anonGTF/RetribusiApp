package id.ptkpn.retribusiapp.ui.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import id.ptkpn.retribusiapp.ui.login.LoginActivity
import id.ptkpn.retribusiapp.ui.mainmenu.MainMenuActivity

class SplashActivity : AppCompatActivity() {

     private val auth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        this.recreate()
        intent = if (auth.currentUser != null) {
            Intent(this, MainMenuActivity::class.java)
        } else {
            Intent(this, LoginActivity::class.java)
        }
        startActivity(intent)
        finish()
    }
}