package com.MAGLab.secmon_remote

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.io.IOException
import android.content.SharedPreferences as SharedPreferences

class MainActivity : AppCompatActivity() {

    private lateinit var switchAuto: SwitchCompat
    private lateinit var switchOn: SwitchCompat
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.app_main)
        toolbar = findViewById(R.id.toolbar)
        // The Toolbar defined in the layout has the id "my_toolbar".
        setSupportActionBar(toolbar)


        switchAuto = findViewById(R.id.switchAuto)
        switchOn = findViewById(R.id.switchOn)

        switchAuto.setOnClickListener {
            if (switchAuto.isChecked) {
                switchOn.setEnabled(false)
            } else {
                switchOn.setEnabled(true)
            }
            Thread {
                this.sendUdpPacket()
            }.start()
        }
        switchOn.setOnClickListener {
            Thread {
                this.sendUdpPacket()
            }.start()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.topbar_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            // User chooses the "Settings" item. Show the app settings UI.
            val tent : Intent = Intent(this, SettingsActivity::class.java)
            startActivity(tent)
            true
        }

        else -> {
            // The user's action isn't recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }
    override fun onPause() {
        super.onPause()
        val sp: SharedPreferences = getSharedPreferences("uwudpPref", MODE_PRIVATE)
        val edit: SharedPreferences.Editor = sp.edit()
        edit.putBoolean("auto", switchAuto.isChecked)
        edit.putBoolean("onOff", switchOn.isChecked)
        edit.apply()
    }

    override fun onResume() {
        super.onResume()
        val sh: SharedPreferences = getSharedPreferences("uwudpPref", MODE_PRIVATE)
        val auto: Boolean = sh.getBoolean("auto", false)
        val onOff: Boolean = sh.getBoolean("onOff", true)

        switchAuto.setChecked(auto)
        switchOn.setChecked(onOff)
    }

    private fun sendUdpPacket() {
        try {
            // Setup the UDP socket
            val socket = DatagramSocket()

            // Set the target IP address and port
            val sh : SharedPreferences = getSharedPreferences("uwudpSet", MODE_PRIVATE)
            val ip : String = sh.getString("IP", "").toString()
            val address: InetAddress = InetAddress.getByName(ip)
            val port = 11017

            // Data to send in the UDP packet
            val data: String = if(switchOn.isChecked()) "on" else "off"

            // Create the packet and send it
            val packet = DatagramPacket(data.toByteArray(), data.toByteArray().size, address, port)
            socket.send(packet)

            // Close the socket
            socket.close()

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}