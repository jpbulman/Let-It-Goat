package com.example.letitgoat.ui.messages

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
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