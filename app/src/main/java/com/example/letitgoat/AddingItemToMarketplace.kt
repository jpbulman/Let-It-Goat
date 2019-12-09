package com.example.letitgoat

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.*
import com.example.letitgoat.db_models.Item
import com.google.firebase.firestore.FirebaseFirestore
import org.jetbrains.anko.toast
import java.io.ByteArrayOutputStream
import java.lang.NumberFormatException
import java.util.*
import kotlin.collections.ArrayList
import android.location.Location
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.location.LocationListener
import android.location.LocationManager
import android.media.Image
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import android.os.Looper
import android.os.StrictMode
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.marginBottom
import com.google.android.gms.location.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_adding_item_to_marketplace.*
import java.io.File


class AddingItemToMarketplace : AppCompatActivity() {

    private lateinit var database: FirebaseFirestore
    private var stringsOfBitmapsOfItems: List<String> = ArrayList()
    private var videoFile: File = File("")

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var storage: FirebaseStorage

    private var hasTakenVideo = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adding_item_to_marketplace)

        database = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        val addingItemToMarketplace = findViewById<Button>(R.id.addToMarketPlaceButton)
        addingItemToMarketplace.setOnClickListener{
            addItemToMarketplace()
        }

        val sellerName = findViewById<TextView>(R.id.sellerName)
        sellerName.text = "Being sold by: ${MainActivity.user.name}"

        val takePhotoButton = findViewById<ImageButton>(R.id.newItemPictureButton)
        takePhotoButton.setOnClickListener{
            dispatchTakePictureIntent()
        }

        val locationHelper = WPILocationHelper()
        val spinner = findViewById<Spinner>(R.id.pickupLocationSpinner)
        val locationSpinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            locationHelper.listOfLocationAsStrings
        )
        spinner.adapter = locationSpinnerAdapter

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        findViewById<ImageButton>(R.id.videoButton).setOnClickListener{
            this.dispatchTakeVideoIntent()
        }

//        val builder = StrictMode.VmPolicy.Builder()
//        StrictMode.setVmPolicy(builder.build())
    }

    val REQUEST_VIDEO_CAPTURE = 1

    private fun dispatchTakeVideoIntent() {
        Intent(MediaStore.ACTION_VIDEO_CAPTURE).also { takeVideoIntent ->
            val pather = File(filesDir, "Videos")
            pather.mkdirs()
            val file = File(pather,"VideoFileName.mp4")
            takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this, "$packageName.fileprovider", file))

            takeVideoIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE)
            }
        }
    }

    val REQUEST_IMAGE_CAPTURE = 2

    private fun dispatchTakePictureIntent() {
        val inten = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(inten, REQUEST_IMAGE_CAPTURE)
    }

    //Camera returns
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data!!.extras!!.get("data") as Bitmap
            val picOfAboutToSellItem = findViewById<ImageView>(R.id.itemAboutToBeSoldPicture)
            picOfAboutToSellItem.setImageBitmap(imageBitmap)
            picOfAboutToSellItem.rotation = 90f

            val baos = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
            val b = baos.toByteArray()
            val bitmapAsString = Base64.encodeToString(b, Base64.DEFAULT)
            this.stringsOfBitmapsOfItems += bitmapAsString
        } else if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            if(resultCode != Activity.RESULT_CANCELED){
                var f  = File(filesDir,"Videos")
                f.listFiles().forEach lit@{
                    if(it.name == "VideoFileName.mp4"){
                        f = it
                        return@lit
                    }
                }

                itemAboutToBeSoldPicture.requestLayout()
                itemAboutToBeSoldPicture.layoutParams.width = 0
                itemAboutToBeSoldPicture.layoutParams.height = 0

                videoView.requestLayout()
                videoView.layoutParams.width = 1000
                videoView.layoutParams.height = 750

                val layout = buttonHolder.layoutParams as LinearLayout.LayoutParams
                layout.setMargins(25,20,0,0)

                this.hasTakenVideo = true
                this.videoFile = f

                videoView.setVideoURI(f.toUri())
                videoView.setOnPreparedListener { mediaPlayer -> mediaPlayer.isLooping = true }
                videoView.start()
            }
        }
    }

    private fun addItemToMarketplace(){
        var validInput = true

        val name = findViewById<EditText>(R.id.itemNameField).text.toString()
        var price = -1.0

        try {
            price = findViewById<EditText>(R.id.priceField).text.toString().toDouble()
        } catch (e: NumberFormatException){
            validInput = false
        }

        val user = MainActivity.user
        val description = findViewById<EditText>(R.id.descriptionField).text.toString()
        val currTime = Date()

        if(name == "" || description == ""){
            validInput = false
        }

        if(this.stringsOfBitmapsOfItems.isEmpty() && !this.hasTakenVideo){
            validInput = false
        }

        println(pickupLocationSpinner.selectedItem.toString())

        val wpiLocationHelper = WPILocationHelper()
        var pickupLocation = wpiLocationHelper.locationNameToLocationObjectMap[pickupLocationSpinner.selectedItem.toString()]
        println(pickupLocation)
        if(pickupLocation == null){
            pickupLocation = wpiLocationHelper.getLocationOfGordonLibrary()
        }

        val item = Item(
            name = name,
            price = price,
            user = user,
            description = description,
            postedTimeStamp = currTime,
            stringsOfBitmapofPicuresOfItem = this.stringsOfBitmapsOfItems,
            pickupLocation = pickupLocation
        )

        //Adds item being sold to db
        if (validInput) {
            if(this.stringsOfBitmapsOfItems.isEmpty()){
                database.collection("Items").add(item).addOnSuccessListener { documentReference ->
                    val file = Uri.fromFile(this.videoFile)
                    val riversRef = storage.reference.child("${documentReference.id}/${file.lastPathSegment}")
                    val uploadTask = riversRef.putFile(file)
                    uploadTask.addOnFailureListener {
                        println("Could not upload video to storage!")
                    }.addOnSuccessListener {
                        println("Uploaded video to storage!")
                    }

                }
            } else {
                database.collection("Items").add(item)
            }

            startActivity(Intent(this, Home::class.java))
        } else {
            toast("One or more of the fields is/are invalid")
        }
    }
}
