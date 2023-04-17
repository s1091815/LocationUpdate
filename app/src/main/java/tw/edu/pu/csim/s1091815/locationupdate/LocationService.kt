package tw.edu.pu.csim.s1091815.locationupdate

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*

class LocationService : Service() {

    var msg:String = "定位中"

    lateinit var myFusedLocationClient: FusedLocationProviderClient

    val myLocationRequest: LocationRequest =
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).apply {
            //setMinUpdateDistanceMeters(minimalDistance)
            setWaitForAccurateLocation(true)
        }.build()
//優先順序 其他選項：
//Priority.PRIORITY_BALANCED_POWER_ACCURACY 使用此設定可要求將位置精確度設定為城市街區，準確度約為 100 公尺。系統會將其視為概略準確度，因此耗電量可能會較低
//Priority.PRIORITY_LOW_POWER 使用此設定可要求城市層級精確度，準確度約為 10 公里
//Priority.PRIORITY_PASSIVE (表示NO_POWER) 如果您需要不對耗電量造成影響，但希望在可用時接收位置更新通知，請使用此設定。啟用此設定，您的應用程式不會觸發任何位置更新通知，但會接收其他應用程式觸發的位置資訊

//intervalMillis 更新間隔(毫秒)
    var myLocationCallback: LocationCallback = object : LocationCallback(){
    override fun onLocationResult(locationResult: LocationResult) {
        val locationList = locationResult.locations
        if(locationList.isNotEmpty()){
            val location = locationList.last()
            Toast.makeText(this@LocationService, "緯度：" + location.latitude + '\n' +
                    "經度：" + location.longitude , Toast.LENGTH_LONG).show()
            msg = "(" + location.latitude + ", " + location.longitude + ")"
        }
    }
}
    override fun onCreate() {
        super.onCreate()

        myFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(applicationContext, "需要同意位置權限才能存取經緯度", Toast.LENGTH_LONG).show()
            return
        }else{
            myFusedLocationClient?.requestLocationUpdates(
                myLocationRequest, myLocationCallback, Looper.getMainLooper())
        }
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Thread {
            while (true) {
                // 取得系統通知管理服務
                var notificationManager
                        = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                var notificationChannel: NotificationChannel

                // 兼容 API 26，Android 8.0
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    //建立通知頻道
                    notificationChannel = NotificationChannel("1", "位置通知", NotificationManager.IMPORTANCE_DEFAULT)

                    // 註冊通知頻道
                    notificationManager.createNotificationChannel(notificationChannel)
                }
                // 建構通知訊息內容
                val builder = NotificationCompat.Builder(this, "1")
                    .setContentTitle("定位服務")
                    .setContentText(msg)
                    .setSmallIcon(tw.edu.pu.csim.s1091815.locationupdate.R.drawable.img)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                //發出通知
                startForeground(1, builder.build())

                Thread.sleep(5000)
            }
        }.start()
        return super.onStartCommand(intent, flags, startId)
    }
    override fun onDestroy() {
        super.onDestroy()
        myFusedLocationClient.removeLocationUpdates(myLocationCallback)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}
