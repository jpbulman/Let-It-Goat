package com.example.letitgoat.ui.messages

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.example.letitgoat.AddingItemToMarketplace
import com.example.letitgoat.MainActivity
import com.example.letitgoat.R
import com.example.letitgoat.db_models.User
import com.example.letitgoat.ui.db_models.Message
import com.google.firebase.firestore.FirebaseFirestore

class MessagesFragment : Fragment() {

    private lateinit var messagesViewModel: MessagesViewModel

    private var menu: Menu? = null

    val handler = Handler()

    private var chatArray: ArrayList<String> = arrayListOf<String>();
    private var chatAdapter: ArrayAdapter<String>? = null


    private fun refreshMessages() {
        println("REFRESHING MESSAGES")
            FirebaseFirestore.getInstance().collection("Messages")
                .whereEqualTo("sender", MainActivity.user.email)
                .get().addOnSuccessListener { sentMessages ->
                    FirebaseFirestore.getInstance().collection("Messages")
                        .whereEqualTo("recipient", MainActivity.user.email)
                        .get().addOnSuccessListener { receivedMessages ->
                            var allMessages = sentMessages
                                .toObjects(Message::class.java)
                            allMessages.addAll(receivedMessages.toObjects(Message::class.java))
                            allMessages.sortByDescending { m -> m.timeSent }
                            val allUsers = arrayListOf<String>()
                            println("\n\n\nPRINTING MSGS")
                            allMessages.forEach {
                                println(it.recipient)
                                println()
                                val user =
                                    if (it.recipient == MainActivity.user.email) it.sender else it.recipient
                                if (!allUsers.contains(user)) {
                                    allUsers.add(user)
                                }
                            }
                            chatArray.clear()
                            chatArray.addAll(allUsers)
                            chatAdapter!!.notifyDataSetChanged()
                        }

        }
    }

    val refreshCallback: Runnable = run {
        Runnable {
            refreshMessages()
            handler.postDelayed(refreshCallback, 5000)
        }
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(refreshCallback, 5000)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(refreshCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        messagesViewModel =
            ViewModelProviders.of(this).get(MessagesViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_messages, container, false)
        val chatList = root.findViewById<ListView>(R.id.chatList)


        val currContext = context
        if (currContext == null) {
            return root
        } else {
            chatAdapter = ArrayAdapter<String>(currContext, R.layout.conversation_layout, chatArray)
            chatList.adapter = chatAdapter
            refreshMessages()
            chatList.setOnItemClickListener {_, _, position, _ ->
                val intent = Intent(currContext, ComposeMessageActivity::class.java)
                intent.putExtra("username", chatArray.get(position))
                startActivity(intent)
            }
        }

        setHasOptionsMenu(true)
        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        this.menu = menu

        (menu.findItem(R.id.search)?.actionView as SearchView).apply {
            isIconifiedByDefault = true
            queryHint = "settings?"
        }

        menu.findItem(R.id.search)?.isVisible = false
        menu.findItem(R.id.action_submit)?.isVisible = false
        activity?.invalidateOptionsMenu()
    }

}