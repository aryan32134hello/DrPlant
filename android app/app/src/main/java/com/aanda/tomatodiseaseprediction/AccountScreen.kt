package com.aanda.tomatodiseaseprediction

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.widget.EditText

class AccountScreen : AppCompatActivity() {
    var name_et:EditText? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_screen)
        name_et = findViewById(R.id.name_tv)
        val bundle:Bundle? = intent.extras
        val name: String? = bundle?.getString("pname")
        name_et?.setText(name)
    }
}