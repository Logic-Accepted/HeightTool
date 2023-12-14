package top.labox233.heighttool

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class HistoryAdapter(private var historyList: List<HistoryItem>, private val context: Context) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileNameTextView: TextView = itemView.findViewById(R.id.textViewFileName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val currentItem = historyList[position]
        //holder.fileNameTextView.text = currentItem.fileName

        // 截取文件名中的日期字符串
        val dateString = currentItem.fileName.substring(9, 24)

        try {
            // 解析文件名中的时间戳
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val date = dateFormat.parse(dateString)

            // 格式化时间戳为指定格式
            val formattedDate = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()).format(date)

            // 设置文本
            holder.fileNameTextView.text = formattedDate
        } catch (e: Exception) {
            e.printStackTrace()
            holder.fileNameTextView.text = "Invalid Date"
        }

        // 设置点击事件
        holder.itemView.setOnClickListener {
            // 启动 PreviewActivity，并传递文件名
            val intent = Intent(context, PreviewActivity::class.java)
            intent.putExtra("fileName", currentItem.fileName)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        val empty = historyList.isEmpty()

        // 控制空列表提示的可见性
        val textViewEmptyList: TextView = (context as HistoryActivity).findViewById(R.id.textViewEmptyList)
        textViewEmptyList.visibility = if (empty) View.VISIBLE else View.GONE

        return historyList.size
    }
}
