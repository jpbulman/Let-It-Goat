package com.example.letitgoat

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.letitgoat.db_models.User
import com.github.kittinunf.fuel.Fuel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import org.jetbrains.anko.doAsync
import android.graphics.Bitmap
import android.util.Base64
import android.view.View
import java.io.ByteArrayOutputStream


class MainActivity : AppCompatActivity() {

    private lateinit var database: FirebaseFirestore
    private lateinit var context: Context

    //User object - just whoever logs into the app
    //Can be referenced anywhere in the app after the user logs in to see who is logged in
    companion object {
        var user: User = User(
            name = "dev",
            email = "notloggedin@wpi.edu",
            profilePicture = ""
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        doAsync {
            val defaultImageBitmap = BitmapFactory.decodeResource(resources, R.drawable.blank)
            val baos = ByteArrayOutputStream()
            defaultImageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
            val b = baos.toByteArray()
            val bitmapAsString = Base64.encodeToString(b, Base64.DEFAULT)

            user = User(
                name = user.name,
                email = user.email,
                profilePicture = bitmapAsString
            )
        }

        this.context = this

        setContentView(R.layout.activity_main)

        database = FirebaseFirestore.getInstance()

        val loginButton = findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener {
            login()
        }
    }

    fun login(){
        val unameField = findViewById<EditText>(R.id.usernameField)
        val pwdField = findViewById<EditText>(R.id.passwordField)
        
        doAsync{
            val username = unameField.text.toString()

            //DO NOT GET RID OF THESE TWO UNUSED VARIABLES
            //As stupid as it may be, Fuel, the HTTP-Request library, doesn't work if they're not there
            val (a, response, result) = Fuel.post("https://snow-magnesium.glitch.me/login",
                listOf("username" to username, "password" to pwdField.text.toString()))
                .responseString()
            /*
                Convert the login response to a JSON
                See https://github.com/jpbulman/Let-It-Goat-Server for details on what the response contains
            */
            val gson = Gson()
            val json = gson.fromJson(result.component1().toString(), Map::class.java)

            //If the user is authenticated
            if(json["auth"].toString().toBoolean()){
                val docRef = database.collection("Users").document(username)
                docRef.get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            user = User(
                                name = document.data?.get("name").toString(),
                                email = document.data?.get("email").toString(),
                                profilePicture = document.data?.get("profilePicture").toString()
                            )
                        } else {
                            val defaultImageBitmap = BitmapFactory.decodeResource(resources, R.drawable.blank)
                            val baos = ByteArrayOutputStream()
                            defaultImageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
                            val b = baos.toByteArray()
                            val bitmapAsString = Base64.encodeToString(b, Base64.DEFAULT)

                            //Create a DB data object
                            val currUser = User(
                                name = username,
                                email = json["name"].toString(),
                                profilePicture = bitmapAsString
                            )
                            user = currUser
                            //Add it to the DB
                            database.collection("Users").document(username).set(currUser)
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d("", "get failed with ", exception)
                    }

                //Go to the home screen
                val i = Intent(this@MainActivity, Home::class.java)
                startActivity(i)
            } else {
//                println(json["auth"])
                //Username or password is wrong - needs to be fixed
//                toast("Incorrect username or password")
//                val t = Toast.makeText(this@MainActivity,"asdf", Toast.LENGTH_LONG)
//                println("111111111111")
//                t.show()
                Log.e("","User authentication failed")
            }
        }
    }
}
