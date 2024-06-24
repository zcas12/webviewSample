package com.example.andoridtestv2
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btn: Button = findViewById(R.id.enterBtn)
        val intent = Intent(this, WebActivity::class.java)

        btn.setOnClickListener {
            checkCameraPermission {
                intent.putExtra("url", "https://testpay.kcp.co.kr/support/hdw/mgkim/cjoshop_qpay_20240530/sample/index.html")
                startActivity(intent)
            }
        }
    }

    private fun checkCameraPermission(onPermissionGranted: () -> Unit) {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                onPermissionGranted()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                Toast.makeText(this, "Camera permission is needed to access this feature.", Toast.LENGTH_SHORT).show()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val intent = Intent(this, WebActivity::class.java)
            intent.putExtra("url", "https://testpay.kcp.co.kr/support/hdw/mgkim/cjoshop_qpay_20240530/sample/index.html")
            startActivity(intent)
        } else {
            Toast.makeText(this, "Camera permission is required to proceed.", Toast.LENGTH_SHORT).show()
        }
    }
}