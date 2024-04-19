package com.example.logindemo

import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Create UI programmatically
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)

            val emailEditText = EditText(context).apply {
                hint = "Email"
            }
            addView(emailEditText)

            val passwordEditText = EditText(context).apply {
                hint = "Password"
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            addView(passwordEditText)

            val usernameEditText = EditText(context).apply {
                hint = "Username"
            }
            addView(usernameEditText)

            val fullNameEditText = EditText(context).apply {
                hint = "Full Name"
            }
            addView(fullNameEditText)

            val registerButton = Button(context).apply {
                text = "Register"
                setOnClickListener {
                    val email = emailEditText.text.toString()
                    val password = passwordEditText.text.toString()
                    val username = usernameEditText.text.toString()
                    val fullName = fullNameEditText.text.toString()
                    registerUser(email, password, username, fullName)
                }
            }
            addView(registerButton)

            val loginButton = Button(context).apply {
                text = "Login"
                setOnClickListener {
                    val email = emailEditText.text.toString()
                    val password = passwordEditText.text.toString()
                    loginUser(email, password)
                }
            }
            addView(loginButton)
        }

        setContentView(layout)
    }

    private fun registerUser(email: String, password: String, username: String, fullName: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    // Update additional user data
                    firebaseUser?.let {
                        val userProfileChangeRequest = UserProfileChangeRequest.Builder()
                            .setDisplayName(username)
                            .build()
                        it.updateProfile(userProfileChangeRequest)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // Additional user data updated successfully
                                    // Store additional user data in Firestore
                                    val user = hashMapOf(
                                        "username" to username,
                                        "fullName" to fullName
                                    )
                                    FirebaseFirestore.getInstance().collection("users")
                                        .document(it.uid)
                                        .set(user)
                                        .addOnSuccessListener {
                                            // User data stored successfully
                                            Toast.makeText(this@MainActivity, "Registration successful", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { e ->
                                            // Handle error
                                            Toast.makeText(this@MainActivity, "Failed to store user data: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                } else {
                                    // Handle error
                                    Toast.makeText(this@MainActivity, "Failed to update user data: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                } else {
                    // Registration failed
                    Toast.makeText(this@MainActivity, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login success
                    Toast.makeText(this@MainActivity, "Login successful", Toast.LENGTH_SHORT).show()
                } else {
                    // Login failed
                    Toast.makeText(this@MainActivity, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}
