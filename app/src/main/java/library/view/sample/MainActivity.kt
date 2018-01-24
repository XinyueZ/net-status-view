package library.view.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import library.view.sample.utils.SystemUiHelper

class MainActivity : AppCompatActivity() {
    private lateinit var uiHelper: SystemUiHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        uiHelper = SystemUiHelper(this, SystemUiHelper.LEVEL_IMMERSIVE, 0)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private fun hideSystemUi(length: Long = 0) {
        if (length <= 0) uiHelper.hide() else uiHelper.delayHide(length)
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
    }
}
