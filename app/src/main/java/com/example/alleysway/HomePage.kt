package com.example.alleysway

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class HomePage : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var txtWelcome: TextView
    private lateinit var btnAttendance: ImageView

    // Drawer (Settings Page) UI Elements
    private lateinit var imgProfile: ImageView
    private lateinit var imgCamera: ImageView
    private lateinit var txtName: TextView
    private lateinit var btnViewP: Button
    private lateinit var btnUOM: Button
    private lateinit var btnDelAccount: Button
    private lateinit var btnFAQ: Button
    private lateinit var btnLogout: Button

    private val RC_REAUTHENTICATE = 1001  // Request code for reauthentication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)
        enableEdgeToEdge()

        txtWelcome = findViewById(R.id.txtWelcome)
        getFullName { fullName ->
            if (fullName != null) {
                Log.d("HomePage", "User's full name: $fullName")
                txtWelcome.text = "Welcome back $fullName"
            } else {
                Log.e("HomePage", "Failed to retrieve user's full name.")
                Toast.makeText(this, "Could not retrieve full name.", Toast.LENGTH_SHORT).show()
            }
        }
        // Inside onCreate method
        val btnBooking: ImageView = findViewById(R.id.btnBooking)
        btnBooking.setOnClickListener {
            // Navigate to the Bookings activity
            val intent = Intent(this, Bookings::class.java)
            startActivity(intent)
        }



        btnAttendance = findViewById(R.id.imageView7)
        imgCamera = findViewById(R.id.btnCamera)
        btnAttendance.setOnClickListener {
            val intent = Intent(this, ScanQRCode::class.java)
            startActivity(intent)
        }

        btnAttendance.setOnClickListener {
            val intent = Intent(this, ScanQRCode::class.java)
            startActivity(intent)
        }

        // Initialize DrawerLayout and settings button
        drawerLayout = findViewById(R.id.drawer_layout)
        val btnSettings: ImageView = findViewById(R.id.btnSettings)
        imgProfile = findViewById(R.id.imgProfile)

        btnSettings.setOnClickListener {
            if (!drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.openDrawer(GravityCompat.END)
            }
        }

        // Initialize UI elements inside the drawer
        imgProfile = findViewById(R.id.imgProfile)
        txtName = findViewById(R.id.txtName)
        btnViewP = findViewById(R.id.btnViewP)
        btnUOM = findViewById(R.id.btnUOM)
        btnDelAccount = findViewById(R.id.btnDelAccount)
        btnFAQ = findViewById(R.id.btnFAQ)
        btnLogout = findViewById(R.id.btnLogout)

        loadProfileImage()

        // Fetch and display the user's full name in the drawer
        getFullName { fullName ->
            if (fullName != null) {
                txtName.text = fullName
            } else {
                txtName.text = "Guest"
            }
        }

        // Set click listeners for drawer buttons
        btnLogout.setOnClickListener {
            Firebase.auth.signOut()
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginPage::class.java))
            finish()
        }

        btnFAQ.setOnClickListener {
            // Handle FAQ button click
            Toast.makeText(this, "FAQ clicked", Toast.LENGTH_SHORT).show()
            // Implement FAQ functionality
        }

        btnDelAccount.setOnClickListener {
            // Handle delete account button click
            showDeleteAccountDialog()
        }

        btnUOM.setOnClickListener {
            // Handle Unit of Measurement button click
            Toast.makeText(this, "Unit of Measurement clicked", Toast.LENGTH_SHORT).show()
            // Implement UOM functionality
        }

        btnViewP.setOnClickListener {
            // Handle view profile button click
            startActivity(Intent(this, EditProfile::class.java))
        }



        // Close the drawer when clicking outside
        drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerClosed(drawerView: android.view.View) {
                super.onDrawerClosed(drawerView)
            }
        })
    }

    private fun loadProfileImage() {
        val user = Firebase.auth.currentUser
        if (user != null) {
            val uid = user.uid
            val database = Firebase.database.reference

            val userRef = database.child("users").child(uid)

            userRef.child("profileImageUrl").addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val profileImageUrl = snapshot.getValue(String::class.java)
                    if (!profileImageUrl.isNullOrEmpty()) {
                        // Load the image using Glide
                        Glide.with(this@HomePage)
                            .load(profileImageUrl)
                            .apply(RequestOptions.circleCropTransform())
                            .placeholder(R.drawable.dft) // Optional placeholder image
                            .error(R.drawable.dft) // Optional error image
                            .into(imgProfile)
                    } else {
                        Log.e("loadProfileImage", "Profile image URL is empty.")
                        // Optionally set a default image or handle the case where URL is empty
                        imgProfile.setImageResource(R.drawable.dft)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("loadProfileImage", "Error fetching profile image URL: ${error.message}")
                    // Handle the error
                }
            })
        } else {
            Log.e("loadProfileImage", "No user is signed in.")
            // Handle the case where no user is signed in
        }
    }

    // Function to retrieve the user's full name
    private fun getFullName(onResult: (String?) -> Unit) {
        val user = Firebase.auth.currentUser
        if (user != null) {
            val uid = user.uid
            val database = Firebase.database.reference

            val userRef = database.child("users").child(uid)

            userRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val firstName = snapshot.child("firstName").getValue(String::class.java) ?: ""
                    val lastName = snapshot.child("lastName").getValue(String::class.java) ?: ""
                    val fullName = "$firstName $lastName"
                    onResult(fullName)
                } else {
                    Log.e("getFullName", "User data not found for UID: $uid")
                    onResult(null)
                }
            }.addOnFailureListener { exception ->
                Log.e("getFullName", "Error fetching user data: ${exception.message}")
                onResult(null)
            }
        } else {
            Log.e("getFullName", "No user is signed in.")
            onResult(null)
        }
    }

    // Handle back button press to close the drawer if it's open
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            super.onBackPressed()
        }
    }

    // Function to show a confirmation dialog for account deletion
    private fun showDeleteAccountDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Account")
        builder.setMessage("Are you sure you want to delete your account? This action cannot be undone.")
        builder.setPositiveButton("Delete") { dialog, which ->
            reauthenticateAndDelete()
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }
        builder.show()
    }

    // Function to reauthenticate the user and delete their account
    private fun reauthenticateAndDelete() {
        val user = Firebase.auth.currentUser
        if (user != null) {
            val providers = user.providerData.map { it.providerId }
            when {
                providers.contains(GoogleAuthProvider.PROVIDER_ID) -> {
                    // Reauthenticate with Google
                    reauthenticateWithGoogle()
                }
                providers.contains(EmailAuthProvider.PROVIDER_ID) -> {
                    // Reauthenticate with Email/Password
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

    // Reauthenticate using Google Sign-In
    private fun reauthenticateWithGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
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

    // Show password input dialog for email/password users
    private fun showPasswordDialog(user: FirebaseUser) {
        val passwordInput = EditText(this)
        passwordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        val passwordDialog = AlertDialog.Builder(this)
            .setTitle("Confirm Password")
            .setMessage("Please enter your password to confirm account deletion:")
            .setView(passwordInput)
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
                dialog.dismiss()
            }
            .create()

        passwordDialog.show()
    }

    // Reauthenticate with the provided credential and delete account
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

    // Delete user data and account
    private fun deleteUserDataAndAccount(user: FirebaseUser) {
        val userId = user.uid
        val database = Firebase.database.reference

        database.child("users").child(userId).removeValue()
            .addOnCompleteListener { dbTask ->
                if (dbTask.isSuccessful) {
                    user.delete()
                        .addOnCompleteListener { deleteTask ->
                            if (deleteTask.isSuccessful) {
                                Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, LoginPage::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
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
