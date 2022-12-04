package com.udacity.project4.authentication

import android.app.Application
import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.udacity.project4.base.BaseViewModel

class LoginViewModel(application: Application) : BaseViewModel(application) {

    val firebaseUser = object : LiveData<FirebaseUser>() {

        private val firebaseAuth = FirebaseAuth.getInstance()

        private val authStateListener = FirebaseAuth.AuthStateListener {
            value = it.currentUser
        }

        override fun onActive() {
            firebaseAuth.addAuthStateListener(authStateListener)
        }

        override fun onInactive() {
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }
}

