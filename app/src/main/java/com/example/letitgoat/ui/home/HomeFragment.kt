package com.example.letitgoat.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.example.letitgoat.AddingItemToMarketplace
import com.example.letitgoat.R
import com.example.letitgoat.ui.home.buy_recycler.BuyRecyclerFragment
import com.google.android.material.tabs.TabLayout
import java.util.*


class HomeFragment : Fragment(), SliderFragment.OnFragmentInteractionListener,
    BuyRecyclerFragment.OnFragmentInteractionListener  {

    private lateinit var homeViewModel: HomeViewModel

    private var root: View? = null
    private var menu: Menu? = null

    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var viewPager: ViewPager

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val list_title: ArrayList<String> = ArrayList()
        list_title.add("All")
        activity?.let {
            ArrayAdapter.createFromResource(
                it,
                R.array.planets_array,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                for (i in 0 until adapter.count) {
                    list_title.add(adapter.getItem(i).toString())
                }
            }
        }

        viewPagerAdapter = ViewPagerAdapter(childFragmentManager, list_title)
        viewPager = view.findViewById(R.id.pager)
        viewPager.adapter = viewPagerAdapter

        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout)
        tabLayout.setupWithViewPager(viewPager)
    }



    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        this.menu = menu

        (menu?.findItem(R.id.search)?.actionView as SearchView).apply {
            isIconifiedByDefault = true
        }
        menu.findItem(R.id.search)?.isVisible = true
        activity?.invalidateOptionsMenu()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_submit -> {
            // do stuff
            this.startActivity(Intent(activity, AddingItemToMarketplace::class.java))
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onFragmentInteraction(uri: Uri?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}