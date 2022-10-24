package com.aitg.iot.kotlintcpudpdemo

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.*
import java.net.Socket


class TCPClientActivity : AppCompatActivity() {
    private var mThread: Thread? = null
    private var etIP: EditText? = null
    private var etPort: EditText? = null
    private var tvMessages: TextView? = null
    private var etMessage: EditText? = null
    private var SERVER_IP: String? = null
    private var SERVER_PORT = 0
    private var reader: BufferedReader? = null
    private var socket: Socket? = null
    private var tmp: String? = null
    private var out: PrintWriter? = null
    private var message: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tcp_client)
        etIP = findViewById(R.id.etIP)
        etPort = findViewById(R.id.etPort)
        tvMessages = findViewById(R.id.tvMessages)
        etMessage = findViewById(R.id.etMessage)
        val btnSend = findViewById<Button>(R.id.btnSend)
        val btnConnect = findViewById<Button>(R.id.btnConnect)

        btnConnect.setOnClickListener {
            tvMessages!!.setText("")
            SERVER_IP = etIP!!.getText().toString()
            SERVER_PORT = etPort!!.getText().toString().toInt()
            mThread = Thread(clientThread())
            mThread!!.start()
        }
        btnSend.setOnClickListener {
            message = etMessage!!.getText().toString()
            if (!message!!.isEmpty()) {
                Thread(SendData(message!!)).start()
            }
        }
    }

    internal inner class clientThread : Runnable {
        override fun run() {
            try {
                socket = Socket(SERVER_IP, SERVER_PORT)
                if (socket!!.isConnected) {
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
//                    while (reader!!.readLine().also { tmp = it } != null) {
//                        runOnUiThread {
//                            tvMessages!!.append(
//                                "收 ${socket!!.inetAddress.hostAddress}: $tmp\n"
//                            )
//                        }
//                    }
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
    }
}
