package top.labox233.heighttool

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import java.io.File

class AnalyseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analyse)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        val textViewIfEmpty: TextView = findViewById(R.id.textSelectEmpty)//这个是选择框没选的时候那个textview
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            title = "分析"
            setDisplayHomeAsUpEnabled(true)
        }

        val uuidSpinner: Spinner = findViewById(R.id.uuidSpinner)
        val uuidList = mutableListOf<String>()
        uuidList.addAll(getUniqueUUIDsFromHistoryFolder())
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, uuidList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        uuidSpinner.adapter = adapter
        // uuidSpinner.prompt = ""
        uuidList.add(0, "请选择")
        // 选择监听器
        uuidSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            val displayText = "未选择ID"
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long) {
                val selectedUUID = uuidList[position]
                // 判断是否选择的是 "请选择"
                if (position == 0) {
                    Log.d("Point", "选到position=0了喵")
                    textViewIfEmpty.text = displayText
                    textViewIfEmpty.visibility = View.VISIBLE
                } else {
                    textViewIfEmpty.visibility = View.GONE
                    dataAnalyse(selectedUUID)
                }
            }

            private fun dataAnalyse(selectedUUID: String) {
                // TODO("Not yet implemented")
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                textViewIfEmpty.text = displayText
                textViewIfEmpty.visibility = View.VISIBLE
            }
        }

    }

    private fun getUniqueUUIDsFromHistoryFolder(): List<String> {
        val historyDir = getHistoryDirectory()
        val files = historyDir.listFiles()

        val uuidSet = mutableSetOf<String>()

        files?.forEach {
            val fileName = it.name
            val uuid = fileName.substringAfterLast('_').substringBefore('.')
            uuidSet.add(uuid)
        }

        return uuidSet.toList()
    }

    private fun getHistoryDirectory(): File {
        // 获取内部存储的根目录
        val rootDir = applicationContext.filesDir
        return File(rootDir, "history")
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
