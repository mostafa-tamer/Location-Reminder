package com.udacity.project4.locationreminders

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.authentication.LoginViewModel
import com.udacity.project4.databinding.ActivityRemindersBinding


/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {

    private val loginViewModel: LoginViewModel by viewModels()
    lateinit var binding: ActivityRemindersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRemindersBinding.inflate(layoutInflater)

        observeAuthentication()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun observeAuthentication() {

        loginViewModel.firebaseUser.observe(this, Observer {
            if (it == null) {
                startActivity(Intent(applicationContext, AuthenticationActivity::class.java))
            } else {
                Toast.makeText(
                    this,
                    "Welcome ${it.displayName}",
                    Toast.LENGTH_SHORT
                ).show()
                setContentView(binding.root)
            }
        })
    }
}
