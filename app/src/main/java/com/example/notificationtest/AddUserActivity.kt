package com.example.notificationtest

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class AddUserActivity : AppCompatActivity() {

    private lateinit var userEmail: EditText
    private lateinit var userName: EditText
    private lateinit var userRegisterButton: AppCompatButton
    private val dataSavedFirebaseFireStore = FirebaseFirestore.getInstance()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_login)

        userEmail = findViewById(R.id.enter_email)
        userName = findViewById(R.id.enter_name)
        userRegisterButton = findViewById(R.id.register_user)

        Firebase.messaging.isAutoInitEnabled = true

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            FirebaseMessaging.getInstance().token.addOnSuccessListener {
                Log.d("token", it)
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                10
            )
        }

        userRegisterButton.setOnClickListener {
            FirebaseMessaging.getInstance().token.addOnSuccessListener {
                getUserData(it)
            }
        }
    }

    private fun getUserData(token: String) {
        val userRegisterEmail = userEmail.text.toString()
        val userRegisterName = userName.text.toString()

        if (userRegisterEmail.isBlank() || userRegisterName.isBlank()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = UUID.randomUUID().toString()
        val userData = DataModel(uid, userRegisterEmail, userRegisterName, token)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                dataSavedFirebaseFireStore.collection("users").document(uid).set(userData)
                    .addOnSuccessListener {
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(
                                this@AddUserActivity,
                                "Registration successful",
                                Toast.LENGTH_SHORT
                            ).show()
                            startActivity(Intent(this@AddUserActivity, MainActivity::class.java))
                            sendNotification(token, userRegisterName)
                            finish()
                        }
                    }
                    .addOnFailureListener { e ->
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(
                                this@AddUserActivity,
                                "Registration failed: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddUserActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun sendNotification(token: String, name: String) {
        val updateItem = SendNotificationDelete(
            Data(name, "userName"),
            Notification("Campaign messages", name, true, "Add Data"),
            arrayListOf(token)
        )

        val hashMap = HashMap<String, String>()
        hashMap["Authorization"] = "key=AAAAqM28o0k:APA91bG9UtAZnTZvqRi4_TfId0Q7v0rAzKJvAdGO3N4ZLmlG-UxsHkd01UfmWE4ddjzEtq6Y_xw2I3-ZXkEJSjssUje6jeFH8xzQFm001bZiqvVvsKmSQGRKbAY21YQMVjB89Ty689q9"

        SendNotification.createNotification().deleteNotification(hashMap, updateItem)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                Toast.makeText(this, "Add Data Notification sent", Toast.LENGTH_SHORT).show()
            }, { error ->
                Toast.makeText(this, "Failed to update items: ${error.message}", Toast.LENGTH_SHORT).show()
            })
    }
}
