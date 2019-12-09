package com.example.letitgoat.ui.messages

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.example.letitgoat.AddingItemToMarketplace
import com.example.letitgoat.R

class MessagesFragment : Fragment() {

    private lateinit var messagesViewModel: MessagesViewModel

    private var menu: Menu? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        messagesViewModel =
            ViewModelProviders.of(this).get(MessagesViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_messages, container, false)
        val chatList = root.findViewById<ListView>(R.id.chatList)
        val chatArray = arrayListOf("eagu@wpi.edu", "rjwalls@wpi.edu")
        val currContext = context
        if (currContext == null) {
            return root
        } else {
            val chatListAdapter =
                ArrayAdapter<String>(currContext, R.layout.conversation_layout, chatArray)
            chatList.adapter = chatListAdapter

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