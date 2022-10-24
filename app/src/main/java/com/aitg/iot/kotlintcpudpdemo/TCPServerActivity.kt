package com.aitg.iot.kotlintcpudpdemo

import android.net.wifi.WifiManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.*
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.UnknownHostException
import java.nio.ByteBuffer
import java.nio.ByteOrder


class TCPServerActivity : AppCompatActivity() {
    private var mThread: Thread? = null
    private var tvMessages: TextView? = null
    private var etMessage: EditText? = null
    private val SERVER_PORT = 5500 //傳送埠號
    private var message: String? = null
    private var reader: BufferedReader? = null
    private var socket: Socket? = null
    private var tmp: String? = null
    private var out: PrintWriter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tcp_server)
        val tvIP = findViewById<TextView>(R.id.tvIP)
        val tvPort = findViewById<TextView>(R.id.tvPort)
        tvMessages = findViewById(R.id.tvMessages)
        etMessage = findViewById(R.id.etMessage)
        val btnSend = findViewById<Button>(R.id.btnSend)
        try {
            val SERVER_IP = localIpAddress
            tvIP.text = "IP: $SERVER_IP"
        } catch (e: UnknownHostException) {
            e.printStackTrace()
        }
        mThread = Thread(serverThread())
        mThread!!.start()
        tvMessages!!.setText("Not connected")
        tvPort.text = "Port: $SERVER_PORT"
        btnSend.setOnClickListener {
            message = etMessage!!.getText().toString()
            if (!message!!.isEmpty()) {
                Thread(SendData(message!!)).start()
            }
        }
    }

    internal inner class serverThread : Runnable {
        override fun run() {
            try {
                val serverSocket = ServerSocket(SERVER_PORT)
                try {
                    socket = serverSocket.accept()
                    runOnUiThread { tvMessages!!.text = "Connected\n" }
                    // 取得網路輸入串流
                    reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
                    //取得網路輸出串流
                    out = PrintWriter(
                        BufferedWriter(
                            OutputStreamWriter(socket!!.getOutputStream())
                        ),
                        true
                    )
                    var charsRead = 0
                    val buffer = CharArray(1024) //choose your buffer size if you need other than 1024

                    while (true) {
                        charsRead = reader!!.read(buffer)
                        tmp = String(buffer).substring(0, charsRead)
                        if (tmp != null) {
                            runOnUiThread {
                                tvMessages!!.append(
                                    "收 ${socket!!.inetAddress.hostAddress}: $tmp\n"
                                )
                            }
                        }
                    }

//                        while (reader!!.readLine().also { tmp = it } != null) {
//                            runOnUiThread {
//                                tvMessages!!.append(
//                                    "收 ${socket!!.getInetAddress().hostAddress}: $tmp".trimIndent()
//                                )
//                            }
//                        }

                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    internal inner class SendData(private val message: String) : Runnable {
        override fun run() {
            out!!.println(message)
            runOnUiThread {
                tvMessages!!.append(
                    "發 ${socket!!.localAddress.hostAddress}: $message\n"
                )
            }
        }
    }

    @get:Throws(UnknownHostException::class)
    private val localIpAddress: String
        private get() {
            val wifiManager = (applicationContext.getSystemService(WIFI_SERVICE) as WifiManager)
            val wifiInfo = wifiManager.connectionInfo
            val ipInt = wifiInfo.ipAddress
            return InetAddress.getByAddress(
                ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array()
            ).hostAddress
        }

    override fun onDestroy() {
        super.onDestroy()
        if (socket != null) {
            try {
                socket!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            socket = null
        }
        mThread!!.interrupt()
    }
}