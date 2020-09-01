package com.example.chatapp.models

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(val uid: String?, val userName: String?, val profileImageUri: String?):Parcelable {
    constructor():this("","","")

}