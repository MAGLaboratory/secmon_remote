package com.MAGLab.secmon_remote

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.io.IOException
import java.net.SocketTimeoutException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import android.content.SharedPreferences as SharedPreferences

class MainActivity : AppCompatActivity() {

    private lateinit var switchAuto: SwitchCompat
    private lateinit var switchOn: SwitchCompat
    private lateinit var toolbar: Toolbar
    private lateinit var buttonRefresh: Button
    private lateinit var buttonRestart: Button
    private lateinit var textResponse: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.app_main)
        // find content and assign to local variables
        toolbar = findViewById(R.id.toolbar)
        switchAuto = findViewById(R.id.switchAuto)
        switchOn = findViewById(R.id.switchOn)
        buttonRefresh = findViewById(R.id.buttonRefresh)
        buttonRestart = findViewById(R.id.buttonRestart)
        textResponse = findViewById(R.id.textResponse)

        // functions for the content
        // set toolbar as action bar
        setSupportActionBar(toolbar)

        switchAuto.setOnClickListener {
            if (switchAuto.isChecked) {
                switchOn.setEnabled(false)
            } else {
                switchOn.setEnabled(true)
            }
            this.udpAction()
        }
        switchOn.setOnClickListener {
            this.udpAction()
        }
        buttonRefresh.setOnClickListener {
            this.udpAction()
        }

        buttonRestart.setOnClickListener {
            this.udpAction(true)
        }
    }

    private fun udpAction(restart: Boolean = false) {
        textResponse.text = ""
        Thread {
            this.excUdpPackets(restart)
        }.start()
    }
    // function to add buttons to the top toolbar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.topbar_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    // function to process what was pressed on the top toolbar
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            // User chooses the "Settings" item. Show the app settings UI.
            val activitySettings : Intent = Intent(this, SettingsActivity::class.java)
            startActivity(activitySettings)
            true
        }

        R.id.action_display -> {
            // User chooses the "Display" item.  Show display UI
            val activityDisplay : Intent = Intent(this, DisplayActivity::class.java)
            startActivity(activityDisplay)
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
        // assumes that the switch is enabled on startup
        if (auto) {
            switchOn.setEnabled(false)
        }
        switchOn.setChecked(onOff)
    }

    private fun excUdpPackets(restart: Boolean) {
        // Setup the UDP socket
        val socket = DatagramSocket()
        socket.soTimeout = 1000 // set a socket timeout so that receives time out

        // Set the target IP address and port
        val sh : SharedPreferences = getSharedPreferences("uwudpSet", MODE_PRIVATE)
        val ip : String = sh.getString("IP", "").toString()
        val address: InetAddress = InetAddress.getByName(ip)
        val port = 11017

        // Data to send in the UDP packet
        val jsonObject : JSONObject = JSONObject()
        jsonObject.put("auto", switchAuto.isChecked)
        if (!switchAuto.isChecked) {
            jsonObject.put("force", switchOn.isChecked)
        }
        if (restart) {
            jsonObject.put("restart", true)
        }
        jsonObject.put("time", System.currentTimeMillis() / 1000L)
        val jstring: String = jsonObject.toString()
        val xToken = TokenUtil.extractToken(sh.getString("Token", "").toString())
        val ac: Mac = Mac.getInstance("HmacSHA256")
        ac.init(SecretKeySpec(xToken, "HmacSHA256"))
        ac.update(jstring.toByteArray(Charsets.UTF_8))
        val data: String = Pair(jstring, Base64.encodeToString(ac.doFinal(), Base64.NO_PADDING).trimEnd()).toString()

        // Create the packet and send it
        val packet = DatagramPacket(data.toByteArray(), data.toByteArray().size, address, port)

        try {
            socket.send(packet)
	        // Receive the server response 
            val rxd : ByteArray = ByteArray(1024)
	        val rxp: DatagramPacket = DatagramPacket(rxd, rxd.size)
	        socket.receive(rxp)
	        val rxs : String = rxd.decodeToString().substringBefore("\u0000")
	        Log.i("UDP", "Received: $rxs")
            textResponse.post(Runnable { textResponse.text = getString(R.string.udpR, rxs) })

        } catch (e: SocketTimeoutException) {
            // normal if no response received.  ignore it.
            Log.i("UDP", "Socket receive timed out.  Server did not respond.")
            textResponse.post(Runnable { textResponse.text = getString(R.string.udpNR) })
        } catch (e: IOException) {
            // exception encountered
            e.printStackTrace()
        } finally {
            // Close the socket
            socket.close()
        }
    }
}
