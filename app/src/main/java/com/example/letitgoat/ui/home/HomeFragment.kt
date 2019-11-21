package com.example.letitgoat.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProviders
import com.example.letitgoat.AddingItemToMarketplace
import com.example.letitgoat.R
import com.example.letitgoat.ui.home.items_recycler.ItemsRecyclerFragment


class HomeFragment : Fragment(), SliderFragment.OnFragmentInteractionListener,
    ItemsRecyclerFragment.OnFragmentInteractionListener  {

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

//        val newItemButton = root.findViewById<Button>(R.id.addNewItemButton)
//        newItemButton.setOnClickListener {
//            startActivity(Intent(activity, AddingItemToMarketplace::class.java))
//        }



        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Create a new Fragment to be placed in the activity layout
        val firstFragment = SliderFragment.newInstance("1", "2")
        val secondFragment = ItemsRecyclerFragment.newInstance("1", "2")

        val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container_, firstFragment)
        transaction.replace(R.id.fragment_container_2, secondFragment).commit()

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

    override fun onFragmentInteraction(uri: Uri?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}