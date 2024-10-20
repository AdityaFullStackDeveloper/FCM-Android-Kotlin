package com.example.notificationtest

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class UserListAdapter(private val list: List<DataModel>, private val context: Context, val onClickDeleteItem: OnClickDeleteItem):RecyclerView.Adapter<UserListAdapter.UserNotificationViewHolder>() {

    class UserNotificationViewHolder(userView: View):RecyclerView.ViewHolder(userView){
        val userEmailView: TextView = userView.findViewById(R.id.userEmailView)
        val userNameView: TextView = userView.findViewById(R.id.userNameView)
        val userUpdateView: ImageView = userView.findViewById(R.id.userUpdateIcon)
        val userDeleteView: ImageView = userView.findViewById(R.id.userDeleteIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserNotificationViewHolder {
        val userView = LayoutInflater.from(context).inflate(R.layout.user_list_api,parent,false)
        return UserNotificationViewHolder(userView)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: UserNotificationViewHolder, position: Int) {
        holder.userEmailView.text = list[position].email
        holder.userNameView.text = list[position].name

        holder.userUpdateView.setOnClickListener {
            val updateIntent = Intent(context, UpdateActivity::class.java)
            updateIntent.putExtra("id_key", list[position].id)
            updateIntent.putExtra("update_email", list[position].email)
            updateIntent.putExtra("update_name", list[position].name)
            updateIntent.putExtra("token_id", list[position].tokenId)

            context.startActivity(updateIntent)
        }

        holder.userDeleteView.setOnClickListener {
            FirebaseFirestore.getInstance().collection("storage").document(list[position].id!!)
                .delete()
                .addOnSuccessListener {
                    onClickDeleteItem.deleteItemView(list[position])
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error deleting user: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("UserListAdapter", "Error deleting user", e)
                }
        }

    }

    interface OnClickDeleteItem{
        fun deleteItemView(dataModel: DataModel)
    }
}