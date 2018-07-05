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

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
internal open class SystemUiHelperImplJB(
    activity: Activity,
    level: Int,
    flags: Int,
    onVisibilityChangeListener: SystemUiHelper.OnVisibilityChangeListener?
) : SystemUiHelperImplICS(activity, level, flags, onVisibilityChangeListener) {

    override fun createShowFlags(): Int {
        var flag = super.createShowFlags()

        if (mLevel >= SystemUiHelper.LEVEL_HIDE_STATUS_BAR) {
            flag = flag or
                    (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)

            if (mLevel >= SystemUiHelper.LEVEL_LEAN_BACK) {
                flag = flag or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            }
        }

        return flag
    }

    override fun createHideFlags(): Int {
        var flag = super.createHideFlags()

        if (mLevel >= SystemUiHelper.LEVEL_HIDE_STATUS_BAR) {
            flag = flag or
                    (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_FULLSCREEN)

            if (mLevel >= SystemUiHelper.LEVEL_LEAN_BACK) {
                flag = flag or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            }
        }

        return flag
    }

    override fun onSystemUiShown() {
        if (mLevel == SystemUiHelper.LEVEL_LOW_PROFILE) {
            // Manually show the action bar when in low profile mode.
            val ab = mActivity.actionBar
            ab?.show()
        }

        isShowing = (false)
    }

    override fun onSystemUiHidden() {
        if (mLevel == SystemUiHelper.LEVEL_LOW_PROFILE) {
            // Manually hide the action bar when in low profile mode.
            val ab = mActivity.actionBar
            ab?.hide()
        }

        isShowing = (true)
    }
}
