package top.labox233.heighttool

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class PreviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        val textViewUUID: TextView = findViewById(R.id.textViewUUID)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            title = "预览"
            setDisplayHomeAsUpEnabled(true)
        }

        // 获取文件名
        val fileName = intent.getStringExtra("fileName")

        // 解析文件名，获取最后一个下划线之后的部分
        val uuid = fileName?.substringAfterLast('_')?.substringBefore('.')

        // 显示 UUID
        textViewUUID.text = "  ID:\n  $uuid"

        // 获取文件内容并显示
        val textViewContent: TextView = findViewById(R.id.textViewContent)
        if (fileName != null) {
            val fileContent = readFileContent(fileName)
            textViewContent.text = fileContent

            // 尝试解析文件内容为 JsonObject
            val gson = Gson()
            try {
                val jsonObject: JsonObject = gson.fromJson(fileContent, JsonObject::class.java)

                // 处理 JsonObject 中的数据，例如：
                val scale = String.format("%.3f", (jsonObject.getAsJsonObject("data").get("scale").asString).toDouble())
                val height = String.format("%.3f", (jsonObject.getAsJsonObject("data").get("height").asString).toDouble())
                val currentHeight = String.format("%.3f", (jsonObject.getAsJsonObject("data").get("currentHeight").asString).toDouble())
                val maxHeight = String.format("%.3f", (jsonObject.getAsJsonObject("data").get("maxHeight").asString).toDouble())
                val minHeight = String.format("%.3f", (jsonObject.getAsJsonObject("data").get("minHeight").asString).toDouble())

                // 显示处理过的数据
                val processedData = "  S值: $scale\n  H值: $height\n  目前身高: $currentHeight\n  最大值: $maxHeight\n  最小值: $minHeight"
                val textViewProcessedData: TextView = findViewById(R.id.textViewResult)
                textViewProcessedData.text = processedData
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.preview_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_delete -> {
                // 处理删除选项的点击事件
                showDeleteConfirmationDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("确认删除")
        builder.setMessage("确定要删除这个文件吗？")
        builder.setPositiveButton("确定") { _, _ ->
            // 用户点击确定，执行删除操作
            deleteFile()
        }
        builder.setNegativeButton("取消", null)

        val dialog = builder.create()
        dialog.show()
    }

    private fun deleteFile() {
        val fileName = intent.getStringExtra("fileName")
        val file = File(getHistoryDirectory(), fileName)
        if (file.exists()) {
            file.delete()
            sendBroadcast(Intent("top.labox233.heighttool.SINGLE-DELETED")) // 发个广播告诉上一页 让他刷新一下列表
            finish()// 删除后关闭当前 Activity
        }
        else finish()
    }

    private fun readFileContent(fileName: String): String {
        val file = File(getHistoryDirectory(), fileName)
        val stringBuilder = StringBuilder()

        try {
            val bufferedReader = BufferedReader(FileReader(file))
            var line: String? = bufferedReader.readLine()
            while (line != null) {
                stringBuilder.append(line).append("\n")
                line = bufferedReader.readLine()
            }
            bufferedReader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return stringBuilder.toString()
    }

    private fun getHistoryDirectory(): File {
        // 获取内部存储的根目录
        val rootDir = applicationContext.filesDir

        // 创建 history 文件夹
        val historyDir = File(rootDir, "history")
        if (!historyDir.exists()) {
            historyDir.mkdirs()
        }

        return historyDir
    }
}
