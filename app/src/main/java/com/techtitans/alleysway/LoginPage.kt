// File: app/src/main/java/com/example/alleysway/LoginPage.kt
package com.techtitans.alleysway

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

/**
 * Activity for handling user login via Google Sign-In.
 */
class LoginPage : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth // Firebase Authentication instance
    private val RC_SIGN_IN = 9001 // Request code for Google Sign-In
    private lateinit var signIn: ImageView // ImageView acting as the sign-in button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_page)
        enableEdgeToEdge() // Enable edge-to-edge display for the activity

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Find the sign-in button in the layout
        signIn = findViewById(R.id.imageView4)
        signIn.setOnClickListener {
            signIn() // Trigger sign-in process when button is clicked
        }
    }

    /**
     * Configures Google Sign-In and starts the sign-in intent.
     */
    private fun signIn() {
        // Configure Google Sign-In options
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id)) // Request ID token
            .requestEmail() // Request user's email
            .requestScopes(Scope("https://www.googleapis.com/auth/calendar")) // Request calendar scope
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Start the Google Sign-In intent
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    /**
     * Handles the result from the Google Sign-In intent.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val accessToken = account.serverAuthCode
                firebaseAuthWithGoogle(account.idToken!!) // Authenticate with Firebase using Google ID token
            } catch (e: ApiException) {
                Log.w("LoginPage", "Google sign in failed", e)
                Toast.makeText(this, "Google sign in failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Authenticates the user with Firebase using the Google ID token.
     *
     * @param idToken The ID token from Google Sign-In.
     */
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        processUser(it) // Process the authenticated user
                    }
                } else {
                    Log.w("LoginPage", "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    /**
     * Processes the authenticated Firebase user by setting up their profile in the database.
     *
     * @param user The authenticated FirebaseUser.
     */
    private fun processUser(user: FirebaseUser) {
        val uid = user.uid
        val email = user.email ?: ""
        val firstName = user.displayName?.split(" ")?.firstOrNull() ?: ""
        val lastName = user.displayName?.split(" ")?.getOrNull(1) ?: ""
        val encodedEmail = email.replace('.', ',') // Encode email to use as a key
        val database = Firebase.database.reference

        // Reference to the pending_users node for the encoded email
        val pendingUserRef = database.child("pending_users").child(encodedEmail)
        pendingUserRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val role = snapshot.child("role").value as? String ?: "client"
                val rate = snapshot.child("ptRate").value as? String ?: ""

                // Prepare final user data
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
                        pendingUserRef.removeValue() // Remove user from pending_users
                        redirectToDashboard(role) // Navigate to the appropriate dashboard
                    } else {
                        Log.e(TAG, "Failed to finalize user setup: ${task.exception}")
                    }
                }
            } else {
                // Check if user already exists in the users node
                val userRef = database.child("users").child(uid)
                userRef.get().addOnSuccessListener { userSnapshot ->
                    if (userSnapshot.exists()) {
                        // Update existing user details
                        val updates = mapOf(
                            "firstName" to firstName,
                            "lastName" to lastName,
                            "email" to email
                        )
                        userRef.updateChildren(updates).addOnCompleteListener {
                            val role = userSnapshot.child("role").value as? String ?: "client"
                            redirectToDashboard(role) // Navigate to the appropriate dashboard
                        }
                    } else {
                        // Create a new user entry with default role 'client'
                        val role = "client"
                        val userData = mapOf(
                            "firstName" to firstName,
                            "lastName" to lastName,
                            "role" to role,
                            "email" to email
                        )
                        userRef.setValue(userData).addOnCompleteListener {
                            redirectToDashboard(role) // Navigate to the appropriate dashboard
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

    /**
     * Redirects the user to the appropriate dashboard based on their role.
     *
     * @param role The role of the user (e.g., "admin", "client").
     */
    private fun redirectToDashboard(role: String) {
        when (role) {
            "admin" -> {
                // Navigate to Admin Dashboard
                val intent = Intent(this, HomePage::class.java)
                Toast.makeText(this, "Signed in as Admin", Toast.LENGTH_SHORT).show()
                startActivity(intent)
                finish()
            }
            "client" -> {
                // Navigate to Client Dashboard
                val intent = Intent(this, HomePage::class.java)
                Toast.makeText(this, "Signed in as Client", Toast.LENGTH_SHORT).show()
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
