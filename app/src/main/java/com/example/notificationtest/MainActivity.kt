package com.example.notificationtest

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.messaging
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), UserListAdapter.OnClickDeleteItem {

    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userAdapter: UserListAdapter
    private val firebaseFirestore = FirebaseFirestore.getInstance()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)

        userRecyclerView = findViewById(R.id.userRecyclerView)

        getUserData()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getUserData() {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main){
                firebaseFirestore.collection("storage").get()
                    .addOnSuccessListener { result ->
                        val arrayList = ArrayList<DataModel>()
                        for (userData in result) {
                            val profile = userData.toObject(DataModel::class.java)
                            arrayList.add(profile)
                        }

                        userAdapter = UserListAdapter(arrayList, this@MainActivity, this@MainActivity)
                        userRecyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
                        userRecyclerView.adapter = userAdapter
                    }.addOnFailureListener {
                        Toast.makeText(this@MainActivity, "Failed to add data", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    override fun deleteItemView(dataModel: DataModel) {
        sendNotificationToDeleteItem(dataModel)
        getUserData()
    }

    @SuppressLint("CheckResult")
    private fun sendNotificationToDeleteItem(userData: DataModel) {
        val deleteItem = SendNotificationDelete(
            Data(
                "name",
                "userName"
            ),
            Notification("Campaign messages", "${userData.name}", true, "Deleted"),
            arrayListOf(userData.tokenId.toString())
        )

        val hashMap = HashMap<String, String>()
        hashMap["Authorization"] =
            "key=AAAAqM28o0k:APA91bG9UtAZnTZvqRi4_TfId0Q7v0rAzKJvAdGO3N4ZLmlG-UxsHkd01UfmWE4ddjzEtq6Y_xw2I3-ZXkEJSjssUje6jeFH8xzQFm001bZiqvVvsKmSQGRKbAY21YQMVjB89Ty689q9"
        SendNotification.createNotification().deleteNotification(hashMap, deleteItem)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                Toast.makeText(this, "Notification Sent", Toast.LENGTH_SHORT).show()
            }, { error ->
                Toast.makeText(this, "Failed to delete item${error.message}", Toast.LENGTH_SHORT)
                    .show()
            })
    }

}
