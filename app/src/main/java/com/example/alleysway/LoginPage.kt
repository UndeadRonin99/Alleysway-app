package com.example.alleysway

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class LoginPage : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val RC_SIGN_IN = 9001
    private lateinit var signIn: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_page)
        enableEdgeToEdge()

        // Initialize Firebase Auth
        auth = Firebase.auth

        signIn = findViewById(R.id.imageView4)
        signIn.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id)) // Use value from strings.xml
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Start sign-in intent
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w("LoginPage", "Google sign in failed", e)
                Toast.makeText(this, "Google sign in failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        processUser(it)
                    }
                } else {
                    Log.w("LoginPage", "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }


    private fun processUser(user: FirebaseUser) {
        val uid = user.uid
        val email = user.email ?: ""
        val firstName = user.displayName?.split(" ")?.firstOrNull() ?: ""
        val lastName = user.displayName?.split(" ")?.getOrNull(1) ?: ""
        val encodedEmail = email.replace('.', ',')
        val database = Firebase.database.reference

        // Check for pending user setup
        val pendingUserRef = database.child("pending_users").child(encodedEmail)
        pendingUserRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val role = snapshot.child("role").value as? String ?: "client"
                val rate = snapshot.child("ptRate").value as? String ?: ""

                // Finalize user setup
                val finalData = mapOf(
                    "firstName" to firstName,
                    "lastName" to lastName,
                    "role" to role,
                    "rate" to rate,
                    "email" to email
                )
                val userRef = database.child("users").child(uid)
                userRef.setValue(finalData).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Remove from pending users
                        pendingUserRef.removeValue()
                        redirectToDashboard(role)
                    } else {
                        Log.e(TAG, "Failed to finalize user setup: ${task.exception}")
                    }
                }
            } else {
                // Check if user exists in 'users' node
                val userRef = database.child("users").child(uid)
                userRef.get().addOnSuccessListener { userSnapshot ->
                    if (userSnapshot.exists()) {
                        // User exists, update name and email
                        val updates = mapOf(
                            "firstName" to firstName,
                            "lastName" to lastName,
                            "email" to email
                        )
                        userRef.updateChildren(updates).addOnCompleteListener {
                            val role = userSnapshot.child("role").value as? String ?: "client"
                            redirectToDashboard(role)
                        }
                    } else {
                        // Create new user with default role 'client'
                        val role = "client"
                        val userData = mapOf(
                            "firstName" to firstName,
                            "lastName" to lastName,
                            "role" to role,
                            "email" to email
                        )
                        userRef.setValue(userData).addOnCompleteListener {
                            redirectToDashboard(role)
                        }
                    }
                }.addOnFailureListener {
                    Log.e(TAG, "Failed to check user existence: ${it.message}")
                }
            }
        }.addOnFailureListener {
            Log.e(TAG, "Failed to check pending user: ${it.message}")
        }
    }

    private fun redirectToDashboard(role: String) {
        when (role) {
            "admin" -> {
                // Navigate to Admin Dashboard Activity
                val intent = Intent(this, HomePage::class.java)
                Toast.makeText(this,"Signed in as Admin", Toast.LENGTH_SHORT).show()
                startActivity(intent)
                finish()
            }
            "client" -> {
                // Navigate to Client Dashboard Activity
                val intent = Intent(this, HomePage::class.java)
                Toast.makeText(this,"Signed in as Client", Toast.LENGTH_SHORT).show()
                startActivity(intent)
                finish()
            }
            else -> {
                // Handle undefined roles
                Log.e(TAG, "Undefined user role: $role")
                Toast.makeText(this, "User role is not defined properly.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
