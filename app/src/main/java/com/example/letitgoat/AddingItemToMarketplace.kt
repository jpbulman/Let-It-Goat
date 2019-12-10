package com.example.letitgoat

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
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
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.google.android.gms.location.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_adding_item_to_marketplace.*
import java.io.File
import kotlin.concurrent.thread


class AddingItemToMarketplace : AppCompatActivity() {
    var isOpened = false
    private lateinit var database: FirebaseFirestore
    private var stringsOfBitmapsOfItems: List<String> = ArrayList()
    private var videoFile: File = File("")

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var storage: FirebaseStorage

    private var hasTakenVideo = false

    lateinit var img: ImageView
    lateinit var name: TextView
    lateinit var price: TextView
    lateinit var description: TextView
    lateinit var addButton: Button
    lateinit var deleteButton: Button

    lateinit var itemID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adding_item_to_marketplace)

        img = findViewById<ImageView>(R.id.itemAboutToBeSoldPicture)
        name = findViewById<TextView>(R.id.itemNameField)
        price = findViewById<TextView>(R.id.priceField)
        description = findViewById<TextView>(R.id.descriptionField)
        addButton = findViewById<Button>(R.id.addToMarketPlaceButton)
        deleteButton = findViewById<Button>(R.id.deleteFromMarketPlaceButton)

        setListnerToRootView()

        if (intent.getParcelableExtra<Item>("extra_item") != null) {
            addButton.text = "Update Item On Marketplace"

            val item = intent.getParcelableExtra("extra_item") as Item
            Log.d("ItemActivity", item.name)
            itemID = intent.getStringExtra("id")

            val img = findViewById<ImageView>(R.id.itemAboutToBeSoldPicture)
            val name = findViewById<TextView>(R.id.itemNameField)
            val price = findViewById<TextView>(R.id.priceField)
            val description = findViewById<TextView>(R.id.descriptionField)

            name.setText(item.name)
            name.isFocusable = false
            name.setOnClickListener{
                Toast.makeText(applicationContext,"You can not modify item name...",Toast.LENGTH_SHORT).show()
            }
            price.setText(item.price.toString())
            description.setText(item.description)

            addButton.setOnClickListener{
                updateItemToMarketplace()
            }

            deleteButton.setOnClickListener{
                deleteItemToMarketplace(item.name)
            }

            if (item.stringsOfBitmapofPicuresOfItem.size != 0) {
                val encodeByte: ByteArray = Base64.decode(
                    item.stringsOfBitmapofPicuresOfItem.get(0),
                    Base64.DEFAULT
                )
                val b = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)

                val matrix = Matrix()

                matrix.postRotate(90f)

                val scaledBitmap = Bitmap.createScaledBitmap(b, b.width, b.height, true)

                val rotatedBitmap = Bitmap.createBitmap(
                    scaledBitmap,
                    0,
                    0,
                    scaledBitmap.width,
                    scaledBitmap.height,
                    matrix,
                    true
                )

                img.setImageBitmap(
                    rotatedBitmap
                )

                thread {
                    val baos = ByteArrayOutputStream()
                    rotatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
                    val b = baos.toByteArray()
                    val bitmapAsString = Base64.encodeToString(b, Base64.DEFAULT)
                    this.stringsOfBitmapsOfItems += bitmapAsString }

            }
        } else {
            deleteButton.visibility = View.INVISIBLE
            addButton.setOnClickListener{
                addItemToMarketplace()
            }

            val sellerName = findViewById<TextView>(R.id.sellerName)
            sellerName.text = "${MainActivity.user.name}"
        }

        database = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        val spinnercategories: Spinner = findViewById(R.id.spinner_categories)
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.categories_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinnercategories.adapter = adapter
        }

        val locationHelper = WPILocationHelper()
        val spinner = findViewById<Spinner>(R.id.pickupLocationSpinner)
        val locationSpinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            locationHelper.listOfLocationAsStrings
        )
        spinner.adapter = locationSpinnerAdapter

        val takePhotoButton = findViewById<ImageButton>(R.id.newItemPictureButton)
        takePhotoButton.setOnClickListener{
            dispatchTakePictureIntent()
        }

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
            val params = picOfAboutToSellItem.layoutParams as LinearLayout.LayoutParams
            params.width = 800
            params.height = 600

            val btnparams = buttonHolder.layoutParams as LinearLayout.LayoutParams
            btnparams.setMargins(25, 150, 25, 25)
            buttonHolder.layoutParams = btnparams

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
        val category = findViewById<Spinner>(R.id.spinner_categories).selectedItem.toString()

        if(name == "" || description == ""){
            validInput = false
        }

        if(this.stringsOfBitmapsOfItems.isEmpty() && !this.hasTakenVideo){
            validInput = false
        }

        val wpiLocationHelper = WPILocationHelper()
        var pickupLocation = wpiLocationHelper.locationNameToLocationObjectMap[pickupLocationSpinner.selectedItem.toString()]
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
            pickupLocation = pickupLocation,
            category = category
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
            Toast.makeText(applicationContext,"You have created the item: " + name,Toast.LENGTH_LONG).show()
        } else {
            toast("One or more of the fields is/are invalid")
        }
    }

    private fun updateItemToMarketplace(){
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
        val category = findViewById<Spinner>(R.id.spinner_categories)

        if(name == "" || description == ""){
            validInput = false
        }

        val wpiLocationHelper = WPILocationHelper()
        var pickupLocation = wpiLocationHelper.locationNameToLocationObjectMap[pickupLocationSpinner.selectedItem.toString()]
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
            pickupLocation = pickupLocation,
            category = category.selectedItem.toString()
        )

        //Adds single_buy to db
        if (validInput) {
            database.collection("Items").document(itemID).set(item)
            startActivity(Intent(this, Home::class.java))
            Toast.makeText(applicationContext,"You have updated the item: " + name,Toast.LENGTH_LONG).show()
        } else {
            toast("One or more of the fields is/are invalid")
        }
    }

    private fun deleteItemToMarketplace(itemName: String){
        val builder = AlertDialog.Builder(this)

        // Set the alert dialog title
        builder.setTitle("Warning")

        // Display a message on alert dialog
        builder.setMessage("Do you want to delete the item?")

        // Set a positive button and its click listener on alert dialog
        builder.setPositiveButton("YES"){dialog, which ->
            // Do something when user press the positive button
            database.collection("Items").document(itemID).delete()
            startActivity(Intent(this, Home::class.java))
            Toast.makeText(applicationContext,"You have deleted the item: " + itemName,Toast.LENGTH_LONG).show()
        }

        // Display a negative button on alert dialog
        builder.setNegativeButton("No"){dialog,which ->
            dialog.dismiss()
        }

        // Finally, make the alert dialog using builder
        val dialog: AlertDialog = builder.create()

        // Display the alert dialog on app interface
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.DKGRAY)
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLUE)
    }

    fun setListnerToRootView() {
        val activityRootView: View = window.decorView.findViewById(android.R.id.content)
        activityRootView.getViewTreeObserver()
            .addOnGlobalLayoutListener(ViewTreeObserver.OnGlobalLayoutListener {
                val heightDiff: Int =
                    activityRootView.getRootView().getHeight() - activityRootView.getHeight()
                Log.d("ItemActivity", "" + heightDiff)
                if (heightDiff > 500) { // 99% of the time the height diff will be due to a keyboard.
                    if (!isOpened) { //Do two things, make the view top visible and the editText smaller
                        addButton.visibility = View.INVISIBLE
                        deleteButton.visibility = View.INVISIBLE
                    }
                    isOpened = true
                } else if (isOpened) {
                    addButton.visibility = View.VISIBLE
                    deleteButton.visibility = View.VISIBLE
                    isOpened = false
                }
            })
    }
}
