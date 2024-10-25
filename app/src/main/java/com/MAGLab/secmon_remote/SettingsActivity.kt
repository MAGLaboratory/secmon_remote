package com.MAGLab.secmon_remote

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.util.Base64
import android.util.Log
import android.widget.AutoCompleteTextView
import java.nio.ByteBuffer
import java.util.Locale
import java.util.zip.CRC32

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

    // this is adapted from the code on the maglab repository
    // https://github.com/MAGLaboratory/token_generator/blob/zero/decode.py
    fun checkToken(token: String): ByteArray {
        var centralTokenBytes: ByteArray
        try {
            val start = "magld_"
            // length verification
            require(token.length >= start.length + 8) {"Invalid token length"}

            // header verification
            require(token.substring(0, start.length).uppercase() == start.uppercase()) {"Invalid token header"}

            // retrieve token in bytes
            //  last 5 characters are reserved for crc32
            val centralToken = token.substring(start.length, token.length - 6)
            centralTokenBytes = Base64.decode(centralToken.toByteArray(Charsets.UTF_8), Base64.NO_PADDING)
            val endChecksum = token.substring(token.length - 6)

            val crc32 = CRC32()
            crc32.reset()
            crc32.update(centralTokenBytes)
            // by default, the crc32 function outputs a "Long" which is actually 8 bytes instead of the standard 4
            // we take the last 4 bytes because that is where the data is encoded...  this is
            // reversed because this part is encoded as little-endian (instead of the big-endian that base64 defaults to)
            val checksumBytes = ByteBuffer.allocate(Long.SIZE_BYTES).putLong(crc32.value).array().takeLast(4).reversed().toByteArray()
            val calcChecksum = Base64.encodeToString(checksumBytes, Base64.NO_PADDING).trimEnd()

            require(calcChecksum == endChecksum) {"Checksum Incorrect"}
        } catch (e: IllegalArgumentException) {
            Log.i("TokenDecode", e.message.toString())
            return byteArrayOf()
        }

        return centralTokenBytes
    }

    override fun onPause() {
        super.onPause()
        // uwudpSet is shorthand for UDP settings, not an action
        val sp: SharedPreferences = getSharedPreferences("uwudpSet", MODE_PRIVATE)
        val edit: SharedPreferences.Editor = sp.edit()
        edit.putString("IP", textIP.text.toString())
        val token = checkToken(textToken.text.toString())
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