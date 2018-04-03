package com.ogoons.hagoexpandablelayoutdemo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import com.ogoons.hagoexpandablelayout.HagoExpandableLayout

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val el = findViewById<HagoExpandableLayout>(R.id.expandable_layout)
        val btnToggle = findViewById<Button>(R.id.btn_toggle)
        btnToggle.setOnClickListener {
            el.toggle()
        }

    }
}
