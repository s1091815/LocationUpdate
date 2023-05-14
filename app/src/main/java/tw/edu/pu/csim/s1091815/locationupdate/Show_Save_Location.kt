package tw.edu.pu.csim.s1091815.locationupdate

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore

class Show_Save_Location : AppCompatActivity() {
    private lateinit var listView: ListView
    private val locationDataList = ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_save_location)

        // 綁定 ListView
        listView = findViewById(R.id.list_view)

        val db = FirebaseFirestore.getInstance()
        val locationRef = db.collection("locations")

        val adapter = ArrayAdapter(
            this,
            R.layout.list_item_with_delete_button,
            R.id.text_view_location,
            locationDataList
        )
        listView.adapter = adapter

        //val locationDataList = ArrayList<String>()
        locationDataList.clear()
        // 在按鈕的 onClick 事件內新增以下程式碼：
        locationRef.get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val userId = document.id
                    val latitude = document.get("緯度").toString()
                    val longitude = document.get("經度").toString()
                    locationDataList.add("地點:$userId\n\n經度:$longitude\n緯度:$latitude")
                }
                adapter.notifyDataSetChanged()
                /* 將 locationDataList 填充到 ListView 上
                val adapter =
                    ArrayAdapter(this, android.R.layout.simple_list_item_1, locationDataList)
                listView.adapter = adapter*/

                // 設置刪除按鈕的點擊監聽器
                listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                    deleteLocation(position)
                }
                // 設置編輯按鈕的點擊監聽器
                listView.onItemLongClickListener = AdapterView.OnItemLongClickListener { parent, view, position, id ->
                    editUserId(position)
                    true
                }


            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "讀取資料失敗", Toast.LENGTH_SHORT).show()
            }

    }
    private fun deleteLocation(position: Int) {
        /*獲取要刪除的位置對應的使用者 ID，從 locationDataList 中找到指定位置的資料並使用 split("\n") 方法將其轉換為字符串數組，
        再從數組中取得第一個字符串，並使用 substring(3) 方法從第3個字符開始截取，以獲取使用者 ID。*/
        val userId = locationDataList[position].split("\n")[0].substring(3)

        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("確認刪除")
        alertDialogBuilder.setMessage("您確定要刪除此位置嗎？")
        alertDialogBuilder.setPositiveButton("確定") { _, _ ->
            val db = FirebaseFirestore.getInstance()
            val locationRef = db.collection("locations").document(userId)

            locationRef.delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "成功刪除位置", Toast.LENGTH_SHORT).show()

                    locationDataList.removeAt(position)
                    (listView.adapter as ArrayAdapter<*>).notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "刪除位置失敗", Toast.LENGTH_SHORT).show()
                }
        }
        alertDialogBuilder.setNegativeButton("取消") { _, _ ->
            // 取消刪除操作

        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
    private fun editUserId(position: Int) {
        val currentUserId = locationDataList[position].split("\n")[0].substring(3)

        val editText = EditText(this)
        editText.setText(currentUserId)

        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("編輯使用者ID")
        alertDialogBuilder.setView(editText)

        alertDialogBuilder.setPositiveButton("確定") { _, _ ->
            val newUserId = editText.text.toString()
            updateUserId(position, currentUserId, newUserId)
        }
        alertDialogBuilder.setNegativeButton("取消") { _, _ ->
            // 取消編輯操作
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun updateUserId(position: Int, currentUserId: String, newUserId: String) {
        val db = FirebaseFirestore.getInstance()
        val locationRef = db.collection("locations").document(currentUserId)

        locationRef.update("userId", newUserId)
            .addOnSuccessListener {
                Toast.makeText(this, "成功更新使用者ID", Toast.LENGTH_SHORT).show()

                // 更新 locationDataList 中的使用者ID
                val updatedLocationData = locationDataList[position].replaceFirst(currentUserId, newUserId)
                locationDataList[position] = updatedLocationData

                // 更新 ListView
                (listView.adapter as ArrayAdapter<*>).notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "更新使用者ID失敗", Toast.LENGTH_SHORT).show()
            }
    }


    fun onDeleteLocation(view: View) {
        val position = listView.getPositionForView(view)
        deleteLocation(position)
    }
    fun editUserId(view: View) {
        val position = listView.getPositionForView(view)
        editUserId(position)
    }

}