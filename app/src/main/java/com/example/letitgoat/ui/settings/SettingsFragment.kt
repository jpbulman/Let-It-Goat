package com.example.letitgoat.ui.settings

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.example.letitgoat.MainActivity
import com.example.letitgoat.R
import com.example.letitgoat.db_models.User
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class SettingsFragment : Fragment() {

    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var database: FirebaseFirestore
    private var menu: Menu? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        settingsViewModel =
            ViewModelProviders.of(this).get(SettingsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_settings, container, false)

        database = FirebaseFirestore.getInstance()

        setHasOptionsMenu(true)

        val logoutButton = root.findViewById<Button>(R.id.logoutButton)

        logoutButton.setOnClickListener{
            MainActivity.user = User(
                name ="dev",
                email ="notloggedin@wpi.edu",
                profilePicture = ""
            )
            startActivity(Intent(activity, MainActivity::class.java))
        }

        val newProfilePictureButton = root.findViewById<ImageButton>(R.id.newProfilePictureButton)

        newProfilePictureButton.setOnClickListener{
            dispatchTakePictureIntent()
        }

        val displayUsersFullName = root.findViewById<TextView>(R.id.displayUsersFullName)
        displayUsersFullName.text = MainActivity.user.name

        val profilePicture = root.findViewById<ImageView>(R.id.profilePicture)
        profilePicture.rotation = -90f
        val encodeByte = Base64.decode(MainActivity.user.profilePicture, Base64.DEFAULT)
        profilePicture.setImageBitmap(BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size))

        return root
    }

    val REQUEST_IMAGE_CAPTURE = 1

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    //Camera returns
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data!!.extras!!.get("data") as Bitmap
            val profilePicture = activity!!.findViewById<ImageView>(R.id.profilePicture)
            profilePicture.setImageBitmap(imageBitmap)
            profilePicture.rotation = -90f

            val baos = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
            val b = baos.toByteArray()
            val bitmapAsString = Base64.encodeToString(b, Base64.DEFAULT)

            MainActivity.user = User(
                name = MainActivity.user.name,
                email = MainActivity.user.email,
                profilePicture = bitmapAsString
            )

            database.collection("Users").document(MainActivity.user.email).set(MainActivity.user)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        this.menu = menu

        (menu.findItem(R.id.search)?.actionView as SearchView).apply {
            isIconifiedByDefault = true
            queryHint = "settings?"
        }
        menu.findItem(R.id.search)?.isVisible = false
        activity?.invalidateOptionsMenu()
    }
}