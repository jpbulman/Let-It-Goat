package com.example.letitgoat

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.letitgoat.db_models.Item
import com.example.letitgoat.ui.sell.sell_recycler.SellRecyclerFragment
import com.google.firebase.firestore.FirebaseFirestore
import org.jetbrains.anko.toast
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList


class AddingItemToMarketplace : AppCompatActivity() {
    var isOpened = false
    private lateinit var database: FirebaseFirestore
    private var stringsOfBitmapsOfItems: List<String> = ArrayList()

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

        Log.d("check_add_item", (intent.getSerializableExtra("extra_item")==null).toString())

        if (intent.getSerializableExtra("extra_item")!=null) {
            addButton.text = "Update Item From WPI Marketplace"

            val item = intent.getSerializableExtra("extra_item") as Item
            Log.d("ItemActivity", item.name)

            itemID = intent.getStringExtra("id")

            name.setText(item.name)
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

            }
        } else {
            deleteButton.visibility = View.INVISIBLE
            addButton.setOnClickListener{
                addItemToMarketplace()
            }

        }

        database = FirebaseFirestore.getInstance()





        val sellerName = findViewById<TextView>(R.id.sellerName)
        sellerName.text = "${MainActivity.user.name}"



        val spinner: Spinner = findViewById(R.id.spinner)
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.planets_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }

        val takePhotoButton = findViewById<ImageButton>(R.id.newItemPictureButton)
        takePhotoButton.setOnClickListener{
            dispatchTakePictureIntent()
        }
    }

    val REQUEST_IMAGE_CAPTURE = 1

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
        val category = findViewById<Spinner>(R.id.spinner)

        if(name == "" || description == ""){
            validInput = false
        }

        if(this.stringsOfBitmapsOfItems.isEmpty()){
            validInput = false
        }

        val item = Item(
            name = name,
            price = price,
            user = user,
            description = description,
            postedTimeStamp = currTime,
            stringsOfBitmapofPicuresOfItem = this.stringsOfBitmapsOfItems,
            category = category.selectedItem.toString()
        )

        //Adds single_buy to db
        if (validInput) {
            database.collection("Items").add(item)
            finish()
//            startActivity(Intent(this, Home::class.java))
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
        val category = findViewById<Spinner>(R.id.spinner)

        if(name == "" || description == ""){
            validInput = false
        }

//        if(this.stringsOfBitmapsOfItems.isEmpty()){
//            validInput = false
//        }


        val item = Item(
            name = name,
            price = price,
            user = user,
            description = description,
            postedTimeStamp = currTime,
            stringsOfBitmapofPicuresOfItem = this.stringsOfBitmapsOfItems,
            category = category.selectedItem.toString()
        )

        //Adds single_buy to db
        if (validInput) {
            database.collection("Items").document(itemID).set(item)
            finish()
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
            finish()
            Toast.makeText(applicationContext,"You have deleted the item: " + itemName,Toast.LENGTH_SHORT).show()
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
            .addOnGlobalLayoutListener(OnGlobalLayoutListener {
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
