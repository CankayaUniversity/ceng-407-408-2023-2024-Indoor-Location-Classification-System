package team20232.indoorlocationclassificationsystem

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import team20232.indoorLocationClassificationSystem.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnstart.setOnClickListener{
            intent = Intent(applicationContext, TakePhoto::class.java)
            startActivity(intent)
        }


    }
}