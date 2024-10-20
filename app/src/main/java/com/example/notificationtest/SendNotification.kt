package com.example.notificationtest

import io.reactivex.rxjava3.core.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST

interface SendNotification {

    @POST("send")
    fun deleteNotification(@HeaderMap hashMap : HashMap<String, String>,@Body deleteItemToNotify: SendNotificationDelete): Observable<Any>

    companion object NotificationApi{
        fun createNotification(): SendNotification {
            return Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .baseUrl("https://fcm.googleapis.com/fcm/")
                .build()
                .create(SendNotification::class.java)
        }
    }
}



