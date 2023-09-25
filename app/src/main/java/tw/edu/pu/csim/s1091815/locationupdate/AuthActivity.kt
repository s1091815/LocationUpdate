package tw.edu.pu.csim.s1091815.locationupdate

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class AuthActivity : AppCompatActivity() {

    lateinit var phone: EditText
    lateinit var otp: EditText
    lateinit var btngenOTP: Button
    lateinit var btnlogin: Button
    lateinit var btnverOTP: Button
    lateinit var mAuth: FirebaseAuth
    lateinit var verificationId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        supportActionBar?.title = ""

        val actionBar = supportActionBar

        actionBar?.displayOptions = androidx.appcompat.app.ActionBar.DISPLAY_SHOW_CUSTOM
        actionBar?.setCustomView(R.layout.custom_actionbar)

        val imageView = actionBar?.customView?.findViewById<ImageView>(R.id.customImageView)
        imageView?.setImageResource(R.drawable.logo)

        FirebaseApp.initializeApp(this)

        phone = findViewById(R.id.phone)
        otp = findViewById(R.id.otp)
        btngenOTP = findViewById(R.id.btngenOTP)
        btnlogin = findViewById(R.id.btnlogin)
        btnverOTP = findViewById(R.id.btnverOTP)
        mAuth = FirebaseAuth.getInstance()

        btngenOTP.setOnClickListener { view ->
            val number = phone.text.toString()
            if (TextUtils.isEmpty(number)) {
                Toast.makeText(this, "請輸入正確的手機號碼", Toast.LENGTH_SHORT).show()
            } else {
                sendVerificationCode(number)
            }
        }

        btnlogin.setOnClickListener { view ->
            val number = phone.text.toString()

            if (TextUtils.isEmpty(number)) {
                Toast.makeText(this, "請輸入正確的手機號碼", Toast.LENGTH_SHORT).show()
            } else {
                checkUserExistence(number)
            }
        }

        btnverOTP.setOnClickListener { view ->
            VerifyCode()
        }
    }

    private fun sendVerificationCode(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber("+886$phoneNumber") // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
    private fun checkUserExistence(phoneNumber: String) {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user != null) {
            Toast.makeText(this@AuthActivity, "用戶已登入", Toast.LENGTH_SHORT).show()
            navigateToMainActivity()
        } else {
            Toast.makeText(this@AuthActivity, "無此用戶紀錄，請註冊！(已傳送簡訊OTP驗證碼)", Toast.LENGTH_SHORT).show()
            sendVerificationCode(phoneNumber)
        }
    }


    private fun VerifyCode() {
        val code = otp.text.toString()
        if (TextUtils.isEmpty(code)) {
            Toast.makeText(this, "請輸入正確的 OTP", Toast.LENGTH_SHORT).show()
        } else {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            signInWithPhoneAuthCredential(credential)
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // 驗證成功，用戶已成功登入
                    val user = task.result?.user
                    // 在這裡可以導向 MainActivity 或其他需要的操作
                    Toast.makeText(this@AuthActivity, "驗證成功", Toast.LENGTH_SHORT).show()
                    navigateToMainActivity()
                } else {
                    // 驗證失敗
                    Toast.makeText(this@AuthActivity, "驗證失敗", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // 這將在驗證自動完成時被調用
            // 您可以選擇自動登入使用者或進行其他操作
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // 驗證失敗時的處理
            Toast.makeText(this@AuthActivity, "驗證失敗: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // 驗證碼已發送到用戶手機，保存 verificationId 以供後續使用
            this@AuthActivity.verificationId = verificationId
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
