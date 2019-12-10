package com.example.letitgoat.ui.search

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import com.example.letitgoat.R
import com.example.letitgoat.ui.home.buy_recycler.BuyRecyclerFragment

class SearchResultsActivity : AppCompatActivity() {
    var numItems = 0
    var searchQuery = ""
    var numItemsCallback : (Int)->Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            searchQuery = query
        }

        setContentView(R.layout.activity_search)
        handleIntent(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_search, menu)
        // Associate searchable configuration with the SearchView
        return true
    }

    private fun handleIntent(intent: Intent) {
    }

    fun runCallback(numItems: String) {
        if(numItems == "numItems"){
            numItemsCallback(this.numItems)
        }
    }
}
