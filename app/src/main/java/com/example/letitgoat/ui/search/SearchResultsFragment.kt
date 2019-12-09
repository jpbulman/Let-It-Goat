package com.example.letitgoat.ui.search

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.example.letitgoat.R
import com.example.letitgoat.ui.home.HomeViewModel
import com.example.letitgoat.ui.home.SliderFragment
import com.example.letitgoat.ui.home.ViewPagerAdapter
import com.example.letitgoat.ui.home.buy_recycler.BuyRecyclerFragment
import java.util.ArrayList


class SearchResultsFragment : Fragment(), SliderFragment.OnFragmentInteractionListener,
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
        val root = inflater.inflate(R.layout.fragment_search, container, false)
        this.root = root

        setHasOptionsMenu(true)


        return root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        var resultsText = view?.findViewById<TextView>(R.id.num_results2)

        var activity = (activity as SearchResultsActivity)
        resultsText?.text = "5 Results for ${activity.searchQuery}"
//        viewPagerAdapter = ViewPagerAdapter(childFragmentManager, activity.searchQuery)
//        viewPager = view.findViewById(R.id.pager)
//        viewPager.adapter = viewPagerAdapter

        super.onViewCreated(view, savedInstanceState)
    }



    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        this.menu = menu

        activity?.invalidateOptionsMenu()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_back_to_home -> {
            // do stuff
            activity?.finish()
            true
        }
        else -> {true}
    }

    override fun onFragmentInteraction(uri: Uri?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}