package com.github.seelikes.gradle.plugin.demo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.github.seelikes.groovy.plugin.ao.annotation.Inject

class MainActivity : AppCompatActivity() {
    @Inject("DEBUG")
    val DEBUG : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
