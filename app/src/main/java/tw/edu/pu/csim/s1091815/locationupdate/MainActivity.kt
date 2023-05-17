package tw.edu.pu.csim.s1091815.locationupdate

import android.Manifest
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import tw.edu.pu.csim.s1091815.locationupdate.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    private lateinit var locationCatch: LocationCatch
    private var isBound = false

    var util: Util = Util()
    var myLocationService: LocationService = LocationService()
    lateinit var serviceIntent: Intent

    lateinit var startButton: Button
    lateinit var stopButton: Button
    lateinit var newButton: ImageButton
    lateinit var button_show_data: ImageButton
    lateinit var GPSButton: ImageButton
    lateinit var loading: ImageView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)
        newButton = findViewById(R.id.newButton)
        button_show_data = findViewById(R.id.button_show_data)
        GPSButton = findViewById(R.id.GPSButton)
        loading = findViewById(R.id.loading)

        startButton.visibility = View.INVISIBLE;
        stopButton.visibility = View.INVISIBLE;
        GPSButton.visibility = View.INVISIBLE;
        newButton.visibility = View.INVISIBLE;
        button_show_data.visibility = View.INVISIBLE;

        val handler = Handler()
        handler.postDelayed({
            // 顯示按鈕
            startButton.visibility = View.VISIBLE
            stopButton.visibility = View.VISIBLE
            GPSButton.visibility = View.VISIBLE
            newButton.visibility = View.VISIBLE
            button_show_data.visibility = View.VISIBLE

            // 隱藏圖片
            loading.visibility = View.GONE
            showSafeDialog()
        }, 3000) //延遲3秒後顯示

        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.add)
        newButton.setImageBitmap(bitmap)
        val bitmap1 = BitmapFactory.decodeResource(resources, R.drawable.favorite)
        button_show_data.setImageBitmap(bitmap1)
        val bitmap2 = BitmapFactory.decodeResource(resources, R.drawable.gps)
        GPSButton.setImageBitmap(bitmap2)

        startButton.setOnClickListener {

            //如果同意位置存取權限，Android10以上需再判度背景位置資訊存取權
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    //Android10以上，需允許背景位置資訊存取權
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                        starServiceFunc()
                    }
                    //尚未同意背景位置權限
                    else{
                        AlertDialog.Builder(this).apply {
                            setTitle("背景位置資訊存取權")
                            setMessage("本App需要同意背景位置權限才能持續存取位置")
                            setPositiveButton("直接啟動服務",
                                DialogInterface.OnClickListener { dialog, id ->
                                    starServiceFunc()
                                })
                            setNegativeButton("同意背景位置權限",
                                DialogInterface.OnClickListener { dialog, id ->
                                    requestBackgroundLocationPermission()
                                })
                        }.create().show()
                    }
                }
                //同意位置存取權限，且小於Android10版本，直接啟動服務
                else{
                    starServiceFunc()
                }
                //尚未同意位置權限
            }else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    AlertDialog.Builder(this)
                        .setTitle("位置存取權限")
                        .setMessage("需要同意位置存取權限")
                        .setPositiveButton(
                            "OK"
                        ) { _, _ ->
                            requestFineLocationPermission()
                        }
                        .create().show()
                } else {
                    requestFineLocationPermission()
                }
            }

        }
        stopButton.setOnClickListener {
            stopServiceFunc()
        }
        binding.newButton.setOnClickListener {
            showLocationDialog()
        }
        button_show_data.setOnClickListener {
            val intent = Intent(this, Show_Save_Location::class.java)
            startActivity(intent)
        }
        var isHidden = true
        GPSButton.setOnClickListener{
            if(isHidden) {
                button_show_data.visibility = View.VISIBLE
                newButton.visibility = View.VISIBLE
            }else{
                button_show_data.visibility = View.INVISIBLE
                newButton.visibility = View.INVISIBLE
            }
            isHidden = !isHidden
        }

    }
    private fun showSafeDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("\u26A0 用路安全提醒")
        builder.setMessage("請在運動時保持專注，注意路況，\n運動過程中可不必頻繁使用本APP。\n確定後可繼續使用。")

        builder.setPositiveButton("確定") { dialog, which ->
            // Continue with the program
            dialog.dismiss()  // 關閉對話框
        }
        builder.setCancelable(false)
        builder.show()
    }

    override fun onStart() {
        super.onStart()
        Intent(this, LocationCatch::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as LocationCatch.LocationBinder
            locationCatch = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }
    fun showLocationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("請輸入要保存位置的名稱")

        val input = EditText(this)
        builder.setView(input)

        builder.setPositiveButton("確定") { dialog, which ->
            val userId = input.text.toString()
            showLocation(userId)
        }

        builder.setNegativeButton("取消") { dialog, which ->
            dialog.cancel()
        }

        builder.show()
    }
    fun showLocation(userId: String) {
        locationCatch.getLocation { location ->
            val latitude = location.latitude
            val longitude = location.longitude
            val locationData = HashMap<String,String>()
            locationData["經度"] = latitude.toString()
            locationData["緯度"] = longitude.toString()

            val db = FirebaseFirestore.getInstance()
            val locationRef = db.collection("locations")

            locationRef.document(userId).set(locationData)
                .addOnSuccessListener {
                    Toast.makeText(
                        applicationContext,
                        "成功保存位置",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        applicationContext,
                        "保存位置失敗",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun starServiceFunc(){
        myLocationService = LocationService()
        serviceIntent = Intent(
            this,
            myLocationService::class.java
        )
        //if (!Util.isMyServiceRunning(myLocationService::class.java, this)) {

        if (!util.isMyServiceRunning){
            util.isMyServiceRunning = true
            startForegroundService(serviceIntent)

            Toast.makeText(this, "服務成功啟動", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "服務目前運作中", Toast.LENGTH_SHORT).show()
        }
    }
    private fun stopServiceFunc(){
        myLocationService = LocationService()
        serviceIntent = Intent(
            this,
            myLocationService::class.java
        )
        //if (Util.isMyServiceRunning(myLocationService.javaClass, this)) {
        if (util.isMyServiceRunning){
            util.isMyServiceRunning = false
            stopService(serviceIntent)
            Toast.makeText(this, "服務成功停止", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "服務已停止", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun requestBackgroundLocationPermission() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), MY_BACKGROUND_LOCATION_REQUEST)
    }

    private fun requestFineLocationPermission() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), MY_FINE_LOCATION_REQUEST)
    }
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_FINE_LOCATION_REQUEST -> {

                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                        requestBackgroundLocationPermission()
                    }

                } else {
                    Toast.makeText(this, "拒絕位置存取權限", Toast.LENGTH_LONG).show()
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", this.packageName, null)
                            ),
                        )
                    }
                }
                return
            }
            MY_BACKGROUND_LOCATION_REQUEST -> {

                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "同意背景位置資訊存取權", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "拒絕背景位置資訊存取權", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }

    companion object {
        private const val MY_FINE_LOCATION_REQUEST = 99
        private const val MY_BACKGROUND_LOCATION_REQUEST = 100
    }
}