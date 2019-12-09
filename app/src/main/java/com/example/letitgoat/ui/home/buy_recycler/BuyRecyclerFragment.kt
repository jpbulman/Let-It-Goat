package com.example.letitgoat.ui.home.buy_recycler

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.letitgoat.ItemActivity
import com.example.letitgoat.R
import com.example.letitgoat.db_models.Item
import com.example.letitgoat.ui.search.SearchResultsActivity

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [BuyRecyclerFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [BuyRecyclerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BuyRecyclerFragment : Fragment(),
    BuyViewAdapter.ItemClickListener {
    private var recyclerView: RecyclerView? = null
    private var mAdapter: BuyViewAdapter? = null
    private var layoutManager: RecyclerView.LayoutManager? = null
    // TODO: Rename and change types of parameters
    private var title: String? = null
    private var mListener: OnFragmentInteractionListener? =
        null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            title = arguments!!.getString(ARG_TITLE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? { // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_buy_recycler, container, false)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = getView()?.findViewById(R.id.buy_recyclerview)
        // use this setting to improve performance if you know that changes
    // in content do not change the layout size of the RecyclerView
        recyclerView?.setHasFixedSize(true)
        // use a linear layout manager
        layoutManager = LinearLayoutManager(context)
        recyclerView?.layoutManager = layoutManager
        // specify an adapter (see also next example)
        if (activity is SearchResultsActivity){
            val activity = activity as SearchResultsActivity
            mAdapter = BuyViewAdapter(context, title, activity.searchQuery)
        }
        else {
            mAdapter = BuyViewAdapter(context, title)
        }
        mAdapter!!.setClickListener(this)
        recyclerView?.adapter = mAdapter
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri?) {
        if (mListener != null) {
            mListener!!.onFragmentInteraction(uri)
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri?)
    }

    override fun onItemClick(
        view: View,
        position: Int,
        item: Item
    ) {
        Log.d("ItemsFragment", position.toString() + "")
        val intent = Intent(context, ItemActivity::class.java)
        intent.putExtra("extra_item", item)
        startActivity(intent)
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private val ARG_TITLE: String? = null

        @JvmStatic
        fun newInstance(title: String): BuyRecyclerFragment {
            val fragment = BuyRecyclerFragment()
            val args = Bundle()
            args.putString(ARG_TITLE, title)
            fragment.arguments = args
            Log.d("check_fragment: ", title + "")
            return fragment
        }
    }
}