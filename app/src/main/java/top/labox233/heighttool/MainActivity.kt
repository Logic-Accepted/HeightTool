package top.labox233.heighttool

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.icu.text.SimpleDateFormat
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import java.io.BufferedReader
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var editTextKey: EditText
    private lateinit var editTextCx: EditText
    private lateinit var buttonSendRequest: Button
    private lateinit var textViewResult: TextView
    private lateinit var textViewResultRaw: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("Point", "启动了喵")
        super.onCreate(savedInstanceState)
        // 主 activity
        setContentView(R.layout.activity_main)

        //设置 toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "光遇身高工具"

        editTextKey = findViewById(R.id.editTextKey)
        editTextCx = findViewById(R.id.editTextCx)
        buttonSendRequest = findViewById(R.id.buttonSendRequest)
        textViewResult = findViewById(R.id.textViewResult)
        progressBar = findViewById(R.id.progressBar)

        // 获取SharedPreferences实例
        sharedPreferences = getPreferences(Context.MODE_PRIVATE)

        // 尝试从SharedPreferences中获取保存的key
        val savedKey = sharedPreferences.getString("key", "")
        if (!savedKey.isNullOrEmpty()) {
            editTextKey.setText(savedKey)
        } else {
            // 如果SharedPreferences中没有保存的key，弹出对话框进行输入
            showKeyInputDialog()
        }

        buttonSendRequest.setOnClickListener {
            val key = editTextKey.text.toString()
            val cx = editTextCx.text.toString()

            if (isValidCx(cx)) {
                // 构建API请求URL
                val apiUrl = "example.com?key=${URLEncoder.encode(key, "UTF-8")}&cx=${URLEncoder.encode(cx, "UTF-8")}"

                // 发送GET请求
                SendGetRequestTask().execute(apiUrl)
            } else {
                // 输入不合法，显示提示信息给用户
                Toast.makeText(this, "原ID/长ID/UUID不合法，请检查。", Toast.LENGTH_SHORT).show()
            }
        }

    }

    // 检查一下uuid合不合法，不然后面保存的时候会崩
    private fun isValidCx(cx: String): Boolean {
        return try {
            UUID.fromString(cx)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
        // return true
    }

    // 这是输入apikey的对话框
    private fun showKeyInputDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("输入APIKey")

        val input = EditText(this)

        // 尝试从SharedPreferences中获取保存的这个apikey
        val savedKey = sharedPreferences.getString("key", "")
        input.setText(savedKey)

        builder.setView(input)

        builder.setPositiveButton("保存") { _, _ ->
            // 保存用户输入的key到SharedPreferences
            val enteredKey = input.text.toString()
            sharedPreferences.edit().putString("key", enteredKey).apply()
            editTextKey.setText(enteredKey)
        }

        builder.setCancelable(false) // 不允许点击对话框外部取消哈
        builder.show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_history -> {
                startActivity(Intent(this, HistoryActivity::class.java))
                //startActivity(Intent(this, PreviewActivity::class.java))
                true
            }
            R.id.menu_about -> {
                showAboutDialog()
                true
            }
            R.id.menu_reset_apikey -> {
                //重新设置apikey啊
                showKeyInputDialog()
                true
            }
            R.id.menu_dropdown -> {
                item.actionView?.let { showDropdownMenu(it) }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showAboutDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("关于")
        builder.setMessage("作者:\nhttps://github.com/Logic-Accepted\n项目地址:\nhttps://github.com/Logic-Accepted/HeightTool/")
        builder.setPositiveButton("确定") { _, _ -> }

        val dialog = builder.create()
        dialog.show()
    }

    private fun showDropdownMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        val inflater = popupMenu.menuInflater
        inflater.inflate(R.menu.toolbar_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_history -> {
                    true
                }
                R.id.menu_about -> {
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    @SuppressLint("StaticFieldLeak")
    private inner class SendGetRequestTask : AsyncTask<String, Void, String>() {
        // 等待api回应时的加载环
        @Deprecated("Deprecated in Java")
        override fun onPreExecute() {
            progressBar.visibility = View.VISIBLE
        }

        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg params: String?): String {
            val urlString = params[0]

            val url = URL(urlString)
            val urlConnection = url.openConnection() as HttpURLConnection

            try {
                val inputStream = urlConnection.inputStream
                val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                val stringBuilder = StringBuilder()

                var line: String? = bufferedReader.readLine()
                while (line != null) {
                    stringBuilder.append(line).append("\n")
                    line = bufferedReader.readLine()
                }

                return stringBuilder.toString()
            } finally {
                urlConnection.disconnect()
            }
        }

        private fun saveToHistory(data: String?) {
            thread {
                try {
                    Log.d("SaveToHistory", "运行到saveToHistory了喵")
                    // 获取内部存储的根目录
                    val rootDir = getInternalStorageDirectory()

                    // 创建 history 文件夹
                    val historyDir = File(rootDir, "history")
                    if (!historyDir.exists()) {
                        historyDir.mkdirs()
                    }

                    // 构建文件名，目前是“history_yyyyMMdd_HHmmss_xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx.txt”
                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss_", Locale.getDefault()).format(
                        Date()
                    )
                    val cx = editTextCx.text.toString()
                    val fileName = "history_$timeStamp$cx.txt"

                    // 这里创建 txt 文件并写入数据
                    val file = File(historyDir, fileName)
                    val writer = BufferedWriter(FileWriter(file))
                    writer.write(data ?: "")
                    writer.close()
                    Log.d("SaveToHistory", "保存成功了喵: ${file.absolutePath}")
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.d("SaveToHistory", "保存失败了喵")
                }
            }
        }

        // 这里获取内部存储的根目录
        private fun getInternalStorageDirectory(): File {
            return applicationContext.filesDir
            //保存到内部存储得了，到时候如果有必要就加一个判断要不要保存到sd卡
        }

        @Deprecated("Deprecated in Java")
        @SuppressLint("SetTextI18n")
        override fun onPostExecute(result: String?) {
            progressBar.visibility = View.GONE

            textViewResultRaw.text = result
            val gson = Gson()
            val jsonObject: JsonObject = gson.fromJson(result, JsonObject::class.java)

            val code = jsonObject.get("code").asInt

            if (code == 200) {
                val dataObject = jsonObject.getAsJsonObject("data")

                val scale = String.format("%.3f", dataObject.get("scale").asString.toDouble())
                val height = String.format("%.3f", dataObject.get("height").asString.toDouble())
                val currentHeight = String.format("%.3f", dataObject.get("currentHeight").asDouble)
                val maxHeight = String.format("%.3f", dataObject.get("maxHeight").asDouble)
                val minHeight = String.format("%.3f", dataObject.get("minHeight").asDouble)

                textViewResult.text = "S值: $scale\nH值: $height\n目前身高: $currentHeight\n最大值: $maxHeight\n最小值: $minHeight"
                textViewResultRaw.text = result
                saveToHistory(result)
                Log.d("Point", "准备保存了喵")
            } else {
                textViewResult.text = "Error: $code"
                //saveToHistory(result)
                //Log.d("Point", "准备保存了，虽然不是200喵")
                // 不是200不配保存喵
            }
        }
    }
}
