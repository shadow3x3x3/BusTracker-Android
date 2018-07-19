package com.shadow3x3x3.bustracker

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.shadow3x3x3.bustracker.service.LocationService

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = Intent(this, LocationService::class.java)
        startService(intent)
    }


}
