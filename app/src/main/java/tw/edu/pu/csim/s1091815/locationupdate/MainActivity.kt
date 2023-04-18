package tw.edu.pu.csim.s1091815.locationupdate

import android.Manifest
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {


    private lateinit var locationCatch: LocationCatch
    private var isBound = false


    var util: Util = Util()
    var myLocationService: LocationService = LocationService()
    lateinit var serviceIntent: Intent

    lateinit var startButton: Button
    lateinit var stopButton: Button
    lateinit var newButton: Button

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)
        newButton = findViewById(R.id.newButton)

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
        newButton.setOnClickListener {
            showLocation(newButton)
        }

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

    fun showLocation(view: View) {
        locationCatch.getLocation { location ->
            val latitude = location.latitude
            val longitude = location.longitude
            Toast.makeText(
                applicationContext,
                "經度: $latitude, 緯度: $longitude",
                Toast.LENGTH_LONG
            ).show()
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
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,), MY_FINE_LOCATION_REQUEST)
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
                        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", this.packageName, null),),)
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