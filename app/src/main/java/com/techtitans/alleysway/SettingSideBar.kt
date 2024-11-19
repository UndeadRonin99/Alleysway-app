// Package declaration indicating the namespace of the class
package com.techtitans.alleysway

// Importing necessary Android and Firebase libraries
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

/**
 * Activity class for managing user settings, including logout, FAQ access, account deletion,
 * unit of measurement (UOM) settings, and profile viewing/editing.
 *
 * This activity provides a sidebar interface where users can perform various account-related actions.
 */
class SettingSideBar : AppCompatActivity() {
    
    // UI Components
    private lateinit var btnLogout: Button           // Button to handle user logout
    private lateinit var btnFAQ: Button              // Button to access Frequently Asked Questions
    private lateinit var btnDelAccount: Button       // Button to delete the user's account
    private lateinit var btnUOM: Button              // Button to change Units of Measurement
    private lateinit var btnViewP: Button            // Button to view user profile
    private lateinit var btnEditP: Button            // Button to edit user profile
    private lateinit var txtName: TextView           // TextView to display the user's full name

    // Constants
    private val RC_REAUTHENTICATE = 1001             // Request code for reauthentication process

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_side_bar) // Set the layout for this activity

        // Initialize UI components by finding them in the layout
        btnLogout = findViewById(R.id.btnLogout)
        btnFAQ = findViewById(R.id.btnFAQ)
        btnDelAccount = findViewById(R.id.btnDelAccount)
        btnUOM = findViewById(R.id.btnUOM)
        btnViewP = findViewById(R.id.btnViewP)
        txtName = findViewById(R.id.txtName)
        btnEditP = findViewById(R.id.btnEditP)

        // Fetch and display the user's full name
        getFullName { fullName ->
            if (fullName != null) {
                // Log the retrieved full name for debugging purposes
                Log.d("MainActivity", "User's full name: $fullName")
                // Display the full name in the TextView
                txtName.text = fullName
            } else {
                // Handle the case where the full name couldn't be retrieved
                Log.e("MainActivity", "Failed to retrieve user's full name.")
                Toast.makeText(this, "Could not retrieve full name.", Toast.LENGTH_SHORT).show()
            }
        }

        // Set OnClickListener for the Logout button
        btnLogout.setOnClickListener {
            // Sign out the user from Firebase Authentication
            Firebase.auth.signOut()
            // Show a confirmation toast message
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            // Navigate back to the LoginPage activity
            startActivity(Intent(this, LoginPage::class.java))
            // Finish the current activity to prevent the user from returning to it
            finish()
        }

        // Set OnClickListener for the FAQ button
        btnFAQ.setOnClickListener {
            // TODO: Implement FAQ functionality
            // For example, navigate to an FAQ activity or display an FAQ dialog
            Toast.makeText(this, "FAQ feature not implemented yet.", Toast.LENGTH_SHORT).show()
        }

        // Set OnClickListener for the Delete Account button
        btnDelAccount.setOnClickListener {
            // Show a confirmation dialog before deleting the account
            showDeleteAccountDialog()
        }

        // Set OnClickListener for the Units of Measurement (UOM) button
        btnUOM.setOnClickListener {
            // TODO: Implement UOM settings functionality
            // For example, navigate to a settings activity where users can select units
            Toast.makeText(this, "UOM feature not implemented yet.", Toast.LENGTH_SHORT).show()
        }

        // Set OnClickListener for the View Profile button
        btnViewP.setOnClickListener {
            // TODO: Implement View Profile functionality
            // For example, navigate to a profile activity displaying user information
            Toast.makeText(this, "View Profile feature not implemented yet.", Toast.LENGTH_SHORT).show()
        }

        // Set OnClickListener for the Edit Profile button
        btnEditP.setOnClickListener {
            // TODO: Implement Edit Profile functionality
            // For example, navigate to an activity where users can edit their profile details
            Toast.makeText(this, "Edit Profile feature not implemented yet.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Retrieves the full name of the currently authenticated user from Firebase Realtime Database.
     *
     * @param onResult A callback function that receives the full name as a String or null if not found.
     */
    fun getFullName(onResult: (String?) -> Unit) {
        // Get the current authenticated user
        val user = Firebase.auth.currentUser
        if (user != null) {
            val uid = user.uid // Unique identifier for the user
            val database = Firebase.database.reference // Reference to the root of the database

            // Access the user's data in the "users" node using their UID
            val userRef = database.child("users").child(uid)

            // Retrieve the user's data once
            userRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    // Extract first name and last name from the snapshot
                    val firstName = snapshot.child("firstName").getValue(String::class.java) ?: ""
                    val lastName = snapshot.child("lastName").getValue(String::class.java) ?: ""
                    val fullName = "$firstName $lastName" // Concatenate first and last names
                    onResult(fullName) // Return the full name via the callback
                } else {
                    // User data does not exist in the database
                    Log.e("getFullName", "User data not found for UID: $uid")
                    onResult(null) // Return null via the callback
                }
            }.addOnFailureListener { exception ->
                // Handle any errors that occur while fetching data
                Log.e("getFullName", "Error fetching user data: ${exception.message}")
                onResult(null) // Return null via the callback
            }
        } else {
            // No user is currently signed in
            Log.e("getFullName", "No user is signed in.")
            onResult(null) // Return null via the callback
        }
    }

    /**
     * Displays a confirmation dialog prompting the user to confirm account deletion.
     */
    private fun showDeleteAccountDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Account") // Set the title of the dialog
        builder.setMessage("Are you sure you want to delete your account? This action cannot be undone.") // Set the message

        // Set the positive button ("Delete") and its click listener
        builder.setPositiveButton("Delete") { dialog, which ->
            // Proceed to reauthenticate the user before deleting the account
            reauthenticateAndDelete()
        }

        // Set the negative button ("Cancel") and its click listener
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss() // Dismiss the dialog without taking any action
        }

        builder.show() // Display the dialog to the user
    }

    /**
     * Initiates the reauthentication process and deletes the user's account upon successful reauthentication.
     */
    private fun reauthenticateAndDelete() {
        val user = Firebase.auth.currentUser // Get the currently authenticated user
        if (user != null) {
            val providers = user.providerData.map { it.providerId } // List of authentication providers used by the user
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
                    // Unknown authentication provider
                    Toast.makeText(this, "Unknown authentication provider.", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            // No user is signed in
            Toast.makeText(this, "No user is logged in", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Initiates the Google Sign-In flow for reauthentication.
     *
     * This method configures Google Sign-In options and starts the sign-in intent.
     */
    private fun reauthenticateWithGoogle() {
        // Configure Google Sign-In with the required options
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id)) // Request the ID token using the web client ID
            .requestEmail() // Request the user's email
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso) // Create a GoogleSignInClient instance

        // Start the Google Sign-In intent for reauthentication
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_REAUTHENTICATE) // Start the sign-in activity with a request code
    }

    /**
     * Handles the result from the reauthentication sign-in intent.
     *
     * @param requestCode The integer request code originally supplied to startActivityForResult().
     * @param resultCode The integer result code returned by the child activity through its setResult().
     * @param data An Intent, which can return result data to the caller.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_REAUTHENTICATE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data) // Retrieve the sign-in task
            try {
                val account = task.getResult(ApiException::class.java) // Attempt to get the GoogleSignInAccount
                if (account != null) {
                    // Obtain the Google Auth Credential using the ID token
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    // Proceed to reauthenticate with the obtained credential
                    reauthenticateWithCredential(credential)
                } else {
                    // Failed to retrieve Google account information
                    Toast.makeText(this, "Failed to get Google account.", Toast.LENGTH_LONG).show()
                }
            } catch (e: ApiException) {
                // Handle errors that occur during Google Sign-In
                Toast.makeText(this, "Google sign-in failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Displays a dialog prompting the user to enter their password for reauthentication.
     *
     * @param user The currently authenticated FirebaseUser.
     */
    private fun showPasswordDialog(user: FirebaseUser) {
        val passwordInput = EditText(this)
        // Set the input type to password to obscure the entered text
        passwordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        // Build the AlertDialog for password input
        val passwordDialog = AlertDialog.Builder(this)
            .setTitle("Confirm Password") // Set the dialog title
            .setMessage("Please enter your password to confirm account deletion:") // Set the dialog message
            .setView(passwordInput) // Set the custom view (password input field)
            .setPositiveButton("Confirm") { dialog, which ->
                val password = passwordInput.text.toString() // Retrieve the entered password
                if (password.isNotEmpty()) {
                    // Create EmailAuthCredential using the user's email and entered password
                    val credential = EmailAuthProvider.getCredential(user.email!!, password)
                    // Proceed to reauthenticate with the credential
                    reauthenticateWithCredential(credential)
                } else {
                    // Show an error message if the password field is empty
                    Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss() // Dismiss the dialog without taking any action
            }
            .create()

        passwordDialog.show() // Display the dialog to the user
    }

    /**
     * Reauthenticates the user with the provided credential and deletes their account upon successful reauthentication.
     *
     * @param credential The AuthCredential obtained from Google Sign-In or Email/Password reauthentication.
     */
    private fun reauthenticateWithCredential(credential: AuthCredential) {
        val user = Firebase.auth.currentUser // Get the currently authenticated user
        if (user != null) {
            // Reauthenticate the user with the provided credential
            user.reauthenticate(credential)
                .addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {
                        // If reauthentication is successful, proceed to delete user data and account
                        deleteUserDataAndAccount(user)
                    } else {
                        // Handle reauthentication failure
                        Toast.makeText(this, "Reauthentication failed: ${reauthTask.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        } else {
            // Handle the case where no user is currently authenticated
            Toast.makeText(this, "No user is logged in", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Deletes the user's data from Firebase Realtime Database and then deletes their Firebase Authentication account.
     *
     * @param user The currently authenticated FirebaseUser to be deleted.
     */
    private fun deleteUserDataAndAccount(user: FirebaseUser) {
        val userId = user.uid // Get the user's unique identifier
        val database = Firebase.database.reference // Reference to the root of the database

        // Remove the user's data from the "users" node in Firebase Realtime Database
        database.child("users").child(userId).removeValue()
            .addOnCompleteListener { dbTask ->
                if (dbTask.isSuccessful) {
                    // If data deletion is successful, proceed to delete the user's authentication account
                    user.delete()
                        .addOnCompleteListener { deleteTask ->
                            if (deleteTask.isSuccessful) {
                                // Show a success message upon successful account deletion
                                Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show()
                                // Navigate back to the LoginPage activity
                                val intent = Intent(this, LoginPage::class.java)
                                // Clear the activity stack to prevent the user from returning to the previous activities
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent) // Start the LoginPage activity
                                finish() // Finish the current activity
                            } else {
                                // Handle errors that occur during account deletion
                                Toast.makeText(this, "Failed to delete account: ${deleteTask.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    // Handle errors that occur during data deletion
                    Toast.makeText(this, "Failed to delete user data: ${dbTask.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}
