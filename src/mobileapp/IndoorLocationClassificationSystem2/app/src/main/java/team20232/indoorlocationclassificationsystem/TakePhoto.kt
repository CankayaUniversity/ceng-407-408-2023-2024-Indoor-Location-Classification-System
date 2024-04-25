package team20232.indoorlocationclassificationsystem

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.concurrent.futures.await
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import team20232.indoorLocationClassificationSystem.databinding.ActivityTakePhotoBinding
import java.lang.Exception
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import team20232.indoorLocationClassificationSystem.SendPhoto
import java.io.File
import java.io.FileOutputStream

const val IMAGE_URI_EXTRA = "image_uri_extra"

@Suppress("DEPRECATION")
class TakePhoto : AppCompatActivity() {
    private  val GALLERY_REQUEST_CODE = 100
    private lateinit var viewBinding: ActivityTakePhotoBinding
    private var imageCapture:ImageCapture? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityTakePhotoBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        if (!hasPermissions(baseContext)) {
            activityResultLauncher.launch(REQUIRED_PERMISSIONS)
        } else {
            lifecycleScope.launch {
                startCamera()
            }
        }
        viewBinding.imageCaptureButton.setOnClickListener {
            viewBinding.imageCaptureButton.isEnabled = false
            takePhoto()
        }



        viewBinding.galeriBtn.setOnClickListener {
            viewBinding.galeriBtn.isEnabled = false

            galleryLauncher.launch("image/*") // Galeriye erişim için izin isteği gönderilir
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        // Galeriden seçilen resmin URI'si buraya geliyor
        uri?.let {
            // Seçilen resmi işlemek için yapmak istediğiniz işlemleri burada gerçekleştirin
            val intent = Intent(this, SendPhoto::class.java)
            intent.putExtra(IMAGE_URI_EXTRA, it.toString())
            startActivity(intent)
        }
        viewBinding.galeriBtn.isEnabled = true
    }


    private suspend fun startCamera(){
        val cameraProvider = ProcessCameraProvider.getInstance(this).await()

        val preview = Preview.Builder().build()
        preview.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)

        imageCapture = ImageCapture.Builder()
            .build()
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try{
            cameraProvider.unbindAll()
            var camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageCapture
            )
        } catch (e: Exception){
            Log.e(TAG,"UseCase binding failed", e)
        }
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val photoFile = File(externalMediaDirs.firstOrNull(), "${System.currentTimeMillis()}.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    try {
                        // Kaydedilen fotoğrafın URI'sini al
                        val savedUri = Uri.fromFile(photoFile)
                        val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)

                        // Bitmap'i sağa 90 derece çevir
                        val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply { postRotate(90f) }, true)

                        // Yeni bir dosya oluştur
                        val rotatedPhotoFile = File(externalMediaDirs.firstOrNull(), "${System.currentTimeMillis()}_rotated.jpg")

                        // Rotasyon sonrası bitmap'i dosyaya kaydet
                        FileOutputStream(rotatedPhotoFile).use { out ->
                            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out) // JPEG formatında sıkıştırılmış veriyi dosyaya yaz
                        }

                        // Rotasyon sonrası dosyanın URI'sini al
                        val rotatedUri = Uri.fromFile(rotatedPhotoFile)

                        // Yeni bir Intent oluştur ve SendPhoto aktivitesine gönder
                        val intent = Intent(this@TakePhoto, SendPhoto::class.java)
                        intent.putExtra(IMAGE_URI_EXTRA, rotatedUri.toString())
                        // Intent'i başlat
                        startActivity(intent)

                        // Başarı mesajını logla
                        Log.e(TAG, "Photo saved successfully")
                    } catch (e: Exception) {
                        // Hata durumunda logla
                        Log.e(TAG, "Failed to save photo: ${e.message}", e)
                    }
                    viewBinding.imageCaptureButton.isEnabled = true
                }
                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                }
            }
        )
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())
        {permissions ->

            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS  && it.value == false)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(this, "Permission request denied", Toast.LENGTH_LONG).show()
            } else {
                lifecycleScope.launch {
                    startCamera()
                }
            }
        }
    companion object{

        private const val TAG = "CameraXApp"
        private const val FILE_FORMAT = "yyyy-MM-dd-HH-mm-ss-SS"
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                android.Manifest.permission.CAMERA
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
        fun hasPermissions(context: Context) = REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

}