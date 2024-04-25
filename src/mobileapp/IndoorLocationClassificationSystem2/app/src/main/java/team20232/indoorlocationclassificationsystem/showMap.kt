package team20232.indoorlocationclassificationsystem

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import team20232.indoorLocationClassificationSystem.R


class showMap : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_map)



        val newPhotoBtn = findViewById<Button>(R.id.newPhotoBtn)
        newPhotoBtn.setOnClickListener {
            intent = Intent(applicationContext, TakePhoto::class.java)
            startActivity(intent)
        }



    }
}