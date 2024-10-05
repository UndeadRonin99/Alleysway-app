package com.example.alleysway

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SettingSideBar : AppCompatActivity() {
    private lateinit var btnLogout: Button
    private lateinit var btnFAQ: Button
    private lateinit var btnDelAccount: Button
    private lateinit var btnUOM: Button
    private lateinit var btnViewP: Button
    private lateinit var btnEditP: Button
    private lateinit var txtName: TextView

    private val RC_REAUTHENTICATE = 1001  // Request code for reauthentication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_side_bar)

        btnLogout = findViewById(R.id.btnLogout)
        btnFAQ = findViewById(R.id.btnFAQ)
        btnDelAccount = findViewById(R.id.btnDelAccount)
        btnUOM = findViewById(R.id.btnUOM)
        btnViewP = findViewById(R.id.btnViewP)
        btnEditP = findViewById(R.id.btnEditP)
        txtName = findViewById(R.id.txtName)

        getFullName { fullName ->
            if (fullName != null) {
                // Use the full name as needed
                Log.d("MainActivity", "User's full name: $fullName")
                // For example, display it in a TextView
                txtName.text = fullName
            } else {
                // Handle the case where the full name couldn't be retrieved
                Log.e("MainActivity", "Failed to retrieve user's full name.")
                Toast.makeText(this, "Could not retrieve full name.", Toast.LENGTH_SHORT).show()
            }
        }

        btnLogout.setOnClickListener {
            // Handle logout button click
            Firebase.auth.signOut()
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginPage::class.java))
            finish()
        }

        btnFAQ.setOnClickListener {
            // Handle FAQ button click
        }

        btnDelAccount.setOnClickListener {
            // Handle delete account button click
            showDeleteAccountDialog()
        }

        btnUOM.setOnClickListener {
            // Handle UOM button click
        }

        btnViewP.setOnClickListener {
            // Handle view profile button click
        }

        btnEditP.setOnClickListener {
            // Handle edit profile button click
        }
    }

    fun getFullName(onResult: (String?) -> Unit) {
        // Get the current authenticated user
        val user = Firebase.auth.currentUser
        if (user != null) {
            val uid = user.uid
            val database = Firebase.database.reference

            // Access the user's data in the database
            val userRef = database.child("users").child(uid)

            userRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val firstName = snapshot.child("firstName").getValue(String::class.java) ?: ""
                    val lastName = snapshot.child("lastName").getValue(String::class.java) ?: ""
                    val fullName = "$firstName $lastName"
                    onResult(fullName)
                } else {
                    // User data does not exist in the database
                    Log.e("getFullName", "User data not found for UID: $uid")
                    onResult(null)
                }
            }.addOnFailureListener { exception ->
                // Handle any errors
                Log.e("getFullName", "Error fetching user data: ${exception.message}")
                onResult(null)
            }
        } else {
            // No user is signed in
            Log.e("getFullName", "No user is signed in.")
            onResult(null)
        }
    }

    // Function to show a confirmation dialog for account deletion
    private fun showDeleteAccountDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Account")
        builder.setMessage("Are you sure you want to delete your account? This action cannot be undone.")

        // If confirmed, proceed to reauthenticate
        builder.setPositiveButton("Delete") { dialog, which ->
            reauthenticateAndDelete()
        }

        // If canceled, dismiss the dialog
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }

        builder.show()  // Display the dialog
    }

    // Function to reauthenticate the user and delete their account
    private fun reauthenticateAndDelete() {
        val user = Firebase.auth.currentUser
        if (user != null) {
            val providers = user.providerData.map { it.providerId }
            when {
                providers.contains(GoogleAuthProvider.PROVIDER_ID) -> {
                    // User signed in with Google, reauthenticate using Google Sign-In
                    reauthenticateWithGoogle()
                }
                providers.contains(EmailAuthProvider.PROVIDER_ID) -> {
                    // User signed in with Email/Password, reauthenticate using password
                    showPasswordDialog(user)
                }
                else -> {
                    Toast.makeText(this, "Unknown authentication provider.", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Toast.makeText(this, "No user is logged in", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to reauthenticate using Google Sign-In
    private fun reauthenticateWithGoogle() {
        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))  // Use your web client ID
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Start sign-in intent for reauthentication
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_REAUTHENTICATE)
    }

    // Handle the result of the reauthentication sign-in intent
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_REAUTHENTICATE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    reauthenticateWithCredential(credential)
                } else {
                    Toast.makeText(this, "Failed to get Google account.", Toast.LENGTH_LONG).show()
                }
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign-in failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Function to show password input dialog for email/password users
    private fun showPasswordDialog(user: FirebaseUser) {
        val passwordInput = EditText(this)
        passwordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD  // Set input type to password

        val passwordDialog = AlertDialog.Builder(this)
            .setTitle("Confirm Password")
            .setMessage("Please enter your password to confirm account deletion:")
            .setView(passwordInput)  // Set password input field in dialog
            .setPositiveButton("Confirm") { dialog, which ->
                val password = passwordInput.text.toString()
                if (password.isNotEmpty()) {
                    val credential = EmailAuthProvider.getCredential(user.email!!, password)
                    reauthenticateWithCredential(credential)
                } else {
                    Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()  // Dismiss dialog if canceled
            }
            .create()

        passwordDialog.show()  // Display the dialog
    }

    // Function to reauthenticate with the provided credential and delete account
    private fun reauthenticateWithCredential(credential: AuthCredential) {
        val user = Firebase.auth.currentUser
        if (user != null) {
            user.reauthenticate(credential)
                .addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {
                        deleteUserDataAndAccount(user)
                    } else {
                        Toast.makeText(this, "Reauthentication failed: ${reauthTask.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        } else {
            Toast.makeText(this, "No user is logged in", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to delete user data and account
    private fun deleteUserDataAndAccount(user: FirebaseUser) {
        val userId = user.uid
        val database = Firebase.database.reference

        // Delete the user's data from the database
        database.child("users").child(userId).removeValue()
            .addOnCompleteListener { dbTask ->
                if (dbTask.isSuccessful) {
                    // Delete the user's account from FirebaseAuth
                    user.delete()
                        .addOnCompleteListener { deleteTask ->
                            if (deleteTask.isSuccessful) {
                                Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, LoginPage::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)  // Return to the login page
                                finish()
                            } else {
                                Toast.makeText(this, "Failed to delete account: ${deleteTask.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Failed to delete user data: ${dbTask.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}
