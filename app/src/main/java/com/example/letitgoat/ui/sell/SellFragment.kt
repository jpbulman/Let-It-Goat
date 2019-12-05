package com.example.letitgoat.ui.sell

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProviders
import com.example.letitgoat.AddingItemToMarketplace
import com.example.letitgoat.R
import com.example.letitgoat.ui.sell.sell_recycler.SellRecyclerFragment

class SellFragment : Fragment() {

    private lateinit var sellViewModel: SellViewModel

    private var root: View? = null
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

//        val newItemButton = root.findViewById<Button>(R.id.addNewItemButton)
//        newItemButton.setOnClickListener {
//            startActivity(Intent(activity, AddingItemToMarketplace::class.java))
//        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Create a new Fragment to be placed in the activity layout
        val itemsFragment = SellRecyclerFragment.newInstance("1", "2")

        val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_sell_container, itemsFragment).commit()

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        this.menu = menu

        (menu.findItem(R.id.search)?.actionView as SearchView).apply {
            isIconifiedByDefault = true
//            queryHint = "search from history"
        }

        menu.findItem(R.id.search)?.isVisible = true
        activity?.invalidateOptionsMenu()
    }
}