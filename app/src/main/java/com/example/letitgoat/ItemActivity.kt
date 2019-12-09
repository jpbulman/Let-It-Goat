package com.example.letitgoat

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.letitgoat.db_models.Item
import com.example.letitgoat.ui.sell.sell_recycler.SellViewAdapter

class ItemActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_on_market)
        val item = intent.getParcelableExtra("extra_item") as Item
        Log.d("ItemActivity", item.name)

        val img = findViewById<ImageView>(R.id.single_item_img)
        val name = findViewById<TextView>(R.id.single_item_name)
        val price = findViewById<TextView>(R.id.single_item_price)
        val description = findViewById<TextView>(R.id.single_item_description)

        name.setText(item.name)
        price.setText("$" + item.price.toString())
        description.setText(item.description)

        if(item.stringsOfBitmapofPicuresOfItem.size != 0) {
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
    }
}