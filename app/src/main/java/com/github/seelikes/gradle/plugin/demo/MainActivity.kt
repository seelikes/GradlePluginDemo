package com.github.seelikes.gradle.plugin.demo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.github.seelikes.android.log.SaLog
import com.github.seelikes.groovy.plugin.ao.annotation.Inject

@Inject("DEBUG")
const val DEBUG : Boolean = false

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.i("MainActivity", "onCreate.UL1234LP.DI1211, DEBUG: $DEBUG, MKL.DN: ${MKL.DN}")
    }
}
