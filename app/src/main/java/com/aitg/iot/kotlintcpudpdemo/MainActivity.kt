package com.aitg.iot.kotlintcpudpdemo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    var btn_tp_server: Button? = null
    var btn_tp_client: Button? = null
    var btn_udp: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_tp_server = findViewById(R.id.btn_tp_server)
        btn_tp_client = findViewById(R.id.btn_tp_client)
        btn_udp = findViewById(R.id.btn_udp)
    }

    fun button(v: View) {
        when (v.id) {
            R.id.btn_tp_server -> {
                val i = Intent(this@MainActivity, TCPServerActivity::class.java)
                startActivity(i)
            }
            R.id.btn_tp_client -> {
                val i = Intent(this@MainActivity, TCPClientActivity::class.java)
                startActivity(i)
            }
            R.id.btn_udp -> {
                val i = Intent(this@MainActivity, UDPActivity::class.java)
                startActivity(i)
            }
        }
    }
}