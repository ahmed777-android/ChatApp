package com.example.chatapp.messages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.chatapp.R
import com.example.chatapp.models.ChatMessage
import com.example.chatapp.models.User
import com.example.chatapp.registerlogin.RegisterActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder


import kotlinx.android.synthetic.main.activity_lastest_messages.*

class LastestMessagesActivity : AppCompatActivity() {
    companion object {
        var currentUser: User? = null
    }

     val adapter = GroupAdapter<ViewHolder>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lastest_messages)
        verifyUserIsLoggedIn()

        rv_lastest_messages.adapter = adapter
        rv_lastest_messages.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))

        adapter.setOnItemClickListener { item, view ->
          //  Log.d(listenForLatestMessages.TAG, "123")
            val intent = Intent(this, ChatLogActivity::class.java)
            // we are missing the chat partner user
            val row = item as LatestMessageRow
            intent.putExtra(NewMessageActivity.USER_KEY, row.chatPartnerUser)
            startActivity(intent)
        }
        listenForLatestMessages()

        fetchCurrentUser()
    }
    val latestMessagesMap = HashMap<String, ChatMessage>()

    private fun refreshRecyclerViewMessages() {
        adapter.clear()
        latestMessagesMap.values.forEach {
            adapter.add(LatestMessageRow(it))
        }
    }

    private fun listenForLatestMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return
                latestMessagesMap[p0.key!!] = chatMessage
                refreshRecyclerViewMessages()
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return
                latestMessagesMap[p0.key!!] = chatMessage
                refreshRecyclerViewMessages()
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }
            override fun onChildRemoved(p0: DataSnapshot) {

            }
            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    //fetch current user and store in companion object to access from out side class
    private fun fetchCurrentUser() {

        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/Users/$uid")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser = snapshot.getValue(User::class.java)
               Log.d("LatesMessages", "Current user:${currentUser?.userName} ")
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    //asset if user logged in or not if not make intent to RegisterActivity
    private fun verifyUserIsLoggedIn() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    //handle what action when clicked item in menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_new_message -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //inflate custom menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}


