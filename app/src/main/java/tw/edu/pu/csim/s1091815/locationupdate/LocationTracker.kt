package tw.edu.pu.csim.s1091815.locationupdate

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LocationTracker {
    private var lastLatitude: Double = 0.0
    private var lastLongitude: Double = 0.0

    // 追蹤經緯度位置的程式碼
    fun trackLocation() {
        // 在此處更新lastLatitude和lastLongitude的值
    }

    // 取得最後追蹤到的經緯度位置的函式
    fun getLastLocation(): Pair<Double, Double> {
        return Pair(lastLatitude, lastLongitude)
    }
}

// 假設您的按鈕所在的class為ButtonActivity
