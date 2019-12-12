package com.example.letitgoat

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.letitgoat.util.LoginUtil
import com.example.letitgoat.db_models.User
import com.example.letitgoat.ui.CustomProgressBar
import com.github.kittinunf.fuel.Fuel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import org.jetbrains.anko.doAsync
import java.io.ByteArrayOutputStream


class MainActivity : AppCompatActivity() {
    val progressBar = CustomProgressBar()
    private lateinit var database: FirebaseFirestore
    private lateinit var context: Context
    private lateinit var loginButton: Button

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

        this.context = this
        database = FirebaseFirestore.getInstance()

        setContentView(R.layout.activity_main)

        loginButton = findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener {
            loginButton.isClickable = false
            login()
        }
    }

    fun login(){
        progressBar.show(this,"Please Wait...")
        val unameField = findViewById<EditText>(R.id.usernameField)
        val pwdField = findViewById<EditText>(R.id.passwordField)

        doAsync{
            var username = unameField.text.toString()
            if (!username.endsWith("@wpi.edu")) {
                username += "@wpi.edu"
            }

            //DO NOT GET RID OF THESE TWO UNUSED VARIABLES
            //As stupid as it may be, Fuel, the HTTP-Request library, doesn't work if they're not there
            val (a, response, result) = Fuel.post("https://snow-magnesium.glitch.me/login",
                listOf("username" to username, "password" to pwdField.text.toString()))
                .responseString()

            if (response.statusCode != 200) {
                loginButton.isClickable = true
                progressBar.dialog.dismiss()
                runOnUiThread{
                    makeToast("Server error...")
                }
                return@doAsync
            }
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
                        if (document.data != null) {
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
                                email = username,
                                name = json["name"].toString(),
                                profilePicture = bitmapAsString
                            )
                            user = currUser
                            //Add it to the DB
                            database.collection("Users").document(username).set(currUser)
                        }

                        LoginUtil.setLogin(context, user.name, user.email, user.profilePicture)
                        //Go to the home screen
                        goToHomepage()
                        progressBar.dialog.dismiss()
                    }
                    .addOnFailureListener { exception ->
                        loginButton.isClickable = true
                        progressBar.dialog.dismiss()
                        runOnUiThread {
                            makeToast("User doesn't exist/ Wrong password...")
                        }
                        Log.d("", "get failed with ", exception)
                    }


            } else {
                loginButton.isClickable = true
                progressBar.dialog.dismiss()
                runOnUiThread {
                    makeToast("User doesn't exist/ Wrong password...")
                }

                Log.e("","User authentication failed")
            }
        }
    }

    fun makeToast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }

    fun goToHomepage() {
        val i = Intent(this@MainActivity, Home::class.java)
        startActivity(i)
    }

    override fun onBackPressed() {
        if (LoginUtil.isLogin(context)) {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        if (LoginUtil.isLogin(context)) {
            user = User(
                name = LoginUtil.getUserName(context),
                email = LoginUtil.getUserEmail(context),
                profilePicture = LoginUtil.getUserSelfie(context)
            )
            goToHomepage()
        }
    }


}
