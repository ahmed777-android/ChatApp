package com.example.chatapp.models

data class ChatMessage(val text:String="",val id :String="",val fromId:String="",val toId:String="",val timestamp:Long=-1)
