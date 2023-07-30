package com.aanda.tomatodiseaseprediction

import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.Html.FROM_HTML_MODE_LEGACY
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ml.modeldownloader.CustomModel
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
import com.google.firebase.ml.modeldownloader.DownloadType
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader

class IntroActivity : AppCompatActivity() {
    var txtView:TextView? = null
    var btn:Button? = null
    var ed_name:EditText? = null
    @TargetApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_time_screen)
        btn = findViewById(R.id.btn1)
        val first:String = getColoredSpanned("Dr","#000000")
        val second:String = getColoredSpanned("Plant","#2F9A1E")
        txtView = findViewById(R.id.drplant)
//        ed_name = findViewById(R.id.name_tv)
//        val textEdit: Editable? = ed_name?.text
        txtView?.text = Html.fromHtml(first+""+second,FROM_HTML_MODE_LEGACY)
        val intent:Intent = Intent(this,MainActivity::class.java)
//        intent.putExtra("pname",textEdit.toString())
        btn?.setOnClickListener {
//            if(textEdit!!.isEmpty()){
//                Toast.makeText(this,"Please Enter Name!",Toast.LENGTH_SHORT).show()
//            }
                startActivity(intent)
                finish()
            }
    }
    private fun getColoredSpanned(text: String, color: String): String {
        return "<font color=$color>$text</font>"
    }
}