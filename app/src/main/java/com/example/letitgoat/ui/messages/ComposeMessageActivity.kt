package com.example.letitgoat.ui.messages

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import com.example.letitgoat.MainActivity
import com.example.letitgoat.R
import com.example.letitgoat.ui.db_models.Message
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class ComposeMessageActivity : AppCompatActivity() {

    private val convoArray = arrayListOf<String>("")
    private var recipient = ""
    var onAdapterChange = {}

    private val handler = Handler()

    fun loadMessages() {
        val currUser = MainActivity.user
        if (currUser != null) {
            FirebaseFirestore.getInstance().collection("Messages")
                .whereEqualTo("sender", currUser.email)
                .whereEqualTo("recipient", recipient)
                .get().addOnSuccessListener {sentMessages ->
                    FirebaseFirestore.getInstance().collection("Messages")
                        .whereEqualTo("recipient", currUser.email)
                        .whereEqualTo("sender", recipient)
                        .get().addOnSuccessListener {receivedMessages ->
                            val allMessages = sentMessages.toObjects(Message::class.java)
                            allMessages.addAll(receivedMessages.toObjects(Message::class.java))
                            allMessages.sortBy { m -> m.timeSent }
                            runOnUiThread{
                                val newMessages = arrayListOf<String>()
                                for (message in allMessages) {
                                    newMessages.add(message.sender + "\n" + message.contents)
                                }
                                convoArray.clear()
                                convoArray.addAll(newMessages)
                                onAdapterChange()
                            }
                    }

                }
        }
    }

    val loadCallback = {loadMessages()}

    override fun onResume() {
        super.onResume()
        handler.postDelayed(loadCallback, 5000)

    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(loadCallback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compose_message)
        val conversation = findViewById<ListView>(R.id.conversation)
        recipient = intent.getStringExtra("username")
        val sendButton = findViewById<Button>(R.id.sendButton)
        val messageToSend = findViewById<EditText>(R.id.messageToSend)
        val convoAdapter = ArrayAdapter<String>(this, R.layout.conversation_layout, convoArray)
        onAdapterChange = {
            convoAdapter.notifyDataSetChanged()
        }
        conversation.adapter = convoAdapter
        loadMessages()
        sendButton.setOnClickListener{
            Log.d("MATT", "HELLO HELLO")
            val currUser = MainActivity.user
            if (currUser != null) {
                val message = Message(
                    contents = messageToSend.text.toString(),
                    sender = currUser.email,
                    recipient = recipient,
                    timeSent = Date()
                )
                FirebaseFirestore
                    .getInstance()
                    .collection("Messages")
                    .add(message)
                    .addOnSuccessListener {
                        loadMessages()
                    }
                convoAdapter.notifyDataSetChanged()
                messageToSend.setText("")
                conversation.smoothScrollToPosition(Integer.MAX_VALUE)
            } else {
                System.out.println("Null was detected")
            }
        }
    }
}
