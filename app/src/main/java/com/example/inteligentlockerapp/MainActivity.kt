package com.example.inteligentlockerapp

import android.Manifest
import android.app.AlertDialog
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

        readConfig()

        GlobalScope.launch {
            connectServer(mServerIP, mServerPort)
        }

        showNormalDialog()
    }

    private fun showNormalDialog() {
        val alterDiaglog = AlertDialog.Builder(this)
        alterDiaglog.setTitle("欢迎！")
        alterDiaglog.setMessage("即刻进入！")
//builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))

        alterDiaglog.setPositiveButton("确认") { dialog, which ->
            Toast.makeText(
                applicationContext,
                android.R.string.yes, Toast.LENGTH_SHORT
            ).show()
        }

        alterDiaglog.setNegativeButton("取消") { dialog, which ->
            Toast.makeText(
                applicationContext,
                android.R.string.no, Toast.LENGTH_SHORT
            ).show()
        }

        alterDiaglog.setNeutralButton("Maybe") { dialog, which ->
            Toast.makeText(
                applicationContext,
                "Maybe", Toast.LENGTH_SHORT
            ).show()
        }
        runOnUiThread { alterDiaglog.show() }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == 2) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Internet permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Internet permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private var mTag = R.string.app_name.toString()

    private var mIsRealConnected = false

    private var mClientSocket: Socket? = null
    private var mPrintWriter: PrintWriter? = null
    private var mBufferedReader: BufferedReader? = null

    private var mReceiveMsg: String? = null

    suspend fun connectServer(serverIP: String?, serverPort: String?) {
        if (serverIP != null && serverPort != null) {
            try {
                mClientSocket = Socket(serverIP, serverPort.toInt())

                Log.d(mTag, "successfully!")

                mClientSocket!!.soTimeout = 60000

                mPrintWriter = PrintWriter(
                    BufferedWriter(
                        OutputStreamWriter(
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
                    Toast.makeText(this, "连接服务器成功！", Toast.LENGTH_SHORT).show()
                    Looper.loop()
                }

            } catch (e: Exception) {
                Log.e(mTag, ("fun ConnectServer:" + e.message))
                Looper.prepare()
                Toast.makeText(this, "连接服务器失败！Error:" + e.message, Toast.LENGTH_SHORT).show()
                Looper.loop()
            }
        }
    }

    fun isRealConnected(): Boolean {
        return !mClientSocket!!.isClosed && mClientSocket!!.isConnected && mIsRealConnected
    }

    suspend fun userLogin() {
        if (isRealConnected()) {
            if (mUserName != null && mUserPassword != null) {
                //用户登录请求 用户名
                sendMessageToServer(mUserName!!)
                //密码
                sendMessageToServer(mUserPassword!!)
                //
                GlobalScope.launch {
                    recvMessageFromServer()
                }
            } else {
                Looper.prepare()
                Toast.makeText(this, "连接服务器失败！", Toast.LENGTH_SHORT).show()
                Looper.loop()
            }
        }
    }

    fun recvMessageFromServer() {
        while (isRealConnected()) {
            try {
                mReceiveMsg = mBufferedReader?.readLine() ?: ""
                if (mReceiveMsg != "") {
                    Log.d(mTag, ("fun recvMessageFromServer:" + "recv new message"))
                    var textview: TextView = findViewById(R.id.serverreturnmessage)
                    textview.text = mReceiveMsg
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    suspend fun sendMessageToServer(message: String) {
        if (isRealConnected()) {
            mPrintWriter?.println(message)
            //to delay a time for server to accept the message sended before
            delay(50)
        }
    }

    suspend fun reconnect() {
        connectServer(mServerIP, mServerPort)
    }

    suspend fun unlockRequest(securityCode: String) {
        sendMessageToServer(securityCode)
    }

    var serverIPFileName: String = "serverip.txt"
    var serverPortFileName: String = "serverport.txt"
    var userNameFileName: String = "username.txt"
    var userPasswordFileName: String = "userpassword.txt"
    var userMailFileName: String = "usermail.txt"

    var mServerIP: String? = null
    var mServerPort: String? = null
    var mUserName: String? = null
    var mUserPassword: String? = null
    var mUserMail: String? = null

    private fun readConfig() {
        var dataPath: File? = getExternalFilesDir("")
        try {
            mServerIP = readTxtFile(dataPath.toString(), serverIPFileName)?.trim()
            mServerPort = readTxtFile(dataPath.toString(), serverPortFileName)?.trim()

            mUserName = readTxtFile(dataPath.toString(), userNameFileName)?.trim()
            mUserPassword = readTxtFile(dataPath.toString(), userPasswordFileName)?.trim()

            mUserMail = readTxtFile(dataPath.toString(), userMailFileName)?.trim()

            Toast.makeText(this, "读取配置文件成功！", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("File Exception:", e.message.toString())
        }
    }

    fun updateConfigValue() {
        readConfig()
    }

    /**
     * 写文件
     *
     * @param content 文件内容
     * @param filePath 文件路径(不要以/结尾)
     * @param fileName 文件名称（包含后缀,如：ReadMe.txt）
     * 新内容
     * @throws IOException
     */
    private fun writeTxtFile(
        content: String,
        filePath: String,
        fileName: String,
        append: Boolean
    ): Boolean {
        var flag: Boolean = true
        val thisFile = File("$filePath/$fileName")
        try {
            if (!thisFile.parentFile.exists()) {
                thisFile.parentFile.mkdirs()
            }
            val fw = FileWriter("$filePath/$fileName", append)
            fw.write(content)
            fw.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return flag
    }

    /**
     * 读TXT文件内容
     * @param filePath 文件路径(不要以 / 结尾)
     * @param fileName 文件名称（包含后缀,如：ReadMe.txt）
     * @return
     */
    @Throws(Exception::class)
    private fun readTxtFile(filePath: String, fileName: String): String? {
        var result: String? = ""
        val fileName = File("$filePath/$fileName")
        var fileReader: FileReader? = null
        var bufferedReader: BufferedReader? = null
        try {
            fileReader = FileReader(fileName)
            bufferedReader = BufferedReader(fileReader)
            try {
                var read: String? = null
                while (run {
                        read = bufferedReader.readLine()
                        read
                    } != null) {
                    result = result + read + "\r\n"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            bufferedReader?.close()
            fileReader?.close()
        }
        return result
    }
}
