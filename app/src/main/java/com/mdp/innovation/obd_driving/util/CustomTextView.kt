package com.mdp.innovation.obd_driving.util

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.TextView


internal class CustomTextView : TextView {

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context)
    }

    private fun init(context: Context) {
        var font = ""
        try {
            when (Integer.parseInt(tag.toString())) {
                1 -> font = "fonts/TitilliumWeb-Regular.ttf"
                2 -> font = "fonts/TitilliumWeb-Bold.ttf"
                3 -> font = "fonts/TitilliumWeb-Italic.ttf"
                4 -> font = "fonts/TitilliumWeb-Light.ttf"
            }
        } catch (e: Exception) {
            font = "fonts/TitilliumWeb-Regular.ttf"
        }

        val tf = Typeface.createFromAsset(context.assets, font)
        typeface = tf
    }
}