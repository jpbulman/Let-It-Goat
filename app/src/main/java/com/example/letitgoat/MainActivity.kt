package com.example.letitgoat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.letitgoat.ui.db_models.User
import com.github.kittinunf.fuel.Fuel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import org.jetbrains.anko.doAsync

class MainActivity : AppCompatActivity() {

    private lateinit var database: FirebaseFirestore

    //Static user object - just whoever logs into the app
    //Can be referenced anywhere in the app after the user logs in to see who is logged in
    companion object {
        var user: User? = User(email="dev@wpi.edu", name="Mr. Dev")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

            val (request, response, result) = Fuel.post("https://snow-magnesium.glitch.me/login",
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

                //Create a DB data object
                val currUser = User(username, json["name"].toString())
                user = currUser
                database.collection("Users").document(username).set(currUser)

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
