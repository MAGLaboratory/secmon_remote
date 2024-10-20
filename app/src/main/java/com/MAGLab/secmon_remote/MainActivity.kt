package com.MAGLab.secmon_remote

import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.util.Log
import android.widget.EditText
import android.widget.Switch
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.MAGLab.secmon_remote.ui.theme.MAGLabRemoteTheme
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.io.IOException

class MainActivity : ComponentActivity() {

    private lateinit var switchAuto: Switch
    private lateinit var switchOn: Switch
    private lateinit var ipAddress: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.app_main)

        switchAuto = findViewById(R.id.switchAuto)
        switchOn = findViewById(R.id.switchOn)
        ipAddress = findViewById(R.id.ipAddress)

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


    private fun sendUdpPacket() {
        try {
            // Setup the UDP socket
            val socket = DatagramSocket()

            // Set the target IP address and port
            val address: InetAddress = InetAddress.getByName(this.ipAddress.text.toString())
            val port = 11017

            // Data to send in the UDP packet
            val data: String
            data = if(switchOn.isChecked()) "on" else "off"

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