package com.mdp.innovation.obd_driving_api.app.core

import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.support.annotation.Nullable
import com.mdp.innovation.obd_driving_api_v2.app.ConnectOBDv2

class ApplicationInitProvider: ContentProvider() {

    //abstract fun initialize() : (Application) -> Unit

    override fun insert(p0: Uri?, p1: ContentValues?): Uri {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun query(p0: Uri?, p1: Array<out String>?, p2: String?, p3: Array<out String>?, p4: String?): Cursor? {
        return null
    }

    override fun onCreate(): Boolean {
        val context = context
        ConnectOBDv2.initialize(context)
        ConnectOBD.initialize(context)

        return true
    }

    override fun update(p0: Uri?, p1: ContentValues?, p2: String?, p3: Array<out String>?): Int {
        return 0
    }

    override fun delete(p0: Uri?, p1: String?, p2: Array<out String>?): Int {
        return 0
    }

    override fun getType(p0: Uri?): String? {
        return null
    }


}