package com.example.letitgoat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.letitgoat.ui.db_models.Item
import com.google.firebase.firestore.FirebaseFirestore
import org.jetbrains.anko.toast
import java.lang.NumberFormatException
import java.util.*

class AddingItemToMarketplace : AppCompatActivity() {

    private lateinit var database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adding_item_to_marketplace)

        database = FirebaseFirestore.getInstance()

        val addingItemToMarketplace = findViewById<Button>(R.id.addToMarketPlaceButton)
        addingItemToMarketplace.setOnClickListener{
            addItemToMarketplace()
        }

        val sellerName = findViewById<TextView>(R.id.sellerName)
        sellerName.text = "Being sold by: ${MainActivity.user!!.name}"
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

        val item = Item(
            name = name,
            price = price,
            user = user,
            description = description,
            postedTimeStamp = currTime
        )

        //Adds single_item to db
        if (validInput) {
            database.collection("Items").add(item)
            startActivity(Intent(this, Home::class.java))
        } else {
            toast("One or more of the fields is/are invalid")
        }
    }
}
