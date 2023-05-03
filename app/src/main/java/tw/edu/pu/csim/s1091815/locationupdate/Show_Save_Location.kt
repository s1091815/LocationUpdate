package tw.edu.pu.csim.s1091815.locationupdate

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore

class Show_Save_Location : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_save_location)

        // 綁定 ListView
        val listView = findViewById<ListView>(R.id.list_view)

        val db = FirebaseFirestore.getInstance()
        val locationRef = db.collection("locations")

        val locationDataList = ArrayList<String>()

// 在按鈕的 onClick 事件內新增以下程式碼：
        locationRef.get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val userId = document.id
                    val latitude = document.get("緯度").toString()
                    val longitude = document.get("經度").toString()
                    locationDataList.add("地點:$userId\n經度:$longitude\n緯度:$latitude")
                }

                // 將 locationDataList 填充到 ListView 上
                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, locationDataList)
                listView.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "讀取資料失敗", Toast.LENGTH_SHORT).show()
            }
    }
}