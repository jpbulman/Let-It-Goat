package com.example.letitgoat.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.example.letitgoat.AddingItemToMarketplace
import com.example.letitgoat.R

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    private var root: View? = null
    private var menu: Menu? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        this.root = root

        setHasOptionsMenu(true)

        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        this.menu = menu

        (menu?.findItem(R.id.search)?.actionView as SearchView).apply {
            isIconifiedByDefault = false
        }
        menu.findItem(R.id.search)?.isVisible = true
        activity?.invalidateOptionsMenu()
    }

}