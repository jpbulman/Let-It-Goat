package com.example.letitgoat.ui.sell

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.SearchView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.letitgoat.AddingItemToMarketplace
import com.example.letitgoat.R

class SellFragment : Fragment() {

    private lateinit var sellViewModel: SellViewModel

    private var menu: Menu? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sellViewModel=
            ViewModelProviders.of(this).get(SellViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_sell, container, false)
        setHasOptionsMenu(true)

        val newItemButton = root.findViewById<Button>(R.id.addNewItemButton)
        newItemButton.setOnClickListener {
            startActivity(Intent(activity, AddingItemToMarketplace::class.java))
        }

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
        activity?.invalidateOptionsMenu()
    }
}