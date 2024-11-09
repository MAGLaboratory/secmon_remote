package com.MAGLab.secmon_remote

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.AutoCompleteTextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SettingsActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var textIP: AutoCompleteTextView
    private lateinit var textToken: AutoCompleteTextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Toolbar configuration
        //  The back button is added and the title of this activity is "Settings"
        toolbar = findViewById(R.id.toolbar2)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Settings"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener{onBackPressedDispatcher.onBackPressed()}

        textIP = findViewById(R.id.textIP)
        textToken = findViewById(R.id.textToken)
    }

    override fun onPause() {
        super.onPause()
        // uwudpSet is shorthand for UDP settings, not an action
        val sp: SharedPreferences = getSharedPreferences("uwudpSet", MODE_PRIVATE)
        val edit: SharedPreferences.Editor = sp.edit()
        edit.putString("IP", textIP.text.toString())
        val token = TokenUtil.checkToken(textToken.text.toString())
        if (token.isNotEmpty())
        {
            edit.putString("Token" ,textToken.text.toString())
        }
        edit.apply()
    }

    override fun onResume() {
        super.onResume()
        val sh: SharedPreferences = getSharedPreferences("uwudpSet", MODE_PRIVATE)
        val ip: String = sh.getString("IP", "").toString()
        textIP.setText(ip)
        val token: String = sh.getString("Token", "").toString()
        textToken.setText(token)
    }
}