package com.example.storyapp.view.login

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.example.storyapp.loginwithanimation.R

class CustomButton: AppCompatButton {
    private var txtColor: Int = 0
    private var enabledColor: Int = Color.parseColor("#999FFF")
    private var disabledColor: Int = Color.parseColor("#808080")

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        setTextColor(txtColor)
        textSize = 15f
        gravity = Gravity.CENTER
        text = if(isEnabled) resources.getString(R.string.kirim) else resources.getString(R.string.error)
        setBackgroundColor(if (isEnabled) enabledColor else disabledColor)

        val radius = height.toFloat() / 2
        val shape = GradientDrawable()
        shape.cornerRadius = radius
        shape.setColor(if (isEnabled) enabledColor else disabledColor)
        background = shape
    }

    private fun init() {
        txtColor = ContextCompat.getColor(context, android.R.color.background_light)
    }
}