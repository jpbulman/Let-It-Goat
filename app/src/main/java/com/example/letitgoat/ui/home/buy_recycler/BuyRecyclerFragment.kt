package com.example.letitgoat.ui.home.buy_recycler

import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.letitgoat.ItemActivity
import com.example.letitgoat.R
import com.example.letitgoat.WPILocationHelper
import com.example.letitgoat.db_models.Item
import com.example.letitgoat.db_models.User
import com.example.letitgoat.ui.search.SearchResultsActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import me.dkzwm.widget.srl.RefreshingListenerAdapter
import me.dkzwm.widget.srl.SmoothRefreshLayout
import me.dkzwm.widget.srl.extra.footer.ClassicFooter
import me.dkzwm.widget.srl.extra.header.ClassicHeader
import me.dkzwm.widget.srl.indicator.IIndicator
import org.jetbrains.anko.doAsync
import java.util.*
import kotlin.collections.ArrayList


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
    private var mListener: OnFragmentInteractionListener? = null
    private lateinit var db: FirebaseFirestore
    private var reachedLastItem: Boolean = false
    private lateinit var refreshLayout: SmoothRefreshLayout

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
        refreshLayout = getView()?.findViewById(R.id.refreshLayout) as SmoothRefreshLayout
        refreshLayout.setHeaderView(ClassicHeader<IIndicator>(context))
        refreshLayout.setFooterView(ClassicFooter<IIndicator>(context))
        refreshLayout.setDisableLoadMore(false)
        refreshLayout.setOnRefreshListener(object : RefreshingListenerAdapter() {
            override fun onRefreshing() {
                refreshLayout.refreshComplete()
            }

            override fun onLoadingMore() {
                if (reachedLastItem) {
                    refreshLayout.refreshComplete()
                    Toast.makeText(context, "I am the bottom line...", Toast.LENGTH_LONG).show()
                    return
                }
                doAsync {
                    loadData()
                }
                refreshLayout.refreshComplete()
            }
        })

        db = FirebaseFirestore.getInstance()
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
            mAdapter = BuyViewAdapter(context, title, activity.searchQuery, activity)
            activity.numItems = mAdapter!!.itemCount
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
        view: View?,
        position: Int,
        item: Item?
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

    fun loadData() {
        val dbItems: CollectionReference = db.collection("Items")
        val subset: Query
        subset = if (title != "All") {
            dbItems.whereEqualTo("category", title)
                .startAfter( mAdapter!!.lastSnapshot)
                .limit(10)
        } else {
            dbItems.startAfter( mAdapter!!.lastSnapshot)
                .limit(10)
        }
        val itemsOnMarket: ArrayList<Item> = ArrayList()
        val itemsOnMarketIds: ArrayList<String> = ArrayList()

        subset.get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result!!.size() == 0) {
                        reachedLastItem = true
                        Toast.makeText(context, "I am the bottom line...", Toast.LENGTH_LONG).show()
                    }
                    for (document in task.result!!) {
                        val doc =
                            document.data
                        val hash =
                            doc["user"] as HashMap<String, Any>?
                        val u =
                            User(
                                hash!!["email"].toString(),
                                hash["name"].toString(),
                                hash["profilePicture"].toString()
                            )
                        val d =
                            (doc["postedTimeStamp"] as Timestamp?)!!.toDate()

                        val wpiLocationHelper =
                            WPILocationHelper()
                        var l =
                            wpiLocationHelper.getLocationOfGordonLibrary()
                        if (doc["pickupLocation"] != null) {
                            val mapper =
                                doc["pickupLocation"] as HashMap<String, Any>?
                            l =
                                Location(mapper!!["provider"].toString())
                            l.latitude =
                                java.lang.Double.valueOf(mapper["latitude"].toString())
                            l.longitude =
                                java.lang.Double.valueOf(mapper["longitude"].toString())
                        }

                        var category = "other"
                        if (doc.containsKey("category")) {
                            category = doc["category"].toString()
                        }
                        val i =
                            Item(
                                doc["name"].toString(),
                                java.lang.Double.valueOf(doc["price"].toString()),
                                u,
                                doc["description"].toString(),
                                d,
                                doc["stringsOfBitmapofPicuresOfItem"] as List<String>,
                                l,
                                category
                            )
                        itemsOnMarket.add(i)
                        itemsOnMarketIds.add(document.id)
                        mAdapter!!.lastSnapshot = document
                    }
                    mAdapter!!.addItemsOnMarket(itemsOnMarket, itemsOnMarketIds)
                } else {
                    println("Could not get the user's items for selling from the DB")
                }
            }
    }

}