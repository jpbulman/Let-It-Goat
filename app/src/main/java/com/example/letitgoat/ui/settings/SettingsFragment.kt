package com.example.letitgoat.ui.settings

import android.app.Activity.RESULT_OK
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.*
import androidx.core.content.FileProvider
import androidx.core.view.marginBottom
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.letitgoat.MainActivity
import com.example.letitgoat.R
import java.io.File
import java.nio.file.Files.createFile

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

        val newProfilePictureButton = root.findViewById<ImageButton>(R.id.newProfilePictureButton)

        newProfilePictureButton.setOnClickListener{
            dispatchTakePictureIntent()
        }

        val displayUsersFullName = root.findViewById<TextView>(R.id.displayUsersFullName)
        displayUsersFullName.text = MainActivity.user!!.name

        return root
    }

    val REQUEST_IMAGE_CAPTURE = 1

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
//            takePictureIntent.resolveActivity(getPackageManager())?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
//            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data!!.extras!!.get("data") as Bitmap
            activity!!.findViewById<ImageView>(R.id.profilePicture).setImageBitmap(imageBitmap)

        }
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