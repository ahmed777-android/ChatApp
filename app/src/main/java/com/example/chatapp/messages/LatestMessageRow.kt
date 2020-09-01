package com.example.chatapp.messages

import android.content.Context
import com.example.chatapp.R
import com.example.chatapp.models.ChatMessage
import com.example.chatapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.lastest_message_row.view.*



class LatestMessageRow(private val chatMessage: ChatMessage):Item<ViewHolder>() {
  var chatPartnerUser: User? = null

  override fun bind(viewHolder: ViewHolder, position: Int) {
    viewHolder.itemView.last_message.text = chatMessage.text
    val chatPartnerId: String = if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
      chatMessage.toId
    } else {
      chatMessage.fromId
    }
    val ref = FirebaseDatabase.getInstance().getReference("/Users/$chatPartnerId")
    ref.addListenerForSingleValueEvent(object: ValueEventListener {
      override fun onDataChange(p0: DataSnapshot) {
        chatPartnerUser = p0.getValue(User::class.java)
        viewHolder.itemView.name.text = chatPartnerUser?.userName
        val targetImageView = viewHolder.itemView.profile_lastest
        Picasso.get().load(chatPartnerUser?.profileImageUri).into(targetImageView)
    //    Glide.with(context).load(chatPartnerUser?.profileImageUri).into( targetImageView)
      }
      override fun onCancelled(p0: DatabaseError) {}
    })
  }

  override fun getLayout(): Int {
    return R.layout.lastest_message_row
  }
}