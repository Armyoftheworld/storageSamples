package com.example.storagesamples.contentproviderpaging

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.storagesamples.R

/**
 * @author Army
 * @version V_1.0.0
 * @date 2019-12-29
 * @description
 */
class PagingActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, ImageClientFragment.newInstance())
            .commitAllowingStateLoss()

    }
}