package com.terryreed.swipelist

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProfileActivity : BaseActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var dbHelper: UserDatabaseHelper

    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                dbHelper.insertUser(account.email ?: "", account.id)
                findViewById<TextView>(R.id.txtStatus).text =
                    getString(R.string.signed_in_as, account.displayName)
            } catch (e: ApiException) {
                Toast.makeText(this, "Sign-in failed", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Sign-in failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getLayoutResId(): Int = R.layout.activity_profile

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findViewById<BottomNavigationView>(R.id.bottomNav).selectedItemId = R.id.nav_profile

        dbHelper = UserDatabaseHelper(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val googleButton: Button = findViewById(R.id.googleSignInButton)
        googleButton.setOnClickListener {
            val intent = googleSignInClient.signInIntent
            signInLauncher.launch(intent)
        }

        val loginButton: Button = findViewById(R.id.loginButton)
        val createButton: Button = findViewById(R.id.createAccountButton)
        val usernameField: EditText = findViewById(R.id.usernameField)
        val passwordField: EditText = findViewById(R.id.passwordField)
        val status: TextView = findViewById(R.id.txtStatus)

        loginButton.setOnClickListener {
            val username = usernameField.text.toString()
            val password = passwordField.text.toString()
            if (dbHelper.verifyUser(username, password)) {
                status.text = getString(R.string.signed_in_as, username)
            } else {
                Toast.makeText(this, R.string.login_failed, Toast.LENGTH_SHORT).show()
            }
        }

        createButton.setOnClickListener {
            val username = usernameField.text.toString()
            val password = passwordField.text.toString()
            if (dbHelper.insertUser(username, password)) {
                status.text = getString(R.string.signed_in_as, username)
                Toast.makeText(this, R.string.user_created, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, R.string.account_creation_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }
}

