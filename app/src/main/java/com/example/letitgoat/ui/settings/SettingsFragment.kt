package com.example.letitgoat.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.SearchView
import android.widget.TextView
import androidx.core.view.marginBottom
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.letitgoat.MainActivity
import com.example.letitgoat.R

class SettingsFragment : Fragment() {

    private lateinit var settingsViewModel: SettingsViewModel

    private var menu: Menu? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        settingsViewModel =
            ViewModelProviders.of(this).get(SettingsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_settings, container, false)

        setHasOptionsMenu(true)

        val logoutButton = root.findViewById<Button>(R.id.logoutButton)

        logoutButton.setOnClickListener{
            MainActivity.user = null
            startActivity(Intent(activity, MainActivity::class.java))
        }

        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        this.menu = menu

        (menu?.findItem(R.id.search)?.actionView as SearchView).apply {
            isIconifiedByDefault = true
            queryHint = "settings?"
        }
        menu.findItem(R.id.search)?.isVisible = false
        activity?.invalidateOptionsMenu()
    }
}