package com.example.inteligentlockerapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.inteligentlockerapp.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.*
import java.net.Socket

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.CAMERA),
                1
            )
        }
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.INTERNET
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.INTERNET),
                2
            )
        }

        GlobalScope.launch {
            connectServer(mServerIP, mServerPort)

        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_LONG).show()
            }
        } else if (requestCode == 2) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Internet permission granted", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Internet permission denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    private var mTag = R.string.app_name.toString()

    private var mIsRealConnected = false

    private var mClientSocket: Socket? = null
    private var mPrintWriter: PrintWriter? = null
    private var mBufferedReader: BufferedReader? = null

    private val mServerIP: String = "192.168.1.110"
    private val mServerPort = 25500

    private val mReceiveMsg: String? = null

    suspend fun connectServer(serverIP: String, serverPort: Int) {
        try {
            mClientSocket = Socket(serverIP, serverPort.toInt())

            Log.d(mTag, "successfully!")

            mClientSocket!!.soTimeout = 60000

            mPrintWriter = PrintWriter(
                BufferedWriter(
                    OutputStreamWriter( //步骤二
                        mClientSocket!!.getOutputStream(), "UTF-8"
                    )
                ), true
            )
            mBufferedReader =
                BufferedReader(InputStreamReader(mClientSocket!!.getInputStream(), "UTF-8"))

            mIsRealConnected = true;

            if (isRealConnected()) {
                //发送判断身份验证的码，APP端是0000002，硬件端是0000001
                sendMessageToServer("0000002")

                userLogin()
                Looper.prepare()
                Toast.makeText(this, "连接服务器成功！", Toast.LENGTH_LONG).show()
                Looper.loop()
            }

        } catch (e: Exception) {
            Log.e(mTag, ("fun ConnectServer:" + e.message))
            Looper.prepare()
            Toast.makeText(this, "连接服务器失败！Error:" + e.message, Toast.LENGTH_LONG).show()
            Looper.loop()
        }
    }

    suspend fun isRealConnected(): Boolean {
        return !mClientSocket!!.isClosed && mClientSocket!!.isConnected && mIsRealConnected
    }

    suspend fun userLogin() {
        if (isRealConnected()) {
            //用户登录请求 用户名
            sendMessageToServer("user")
            //密码
            sendMessageToServer("password")
            //
            GlobalScope.launch {
                recvMessageFromServer()
            }
        } else {
            Looper.prepare()
            Toast.makeText(this, "连接服务器失败！", Toast.LENGTH_LONG).show()
            Looper.loop()
        }
    }

    suspend fun recvMessageFromServer() {
        try {
            var receiveMsg: String
            while (isRealConnected()) {
                receiveMsg = mBufferedReader?.readLine() ?: ""
                if (receiveMsg != "") {
                    Log.d(mTag, ("fun recvMessageFromServer:" + "recv new message"))
                    var a: TextView = findViewById<TextView>(R.id.text_home)
                    a.text = receiveMsg;
                }
            }
        } catch (e: IOException) {
            Log.d(mTag, "receiveMsg: ")
            e.printStackTrace()
        }
    }

    suspend fun sendMessageToServer(message: String) {
        if (isRealConnected()) {
            mPrintWriter?.println(message)
            //to delay a time for server to accept the message sended before
            delay(50)
        }
    }

}
