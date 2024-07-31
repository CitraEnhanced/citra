// Copyright 2023 Citra Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package io.github.mandarine3ds.mandarine.utils

import android.annotation.SuppressLint
import android.content.Context

object InsetsHelper {
    const val GESTURE_NAVIGATION = 0

    @SuppressLint("DiscouragedApi")
    fun getSystemGestureType(context: Context): Int {
        val resources = context.resources
        val resourceId = resources.getIdentifier(
            "config_navBarInteractionMode",
            "integer",
            "android"
        )
        return if (resourceId != 0) resources.getInteger(resourceId) else 0
    }
}
