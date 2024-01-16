package top.labox233.heighttool

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class HistoryActivity : AppCompatActivity() {

    private val previewFinishReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "top.labox233.heighttool.SINGLE-DELETED") {
                setRecyclerView()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // 注册广播接收器
        val filter = IntentFilter("top.labox233.heighttool.SINGLE-DELETED")
        registerReceiver(previewFinishReceiver, filter, RECEIVER_NOT_EXPORTED)

        supportActionBar?.apply {
            title = "历史记录"
            setDisplayHomeAsUpEnabled(true)
        }

        // 获取 RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewHistory)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 获取历史记录文件夹中的文件列表
        val historyDir = getHistoryDirectory()
        val files = historyDir.listFiles()

        // 将文件名添加到 HistoryItem 列表
        val historyList = mutableListOf<HistoryItem>()
        files?.forEach {
            historyList.add(HistoryItem(it.name))
        }

        // 创建并设置适配器
        val adapter = HistoryAdapter(historyList, this)
        recyclerView.adapter = adapter
    }

    override fun onDestroy() {
        // 在销毁时注销广播接收器，以防内存泄漏
        unregisterReceiver(previewFinishReceiver)
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.clear_history_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_clear_history -> {
                checkHistoryFolder("menu_clear_history")
                true
            }
            R.id.menu_export -> {
                checkHistoryFolder("menu_export")
                true
            }
            R.id.menu_analyse -> {
                checkHistoryFolder("menu_analyse")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun checkHistoryFolder(menuItem: String) {
        val historyDir = getHistoryDirectory()
        val content = historyDir.list() // 文件list
        if (content.isNullOrEmpty()) {
            Toast.makeText(this, "历史记录已然空空", Toast.LENGTH_SHORT).show()
        }
        else{
            return when (menuItem){
                "menu_clear_history" -> showClearHistoryDialog()
                "menu_export" -> Toast.makeText(this, "在做", Toast.LENGTH_SHORT).show()
                "menu_analyse" -> startActivity(Intent(this, AnalyseActivity::class.java))
                else -> Toast.makeText(this, "?", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showClearHistoryDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("确认删除")
        builder.setMessage("确定要删除所有历史记录吗？")

        // 这里删除
        builder.setPositiveButton("确定") { _: DialogInterface, _: Int ->
            clearHistoryFiles()
        }

        builder.setNegativeButton("取消", null)

        val dialog = builder.create()
        dialog.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun clearHistoryFiles() {
        val historyDir = getHistoryDirectory()
        val files = historyDir.listFiles()

        files?.forEach {
            it.delete()
        }
        val newAdapter = HistoryAdapter(emptyList(), this)

        // 设置新的适配器
        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewHistory)
        recyclerView.adapter = newAdapter

        val stringBuilder = StringBuilder()
        stringBuilder.setLength(0)
        // 刷新列表
        setRecyclerView()

        // 如果要返回到 MainActivity 一定得用back啊 用finish的话他窗口是往左弹得 怎么看怎么怪
        // finish()
        // onBackPressed()

    }

    private fun setRecyclerView() {
        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewHistory)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 获取历史记录文件夹中的文件列表
        val historyDir = getHistoryDirectory()
        val files = historyDir.listFiles()

        // 将文件名添加到 HistoryItem 列表
        val historyList = mutableListOf<HistoryItem>()
        files?.forEach {
            historyList.add(HistoryItem(it.name))
        }

        // 创建并设置适配器
        val adapter = HistoryAdapter(historyList, this)
        recyclerView.adapter = adapter
    }



    private fun getHistoryDirectory(): File {
        // 又是内部存储的根目录
        val rootDir = applicationContext.filesDir

        // 创建 history 文件夹
        val historyDir = File(rootDir, "history")
        if (!historyDir.exists()) {
            historyDir.mkdirs()
        }

        return historyDir
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
