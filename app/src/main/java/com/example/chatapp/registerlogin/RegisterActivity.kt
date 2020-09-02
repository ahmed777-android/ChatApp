package com.example.chatapp.registerlogin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.chatapp.messages.LastestMessagesActivity
import com.example.chatapp.R
import com.example.chatapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class RegisterActivity : AppCompatActivity() {
    private var selectedPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        text.text = "SELECT\nPHOTO"
        register_btn.setOnClickListener { performRegister() }
        hav_acc.setOnClickListener {
            //when click in text that will finish activity and return to parent
            finish()
        }
        //that on click used to take image from mobile storage
        cardView.setOnClickListener {
          //  Log.d("Register", "try to show photo selector: ")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

    }

    private fun performRegister() {

        val email = ET_email.text.toString()
        val password = ET_password.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            //make toast when any filed is empty
            Toast.makeText(this, "Please enter text in email/pw", Toast.LENGTH_LONG).show()
            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful)
                    return@addOnCompleteListener

                //else if successful
                uploadImageToFirebaseStorage()


                   Log.d("Register", "Successfully created user with uid ${it.result?.user?.uid}")

            }.addOnFailureListener {
                  Log.d("Register", "Failed to create user : ${it.message}")
            }
    }

    private fun uploadImageToFirebaseStorage() {
        if (selectedPhotoUri == null)
            return
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        //    Log.d("TAG", "uploadImageToFirebaseStorage: " + filename.length)
        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                   Log.d("Register", "uploadImageToFirebaseStorage: ")
                ref.downloadUrl.addOnSuccessListener {
                    savedUserToFirebaseDatabase(it.toString())
                }
            }
            .addOnFailureListener {
                Log.d("Register", "uploadImageToFirebaseStorage: ")
            }


    }


    private fun savedUserToFirebaseDatabase(proficleImageUri: String) {
        //take user id from firebase Auth that will store in object that will store in firebase database
        val uid = FirebaseAuth.getInstance().uid ?: ""
        //get ref where data will store in firebase
        val ref = FirebaseDatabase.getInstance().getReference("Users/$uid")
        // create object of data that will store in firebase Database
        val user = User(uid =  uid, userName = ET_userName.text.toString(), profileImageUri= proficleImageUri)
        //send object to firebase in ref name by user id
        ref.setValue(user).addOnSuccessListener {
            Log.d("TAG", "savedUserToFirebaseDatabase: ")
            val intent = Intent(this, LastestMessagesActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
      //for take image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d("Register", "onActivityResult: photo was selected")
            //for save image as uri to save in
            selectedPhotoUri = data.data
            //for clear text to not be in the top in the image
            text.text = ""
            // that another way to load image as a Drawable but that way make heavy in mobile and take long time

            //  val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhototUri)
            //  val bitmapDrawable = BitmapDrawable(bitmap)
            //  selectphoto_imageview_register.setBackgroundDrawable(bitmapDrawable)

            //used glide to load image that make load easy and not heavy processor in the mobile
            Glide.with(this).load(selectedPhotoUri).into(selectphoto_imageview_register)
        }
    }
}
