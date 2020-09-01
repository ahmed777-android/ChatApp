package com.example.chatapp.messages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.R
import com.example.chatapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.user_row_new_mseeage.view.*

class NewMessageActivity : AppCompatActivity() {
    companion object{
        val  USER_KEY :String="USER_KEY"

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)
        supportActionBar?.title = "Select User"


        rv_newmessage.layoutManager = LinearLayoutManager(this)
        fetchUsers()
    }

    private fun fetchUsers() {
        val ref = FirebaseDatabase.getInstance().getReference("/Users")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val adapter = GroupAdapter<ViewHolder>()

                snapshot.children.forEach {
                    //   Log.d("TAG", "onDataChange: $it")
                    val user = it.getValue(User::class.java)
                    if (user != null && user.uid !=FirebaseAuth.getInstance().uid) {
                        adapter.add(UserItem(user))
                    }
                }
                adapter.setOnItemClickListener { item, view ->
                    val userItem= item as UserItem
                    val intent = Intent(view.context, ChatLogActivity::class.java)
                    intent.putExtra(USER_KEY,userItem.user)
                    startActivity(intent)
                    finish()
                }
                rv_newmessage.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
}

// rv to view all user in firebase
class UserItem(val user: User) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.itemView.textView_new_message.text = user.userName
      //  Glide.with(Context).load(user.profileImageUri).into(viewHolder.itemView.profile)
        Picasso.get().load(user.profileImageUri).into(viewHolder.itemView.profile)

    }

    override fun getLayout(): Int {
        return R.layout.user_row_new_mseeage
    }

}
