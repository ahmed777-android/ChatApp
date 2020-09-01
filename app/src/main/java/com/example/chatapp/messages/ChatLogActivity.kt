package com.example.chatapp.messages

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.R
import com.example.chatapp.models.ChatMessage
import com.example.chatapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder

import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*


class ChatLogActivity : AppCompatActivity() {
    companion object {
        const val TAG = "ChatLog"
    }

    //that user will send to activity by intent when item clicked
    var toUser: User? = null
    var fromUser: User? = LastestMessagesActivity.currentUser
    private val adapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)
        rv_chat_log.layoutManager = LinearLayoutManager(this)
        rv_chat_log.adapter = adapter

        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar!!.title = toUser!!.userName


        listenerForMessages()

        btn_chat_log.setOnClickListener {
            Log.d(TAG, "onCreate: Attempt to send message ...")
            performSendMessage()
        }
    }

    private fun listenerForMessages() {
        val fromId = FirebaseAuth.getInstance().uid

        val toId = toUser!!.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)

                if (chatMessage != null && chatMessage.text != "") {
                    Log.d(TAG, chatMessage?.text)
                  //  adapter.add(ChatFromItem(chatMessage.text, context = applicationContext, user = fromUser!!))
                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        adapter.add(ChatFromItem(chatMessage.text,  user = fromUser!!))
                    }
                    else
                    {
                        adapter.add(ChatToItem(chatMessage.text, toUser!!))
                    }
                }
                rv_chat_log.scrollToPosition(adapter.itemCount-1)
            }
            override fun onCancelled(error: DatabaseError) {}
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
        })
    }

//this fun used when clicked btn to send massage to firebase
    private fun performSendMessage() {
        val fromId = FirebaseAuth.getInstance().uid

        val toId = toUser!!.uid
        // we send message to firebase twice time for from user and second for to user
        val fromReference =
            FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
        val toReference =
            FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

        var text = editText_chat_log.text.toString()
    //this text what will send to ref from & to user
        val chatMessage = ChatMessage(text = text, id = fromReference.key!!, fromId = fromId!!, toId = toId!!, System.currentTimeMillis() / 1000)

        fromReference.setValue(chatMessage).addOnSuccessListener {
            Log.d(TAG, "performSendMessage: save chat message ${fromReference.key}")
            rv_chat_log.scrollToPosition(adapter.itemCount -1)
            editText_chat_log.text = null

        }
        toReference.setValue(chatMessage)
    val latesMessageRef =
        FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
    latesMessageRef.setValue(chatMessage)
    val latesMessageToRef =
        FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
    latesMessageToRef.setValue(chatMessage)
}


}
//this class used to add item in recyclerview  from user id message
class ChatFromItem(private val text: String = "", private val user: User) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textViewfrom.text = text
      //  Glide.with(context).load(user.profileImageUri).into(viewHolder.itemView.profile_from)
        Picasso.get().load(user.profileImageUri).into(viewHolder.itemView.profile_from)

    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}
//this class used to add item in recyclerview to user id message
class ChatToItem(val text: String = "", private val user: User) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textViewto.text = text
     //   Glide.with(context).load(user.profileImageUri).into(viewHolder.itemView.profile_to)
        Picasso.get().load(user.profileImageUri).into(viewHolder.itemView.profile_to)

    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

}
