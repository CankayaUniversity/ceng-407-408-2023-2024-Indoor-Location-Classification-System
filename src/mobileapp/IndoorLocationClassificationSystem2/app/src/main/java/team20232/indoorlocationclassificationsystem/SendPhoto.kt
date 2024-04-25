package team20232.indoorLocationClassificationSystem

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import team20232.indoorLocationClassificationSystem.databinding.ActivitySendPhotoBinding
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import team20232.indoorlocationclassificationsystem.IMAGE_URI_EXTRA
import team20232.indoorlocationclassificationsystem.showMap
import java.io.ByteArrayOutputStream
import java.io.InputStream


@Suppress("DEPRECATION")
class SendPhoto : AppCompatActivity() {

    data class UploadResponse(val message: String)

    interface ApiService {
        @POST("upload_photo")
        fun uploadPhoto(@Body encodedImage: String): Call<UploadResponse>
        @GET("get_photo") // Sunucu tarafında tanımlanan endpoint
        fun getPhoto(): Call<ResponseBody>
    }



    private lateinit var imageView: ImageView
    lateinit var binding: ActivitySendPhotoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_photo)

        try {
            imageView = findViewById(R.id.picked_image)
            //val imageUri = intent.getParcelableExtra<Uri>(IMAGE_URI_EXTRA)
            val imageUriString = intent.getStringExtra(IMAGE_URI_EXTRA)
            val imageUri = Uri.parse(imageUriString)

            val inputStream = contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val originalWidth = bitmap.width
            val originalHeight = bitmap.height

            // Kırpma boyutunu belirleme (en büyük karesel alana göre)
            val cropSize = if (originalWidth > originalHeight) originalHeight else originalWidth

            // Bitmap'i en büyük karesel alana göre kırpma
            val croppedBitmap = Bitmap.createBitmap(bitmap, (originalWidth - cropSize) / 2, (originalHeight - cropSize) / 2, cropSize, cropSize)

            // Bitmap'i 512x512 boyutuna dönüştürme
            val scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, 512, 512, true)

            // ImageView'e dönüştürülmüş Bitmap'i ayarlama
            imageView.setImageBitmap(scaledBitmap)

            val retakeBtn = findViewById<Button>(R.id.retakeBtn)
            retakeBtn.setOnClickListener {
                onBackPressed() // Geri dön
            }

            val sendBtn = findViewById<Button>(R.id.sendBtn)
            sendBtn.setOnClickListener {
                sendPhotoToServer(scaledBitmap)
                intent = Intent(applicationContext, showMap::class.java)
                startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sendPhotoToServer(bitmap: Bitmap) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        val encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT)

        val retrofit =
            Retrofit.Builder()
                .baseUrl("http://127.0.0.1:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        val service = retrofit.create(ApiService::class.java)
        val request = service.uploadPhoto(encodedImage)
        val onResponseCallback = object : Callback<UploadResponse> {
            override fun onResponse(call: Call<UploadResponse>, response: Response<UploadResponse>) {
                if (response.isSuccessful) {
                    val uploadResponse = response.body()


                    // Sunucudan gelen yanıtı işleyebilirsiniz, eğer varsa
                    Log.d("SendPhoto", "Fotoğraf başarıyla yüklendi: ${uploadResponse?.message}")

                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("SendPhoto", "Photo upload failed: $errorBody")
                }
            }

            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                TODO("Not yet implemented")
            }
        }

        val onFailureCallback = object : Callback<UploadResponse> {
            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                Log.e("SendPhoto", "Bağlantı hatası: ${t.message}", t)
            }

            override fun onResponse(call: Call<UploadResponse>, response: Response<UploadResponse>) {
                // onResponse callback içinde zaten işlediğimiz için burada bir şey yapmamıza gerek yok
            }
        }
        getPhotoFromServer()
    }



    private fun getPhotoFromServer() {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://127.0.0.1:5000/") // Sunucu adresini buraya girin
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)
        val call = service.getPhoto()

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val inputStream: InputStream? = response.body()?.byteStream()
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    val imageView = findViewById<ImageView>(R.id.showMap)
                    imageView.setImageBitmap(bitmap)
                } else {
                    // Sunucudan fotoğraf alınamadı
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // Bağlantı hatası
            }
        })
    }
}


