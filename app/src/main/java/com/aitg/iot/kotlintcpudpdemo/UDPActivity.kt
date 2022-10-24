package com.aitg.iot.kotlintcpudpdemo

import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.net.*
import java.util.*

class UDPActivity : AppCompatActivity() {
    private var tvMessages: TextView? = null
    private var etMessage: EditText? = null
    private val port = 1212 //傳送埠號
    private var message: String? = null
    private var listenStatus = true //接收執行緒的迴圈標識
    private var receiveSocket: DatagramSocket? = null
    private var sendSocket: DatagramSocket? = null
    var wifiManager: WifiManager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.udp)
        val editTextID = findViewById<EditText>(R.id.editTextID)
        tvMessages = findViewById(R.id.tvMessages)
        etMessage = findViewById(R.id.etMessage)
        val btnSend = findViewById<Button>(R.id.btnSend)
        editTextID.setText(getIPAddress(true))

        // ipTextView.setText(getIPAddress(false));
        println("!!!!   getIPAddress(false) = " + getIPAddress(true))
        btnSend.setOnClickListener {
            message = etMessage!!.getText().toString()
            if (!message!!.isEmpty()) {
                Thread(SendData(message!!)).start()
            }
        }
        wifiManager = this.getSystemService(WIFI_SERVICE) as WifiManager
        UdpReceiveThread().start()
    }

    /**
     * UDP 接收資料
     */
    inner class UdpReceiveThread : Thread() {
        override fun run() {
            try {
                receiveSocket = DatagramSocket(port)
                receiveSocket!!.broadcast = true
                val buf = ByteArray(1024)
                val packet = DatagramPacket(buf, buf.size)
                while (listenStatus) {
                    receiveSocket!!.receive(packet)
                    val data = String(packet.data)
                    Log.i(
                        TAG, "Packet received from: " + packet.address.hostAddress +
                                " ,  data: " + data
                    )
                    runOnUiThread {
                        tvMessages!!.append(
                            "${packet.address.hostAddress}:$data\n"
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * UDP 發送資料
     */
    internal inner class SendData(private val message: String) : Runnable {
        override fun run() {
            try {
                sendSocket = DatagramSocket()
                //sendSocket.setBroadcast(true);
                val buf = message.toByteArray()
                // InetAddress targetAddress = InetAddress.getByName("255.255.255.255");
                val targetAddress = InetAddress.getByName("255.255.255.255")
                val ipv6 = Inet6Address.getByName("fe80::d216:b4ff:fe16:b7d5")
                val sendPacket = DatagramPacket(
                    buf, buf.size,
                    targetAddress, port
                )
                sendSocket!!.send(sendPacket)
                Log.i(
                    TAG, "Broadcast packet sent to: " +
                            broadcastAddress.hostAddress
                )
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    @get:Throws(IOException::class)
    val broadcastAddress: InetAddress
        get() {
            assert(wifiManager != null)
            val broadcast = wifiManager!!.dhcpInfo.ipAddress and
                    wifiManager!!.dhcpInfo.netmask or wifiManager!!.dhcpInfo.netmask.inv()
            val quads = ByteArray(4)
            for (i in 0..3) quads[i] = (broadcast shr i * 8 and 0xFF).toByte()
            return InetAddress.getByAddress(quads)
        }

    //    private String getLocalIpAddress() throws UnknownHostException {
    //        assert wifiManager != null;
    //        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
    //        int ipInt = wifiInfo.getIpAddress();
    //        return InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
    //                .putInt(ipInt).array()).getHostAddress();
    //    }
    private val localIpAddress: String
        private get() {
            try {
                val en = NetworkInterface.getNetworkInterfaces()
                while (en.hasMoreElements()) {
                    val networkInterface = en.nextElement()
                    val enumIpAddr = networkInterface.inetAddresses
                    while (enumIpAddr.hasMoreElements()) {
                        val inetAddress = enumIpAddr.nextElement()
                        val hostAddress = inetAddress.hostAddress
                        println("!!!!!   hostAddress = " + inetAddress.hostAddress)
                        if (!inetAddress.isLoopbackAddress && !inetAddress.isLinkLocalAddress && inetAddress is Inet6Address) {
                            return hostAddress
                        } else if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                            return hostAddress
                        }
                    }
                }
            } catch (ex: SocketException) {
                ex.printStackTrace()
            }
            return ""
        }

    override fun onDestroy() {
        super.onDestroy()
        listenStatus = false
        if (sendSocket != null) {
            sendSocket!!.close()
            sendSocket = null
        }
        if (receiveSocket != null) {
            receiveSocket!!.close()
            receiveSocket = null
        }
    }

    companion object {
        private const val TAG = "UDPActivity"
        fun getIPAddress(useIPv4: Boolean): String {
            try {
                val interfaces: List<NetworkInterface> =
                    Collections.list(NetworkInterface.getNetworkInterfaces())
                for (intf in interfaces) {
                    val addrs: List<InetAddress> = Collections.list(intf.inetAddresses)
                    for (addr in addrs) {
                        if (!addr.isLoopbackAddress) {
                            val sAddr = addr.hostAddress
                            //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                            val isIPv4 = sAddr.indexOf(':') < 0
                            if (useIPv4) {
                                if (isIPv4) return sAddr
                            } else {
                                if (!isIPv4) {
                                    val delim = sAddr.indexOf('%') // drop ip6 zone suffix
                                    return if (delim < 0) sAddr.uppercase(Locale.getDefault()) else sAddr.substring(
                                        0,
                                        delim
                                    ).uppercase(
                                        Locale.getDefault()
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (ignored: Exception) {
            } // for now eat exceptions
            return ""
        }
    }
}
