package com.example.alleysway

import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class EditProfile : AppCompatActivity() {
    private lateinit var imgProfile: ImageView
    private lateinit var txtName: EditText
    private lateinit var txtEmail: EditText
    private lateinit var txtUsername: EditText
    private lateinit var imgPencil2: ImageView
    private lateinit var imgEditPFP: ImageView
    private lateinit var btnSave: Button
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var btnBack: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                uploadProfileImage(uri)
            }
        }

        imgProfile = findViewById(R.id.imgPFP)
        txtName = findViewById(R.id.txtName)
        txtEmail = findViewById(R.id.txtEmail)
        txtUsername = findViewById(R.id.txtUsername)
        imgPencil2 = findViewById(R.id.image_pencil_simple_name) //username
        imgEditPFP = findViewById(R.id.image_add_photo) // PFP edit icon
        btnSave = findViewById(R.id.btnSave) // Save button
        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener(){
            finish()
        }

        // Initialize image picker launcher
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                uploadProfileImage(uri)
            }
        }

        // Set click listeners
        imgEditPFP.setOnClickListener {
            openImagePicker()
        }

        loadProfileImage()

        // Fetch and display the user's full name in the drawer
        getFullName { fullName ->
            if (fullName != null) {
                txtName.setText(fullName)
            } else {
                txtUsername.setText("Guest")
            }
        }

        getUsername { username ->
            if (username != null) {
                txtUsername.setText(username)
            } else {
                txtUsername.setText("Guest")
            }
        }

        getEmail { email ->
            if (email != null) {
                txtEmail.setText(email)
            } else {
                txtUsername.setText("Guest")
            }
        }

        imgPencil2.setOnClickListener(){
            txtUsername.requestFocus()
        }

        btnSave.setOnClickListener {
            saveUserData()
        }
    }

    // Function to open image picker
    private fun openImagePicker() {
        imagePickerLauncher.launch("image/*")
    }

    // Function to upload profile image
    private fun uploadProfileImage(uri: Uri) {
        val user = Firebase.auth.currentUser
        if (user != null) {
            val uid = user.uid
            val storageRef = Firebase.storage.reference
            val profileImagesRef = storageRef.child("users/$uid/profile-image.jpeg")

            val uploadTask = profileImagesRef.putFile(uri)
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                profileImagesRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    // Update profileImageUrl in RTDB
                    val database = Firebase.database.reference
                    val userRef = database.child("users").child(uid)
                    userRef.child("profileImageUrl").setValue(downloadUri.toString())
                        .addOnSuccessListener {
                            // Update UI
                            Glide.with(this@EditProfile)
                                .load(downloadUri)
                                .apply(RequestOptions.circleCropTransform())
                                .placeholder(R.drawable.dft)
                                .error(R.drawable.dft)
                                .into(imgProfile)
                            Toast.makeText(this, "Profile image updated", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { exception ->
                            Log.e("uploadProfileImage", "Error updating profileImageUrl: ${exception.message}")
                            Toast.makeText(this, "Failed to update profile image", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    task.exception?.let {
                        Log.e("uploadProfileImage", "Error uploading image: ${it.message}")
                        Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Log.e("uploadProfileImage", "No user is signed in.")
            Toast.makeText(this, "No user is signed in", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to save user data
    private fun saveUserData() {
        val user = Firebase.auth.currentUser
        if (user != null) {
            val uid = user.uid
            val database = Firebase.database.reference
            val userRef = database.child("users").child(uid)

            val updatedUsername = txtUsername.text.toString()
            val updatedName = txtName.text.toString()

            // Split the name into first and last name
            val nameParts = updatedName.split(" ")
            val firstName = nameParts.getOrNull(0) ?: ""
            val lastName = nameParts.getOrNull(1) ?: ""

            val updates = mapOf<String, Any>(
                "username" to updatedUsername,
                "firstName" to firstName,
                "lastName" to lastName
            )

            userRef.updateChildren(updates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Log.e("saveUserData", "Error updating data: ${exception.message}")
                    Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
        } else {
            Log.e("saveUserData", "No user is signed in.")
            Toast.makeText(this, "No user is signed in", Toast.LENGTH_SHORT).show()
        }
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
                        Glide.with(this@EditProfile)
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

    // Function to retrieve the user's email
    private fun getEmail(onResult: (String?) -> Unit) {
        val user = Firebase.auth.currentUser
        if (user != null) {
            val uid = user.uid
            val database = Firebase.database.reference

            val userRef = database.child("users").child(uid)

            userRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val email = snapshot.child("email").getValue(String::class.java) ?: ""
                    onResult(email)
                } else {
                    Log.e("getEmail", "User data not found for UID: $uid")
                    onResult(null)
                }
            }.addOnFailureListener { exception ->
                Log.e("getEmail", "Error fetching user data: ${exception.message}")
                onResult(null)
            }
        } else {
            Log.e("getEmail", "No user is signed in.")
            onResult(null)
        }
    }

    // Function to retrieve the user's username
    private fun getUsername(onResult: (String?) -> Unit) {
        val user = Firebase.auth.currentUser
        if (user != null) {
            val uid = user.uid
            val database = Firebase.database.reference

            val userRef = database.child("users").child(uid)

            userRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val username = snapshot.child("username").getValue(String::class.java) ?: ""

                    onResult(username)
                } else {
                    Log.e("getUsername", "User data not found for UID: $uid")
                    onResult(null)
                }
            }.addOnFailureListener { exception ->
                Log.e("getUsername", "Error fetching user data: ${exception.message}")
                onResult(null)
            }
        } else {
            Log.e("getUsername", "No user is signed in.")
            onResult(null)
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
}