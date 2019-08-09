package com.ogoons.expandablelayoutdemo

import android.os.Bundle
import android.support.v4.view.animation.FastOutLinearInInterpolator
import android.support.v7.app.AppCompatActivity
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Button
import com.ogoons.expandablelayout.ExpandableLayout

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val el1 = findViewById<ExpandableLayout>(R.id.expandable_layout_1)
        val btnToggle = findViewById<Button>(R.id.btn_toggle_1)
        btnToggle.setOnClickListener {
            el1.toggle()
        }

        val el2 = findViewById<ExpandableLayout>(R.id.expandable_layout_2)
        val btnToggle2 = findViewById<Button>(R.id.btn_toggle_2)
        btnToggle2.setOnClickListener {
            if (el2.isExpanded()) {
                el2.interpolator = DecelerateInterpolator()
                el2.collapse()
            } else {
                el2.interpolator = OvershootInterpolator()
                el2.expand()
            }
        }

        val el3 = findViewById<ExpandableLayout>(R.id.expandable_layout_3)
        el3.interpolator = BounceInterpolator()
        val btnToggle3 = findViewById<Button>(R.id.btn_toggle_3)
        btnToggle3.setOnClickListener {
            el3.toggle()
        }

        val el4 = findViewById<ExpandableLayout>(R.id.expandable_layout_4)
        el4.interpolator = FastOutLinearInInterpolator()
        val btnToggle4 = findViewById<Button>(R.id.btn_toggle_4)
        btnToggle4.setOnClickListener {
            el4.toggle()
        }
    }
}
