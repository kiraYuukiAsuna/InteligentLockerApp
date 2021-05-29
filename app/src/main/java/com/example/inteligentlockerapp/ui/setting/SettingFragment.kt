package com.example.inteligentlockerapp.ui.setting

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.inteligentlockerapp.MainActivity
import com.example.inteligentlockerapp.databinding.FragmentSettingBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.*


class SettingFragment : Fragment() {

    private lateinit var settingViewModel: SettingViewModel
    private var _binding: FragmentSettingBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        settingViewModel =
            ViewModelProvider(this).get(SettingViewModel::class.java)

        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setConfigToView()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val mainActivity: MainActivity = activity as MainActivity

        var serversettingbutton: Button = binding.serversettingbutton
        serversettingbutton.setOnClickListener{
            writeConfig()
        }

        var buttonreconnect: Button = binding.buttonreconnect
        buttonreconnect.setOnClickListener{
            GlobalScope.launch {
                mainActivity.reconnect()
            }
        }

        var loginbutton: Button=binding.loginbutton
        loginbutton.setOnClickListener{
            GlobalScope.launch {
                mainActivity.userLogin()
            }
        }
    }

    var serverIPFileName:String="serverip.txt"
    var serverPortFileName:String="serverport.txt"
    var userNameFileName:String="username.txt"
    var userPasswordFileName:String="userpassword.txt"
    var userMailFileName:String="usermail.txt"

    var serverIP:String?=null
    var serverPort:String?=null
    var userName:String?=null
    var userPassword:String?=null
    var userMail:String?=null

    private fun writeConfig(){
        var dataPath: File? = context?.getExternalFilesDir("")
        try {
            writeTxtFile(binding.serveripeditor.text.toString(),dataPath.toString(),serverIPFileName,false)
            writeTxtFile(binding.serverporteditor.text.toString(),dataPath.toString(),serverPortFileName,false)

            writeTxtFile(binding.usernameeditor.text.toString(),dataPath.toString(),userNameFileName,false)
            writeTxtFile(binding.userpasswordeditor.text.toString(),dataPath.toString(),userPasswordFileName,false)

            writeTxtFile(binding.usermaileditor.text.toString(),dataPath.toString(),userMailFileName,false)

            val mainActivity: MainActivity = activity as MainActivity
            mainActivity.updateConfigValue()

            Toast.makeText(activity, "写入配置文件成功！", Toast.LENGTH_SHORT).show()
        }catch (e:Exception){
            Log.e("File Exception:",e.message.toString())
        }
    }

    private fun readConfig(){
        var dataPath: File? = context?.getExternalFilesDir("")
        try {
            serverIP= readTxtFile(dataPath.toString(),serverIPFileName)?.trim()
            serverPort=readTxtFile(dataPath.toString(),serverPortFileName)?.trim()

            userName=readTxtFile(dataPath.toString(),userNameFileName)?.trim()
            userPassword=readTxtFile(dataPath.toString(),userPasswordFileName)?.trim()

            userMail=readTxtFile(dataPath.toString(),userMailFileName)?.trim()

            Toast.makeText(activity, "读取配置文件成功！", Toast.LENGTH_SHORT).show()
        }catch (e:Exception){
            Log.e("File Exception:",e.message.toString())
        }
    }

    private fun setConfigToView(){
        readConfig()
        var dataPath: File? = context?.getExternalFilesDir("")
        if(serverIP!=""){
            binding.serveripeditor.setText(serverIP)
        }else{
            writeTxtFile(binding.serveripeditor.text.toString(),dataPath.toString(),serverIPFileName,false)
            readConfig()
        }
        if(serverPort!=""){
            binding.serverporteditor.setText(serverPort)
        }else{
            writeTxtFile(binding.serverporteditor.text.toString(),dataPath.toString(),serverPortFileName,false)
            readConfig()
        }
        if(userName!=""){
            binding.usernameeditor.setText(userName)
        }else{
            writeTxtFile(binding.usernameeditor.text.toString(),dataPath.toString(),userNameFileName,false)
            readConfig()
        }
        if(userPassword!=""){
            binding.userpasswordeditor.setText(userPassword)
        }else {
            writeTxtFile(binding.userpasswordeditor.text.toString(),dataPath.toString(),userPasswordFileName,false)
            readConfig()
        }
        if(userMail!="") {
            binding.usermaileditor.setText(userMail)
        }else{
            writeTxtFile(binding.usermaileditor.text.toString(),dataPath.toString(),userMailFileName,false)
            readConfig()
        }
    }

    /**
     * 创建文件
     * @param filePath 文件路径(不要以/结尾)
     * @param fileName 文件名称（包含后缀,如：ReadMe.txt）
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun createTxtFile(filePath: String, fileName: String): Boolean {
        var flag = false
        val filename = File("$filePath/$fileName")
        if (!filename.exists()) {
            filename.createNewFile()
            flag = true
        }
        return flag
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
    private fun writeTxtFile(content: String, filePath: String, fileName: String, append: Boolean): Boolean {
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
