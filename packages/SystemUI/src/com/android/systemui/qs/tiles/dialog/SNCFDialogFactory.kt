/*
 * Copyright (C) 2023 The Android Open Source Project
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

package com.android.systemui.qs.tiles.dialog

import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.View

import com.android.systemui.animation.DialogLaunchAnimator
import com.android.systemui.dagger.SysUISingleton
import com.android.systemui.dagger.qualifiers.Main
import com.android.systemui.plugins.ActivityStarter
import javax.inject.Inject

private const val TAG = "SNCFDialogFactory"
private val DEBUG = Log.isLoggable(TAG, Log.DEBUG)

/**
 * Factory to create [SNCFDialog] objects.
 */
@SysUISingleton
class SNCFDialogFactory @Inject constructor(
    @Main private val handler: Handler,
    private val context: Context,
    private val dialogLaunchAnimator: DialogLaunchAnimator,
) {
    private var sncfDialog: SNCFDialog? = null

    /** Creates a [SNCFDialog]. The dialog will be animated from [view] if it is not null. */
    fun create(
        view: View?
    ) {
        if (sncfDialog != null) {
            logD {
                "SNCFDialog is showing, do not create it twice."
            }
            return
        }
        sncfDialog = SNCFDialog(
            context,
            handler,
            this
        ).also {
            if (view != null) {
                dialogLaunchAnimator.showFromView(it, view, animateBackgroundBoundsChange = true)
            } else {
                it.show()
            }
        }
    }

    fun destroyDialog() {
        logD {
            "destroyDialog"
        }
        sncfDialog = null
    }

    private inline fun logD(msg: () -> String) {
        if (DEBUG) {
            Log.d(TAG, msg())
        }
    }
}
