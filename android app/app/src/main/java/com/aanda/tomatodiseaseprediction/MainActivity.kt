package com.aanda.tomatodiseaseprediction

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.constraintlayout.widget.Group
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.get
import com.aanda.tomatodiseaseprediction.databinding.ActivityMainBinding
import com.google.firebase.ml.modeldownloader.CustomModel
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
import com.google.firebase.ml.modeldownloader.DownloadType
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.sql.Time
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val CAMERA_PERMISSION_CODE = 1
        private const val CAMERA_REQUEST_CODE = 2
        private const val GALLERY_PERMISSION_CODE = 3
    }
    //using binding so we can only that activity contents
    private var binding:ActivityMainBinding? = null
    private final val classes:Array<String> = arrayOf("Tomato Bacterial Spot","Tomato Early Blight","Tomato Late Blight","Tomato Leaf Mold",
            "Tomato Septoria Leaf Spot",
            "Tomato Spider Mites Two Spotted Spider Mite",
            "Tomato Target Spot",
            "Tomato Tomato YellowLeaf Curl Virus",
            "Tomato Tomato Mosaic Virus",
            "Tomato Healthy")
    var ImageDialog:Dialog? = null
    var myModel:CustomModel? = null
    var interpreter:Interpreter? = null
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
            //by using viewBinding we not need to create any variable we can directly access using it's id
        val conditions = CustomModelDownloadConditions.Builder().build()
        FirebaseModelDownloader.getInstance().getModel("model",
            DownloadType.LOCAL_MODEL_UPDATE_IN_BACKGROUND,conditions).addOnSuccessListener {
                model: CustomModel? -> Log.i("MODEL STATUS","Model Downloaded")
//            Toast.makeText(this,"ModelDownloaded", Toast.LENGTH_SHORT).show()
            myModel = model
        }
            .addOnFailureListener {
//                Log.i("MODEL STATUS","Model Download Failed")
                Toast.makeText(this,"ModelDownloadFailed", Toast.LENGTH_SHORT).show()
            }
        val calender:Calendar = Calendar.getInstance()
        val hours = calender.get(Calendar.HOUR_OF_DAY)
        when (hours) {
            in 3..11 -> {
                binding?.greet?.text = "Morning!"
            }
            in 12..17 -> {
                binding?.greet?.text = "Afternoon!"
            }
            else -> {
                binding?.greet?.text = "Evening!"
            }
        }
        binding?.chooseBtn?.setOnClickListener{
            val modelFile = myModel?.file
            if(modelFile!=null) {
                interpreter = Interpreter(modelFile)
                showAlertDialog()
//                if (ContextCompat.checkSelfPermission(
//                        this,
//                        Manifest.permission.CAMERA
//                    ) == PackageManager.PERMISSION_GRANTED
////                ) {
//                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//                    resultLauncher.launch(intent)
//                    }
//                else {
//                    ActivityCompat.requestPermissions(
//                        this, arrayOf(Manifest.permission.CAMERA),
//                        CAMERA_PERMISSION_CODE
//                    )
//                }
            }
            else{
                Toast.makeText(this,"Please Check Your Internet" +" Try opening the app again", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                resultLauncher.launch(intent);

            }
            else if(requestCode== GALLERY_PERMISSION_CODE){
                if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    val galleryIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    galleryLauncher.launch(galleryIntent)
                }
            }
            else {
                Toast.makeText(
                    this,
                    "You just denied permission." + "You can allow it on settings.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Bitmap = result.data!!.extras!!.get("data") as Bitmap
            Log.i("IMAGE","IMAGE_EXTRACTED")
            makePredictions(interpreter,data)
        }
    }
    var galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result->if(result.resultCode==Activity.RESULT_OK){
            val data = result.data
        if(data!=null){
            val contentURI = data.data
            try{
                val selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,contentURI)
                Log.i("IMAGE","ImageExtractedFromGallery")
                makePredictions(interpreter,selectedImageBitmap)
            }
            catch (e:Exception){
                e.printStackTrace()
                Toast.makeText(this,"Failed! To load image",Toast.LENGTH_SHORT).show()
            }
        }
    }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
    private fun showAlertDialog(){
        val imgDialog = AlertDialog.Builder(this)
        imgDialog.setTitle("Select Action")
        val imgDialogItems = arrayOf("Select From Gallery","Select From Camera")
        imgDialog.setItems(imgDialogItems){
            dialog,which->
            when(which){
                0->{
                    if(ContextCompat.checkSelfPermission(
                            this,Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED
                    ){
                        val galleryIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        galleryLauncher.launch(galleryIntent)
                    }
                    else{
                        ActivityCompat.requestPermissions(
                            this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            GALLERY_PERMISSION_CODE
                        )
                    }
                }
                1->{
                    if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    resultLauncher.launch(intent)
                    }
                else {
                    ActivityCompat.requestPermissions(
                        this, arrayOf(Manifest.permission.CAMERA),
                        CAMERA_PERMISSION_CODE
                    )
                }
                }
            }
        }
        imgDialog.show()
    }
    private fun makePredictions(interpreter: Interpreter?,bitmap: Bitmap){
        //1 256 256 3
        // 1 10
        //['Tomato_Bacterial_spot', 'Tomato_Early_blight', 'Tomato_Late_blight', 'Tomato_Leaf_Mold', 'Tomato_Septoria_leaf_spot',
        // 'Tomato_Spider_mites_Two_spotted_spider_mite', 'Tomato__Target_Spot', 'Tomato__Tomato_YellowLeaf__Curl_Virus', 'Tomato__Tomato_mosaic_virus', 'Tomato_healthy']
        val bitmapScaled = Bitmap.createScaledBitmap(bitmap,256,256,true)
        val input = ByteBuffer.allocateDirect(256*256*3*4).order(ByteOrder.nativeOrder())
        for(y in 0 until 256){
            for(x in 0 until 256){
                val px = bitmapScaled.get(x,y)
                val r = Color.red(px).toFloat()
                val g = Color.green(px).toFloat()
                val b = Color.blue(px).toFloat()
                input.putFloat(r)
                input.putFloat(g)
                input.putFloat(b)
            }
        }
        val bufferSize = 10 * java.lang.Float.SIZE/java.lang.Byte.SIZE
        val modelOutput = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder())
        interpreter?.run(input,modelOutput)
        modelOutput.rewind()
        val predictionProbabilities = modelOutput.asFloatBuffer()
        var maxIndex:Int = 0
        var maxValue:Float = 0.00f
        for (i in 0 until 10){
            if(predictionProbabilities[i]>maxValue){
                maxValue = predictionProbabilities[i]
                maxIndex = i
            }
        }
        predictionAlertDialog(maxIndex,maxValue)
    }
    @SuppressLint("SetTextI18n")
    private fun predictionAlertDialog(predictedIndex:Int, value:Float){
        val predictedDialog = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_screen,null)
        val disease_tv = view.findViewById<TextView>(R.id.message)
        disease_tv.text = classes[predictedIndex]
        val accuracy_tv = view.findViewById<TextView>(R.id.accuracy)
        val accuracy = value
        accuracy_tv.text = "Accuracy:${accuracy}"
//        val dissmiss = view.findViewById<Button>(R.id.dissmiss_btn)
//        dissmiss.setOnClickListener{
//            predictedDialog.se
//        }
        predictedDialog.setView(view)
        predictedDialog.setPositiveButton("GO BACK") { dialog, _ -> dialog.dismiss() }
        predictedDialog.show()
    }
}