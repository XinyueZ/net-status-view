/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package library.view.sample.utils

import android.annotation.TargetApi
import android.app.Activity
import android.os.Build
import android.view.View
import android.view.WindowManager

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
internal open class SystemUiHelperImplHC(
    activity: Activity, level: Int, flags: Int,
    onVisibilityChangeListener: SystemUiHelper.OnVisibilityChangeListener?
) : SystemUiHelper.SystemUiHelperImpl(activity, level, flags, onVisibilityChangeListener),
    View.OnSystemUiVisibilityChangeListener {

    val mDecorView: View

    init {

        mDecorView = activity.window.decorView
        mDecorView.setOnSystemUiVisibilityChangeListener(this)
    }

    override fun show() {
        mDecorView.systemUiVisibility = createShowFlags()
    }

    override fun hide() {
        mDecorView.systemUiVisibility = createHideFlags()
    }

    override fun onSystemUiVisibilityChange(visibility: Int) {
        if (visibility and createTestFlags() != 0) {
            onSystemUiHidden()
        } else {
            onSystemUiShown()
        }
    }

    protected open fun onSystemUiShown() {
        val ab = mActivity.actionBar
        ab?.show()

        mActivity.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        isShowing = (true)
    }

    protected open fun onSystemUiHidden() {
        val ab = mActivity.actionBar
        ab?.hide()

        mActivity.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        isShowing = (false)
    }

    protected open fun createShowFlags(): Int {
        return View.STATUS_BAR_VISIBLE
    }

    protected open fun createHideFlags(): Int {
        return View.STATUS_BAR_HIDDEN
    }

    protected open fun createTestFlags(): Int {
        return View.STATUS_BAR_HIDDEN
    }
}
