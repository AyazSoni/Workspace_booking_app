package com.example.workspace_booking_app.sharedstore

import android.content.Context
import java.io.File

object Workspace {
    private var name: String? = null
    private var bannerPath: String? = null

    fun setupDefaultData(context: Context) {
        name = "Default Workspace"
        val defaultFile = File(context.filesDir, "banner/banner.png")
        bannerPath = defaultFile.absolutePath
    }

    fun postWorkspaceData(workspaceName: String) {
        name = workspaceName
    }

    fun getWorkspaceName(): String? = name
    fun getWorkspaceBannerPath(): String? = bannerPath
}
