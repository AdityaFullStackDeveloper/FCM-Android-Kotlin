package com.example.notificationtest

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UpdateActivity : AppCompatActivity() {
    private var id: String = ""
    private var tokenId: String? = null
    private var firebaseFireStore = FirebaseFirestore.getInstance()
    private lateinit var updateName: AppCompatEditText
    private lateinit var updateEmail: AppCompatEditText
    private lateinit var updateDataButton: AppCompatButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update)

        FirebaseApp.initializeApp(this)

        id = intent.getStringExtra("id_key").toString()
        val email = intent.getStringExtra("update_email")
        val name = intent.getStringExtra("update_name")
        tokenId = intent.getStringExtra("token_id").toString()

        updateEmail = findViewById(R.id.update_email)
        updateName = findViewById(R.id.update_name)
        updateDataButton = findViewById(R.id.updateItemData)

        updateEmail.setText(email)
        updateName.setText(name)

        updateDataButton.setOnClickListener {
            updateItem()
        }

    }

    @SuppressLint("SuspiciousIndentation")
    private fun updateItem() {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                val email = updateEmail.text.toString()
                val name = updateName.text.toString()

                val map = mapOf(
                    "id" to id,
                    "email" to email,
                    "name" to name
                )

                firebaseFireStore.collection("storage").document(id).update(map)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this@UpdateActivity,
                            "Update Item successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        startActivity(Intent(this@UpdateActivity, MainActivity::class.java))
                        sendNotification(tokenId!!, name, email)
                        finish()
                        return@addOnSuccessListener
                    }
                    .addOnFailureListener {
                        Toast.makeText(this@UpdateActivity, "Update Failed", Toast.LENGTH_SHORT)
                            .show()
                        return@addOnFailureListener
                    }
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun sendNotification(token: String, title: String, email: String) {
        val updateItem = SendNotificationDelete(
            Data(
                title,
                "updated"
            ),
            Notification("Campaign Messages", "update", true, title),
            arrayListOf(token)
        )

        val hashMap = HashMap<String, String>()
        hashMap["Authorization"] =
            "key=AAAAqM28o0k:APA91bG9UtAZnTZvqRi4_TfId0Q7v0rAzKJvAdGO3N4ZLmlG-UxsHkd01UfmWE4ddjzEtq6Y_xw2I3-ZXkEJSjssUje6jeFH8xzQFm001bZiqvVvsKmSQGRKbAY21YQMVjB89Ty689q9"
        SendNotification.createNotification().deleteNotification(hashMap, updateItem)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                Toast.makeText(this, "Notification sent", Toast.LENGTH_SHORT).show()
            }, { error ->
                Toast.makeText(this, "Failed to update item${error.message}", Toast.LENGTH_SHORT)
                    .show()
            })
    }
}
