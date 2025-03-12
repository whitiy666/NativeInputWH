package com.whitiy.native_edit_text

import android.content.Context
import android.text.InputType
import android.view.View
import android.widget.EditText
import androidx.core.content.ContextCompat
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import io.flutter.plugin.common.BinaryMessenger
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.DrawableCompat
import android.view.inputmethod.InputMethodManager
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.util.Log  // 添加 Log 类的导入
import android.view.ViewGroup  // 添加 ViewGroup 类的导入


//import android.graphics.drawable.ShapeDrawable
//import android.graphics.drawable.shapes.RectShape

class NativeEditTextPlugin: FlutterPlugin {
    private lateinit var binding: FlutterPlugin.FlutterPluginBinding

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        this.binding = binding
        binding.platformViewRegistry.registerViewFactory("com.whitiy.native_input_widget/native_input", NativeInputViewFactory(binding.binaryMessenger))
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {}
}

class NativeInputViewFactory(private val messenger: BinaryMessenger) : PlatformViewFactory(StandardMessageCodec.INSTANCE) {
    override fun create(context: Context, id: Int, args: Any?): PlatformView {
        val creationParams = args as Map<String?, Any?>?
        return NativeInputView(context, id, creationParams, messenger)

    }
}

class NativeInputView(context: Context, id: Int, creationParams: Map<String?, Any?>?, messenger: BinaryMessenger) : PlatformView, MethodChannel.MethodCallHandler {
    private val editText: EditText = EditText(context)
    private val methodChannel: MethodChannel = MethodChannel(messenger, "com.whitiy.native_input_widget/native_input_$id")
    private val mainHandler = Handler(Looper.getMainLooper())
    private var lastNotifiedText = "" // 添加此变量声明

    // 存储 TextWatcher 引用以便稍后移除
    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            val currentText = s.toString()
            if (currentText != lastNotifiedText) {
                lastNotifiedText = currentText
                methodChannel.invokeMethod("onChange", currentText)
            }
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }


    init {
        mainHandler.post {
            editText.setTextColor(ContextCompat.getColor(context, android.R.color.white))
            editText.setHintTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
            editText.background = null // 移除下划线

            // 使用存储的 textWatcher 对象
            editText.addTextChangedListener(textWatcher)

            // 设置输入光标颜色为不透明的白色并变窄
            val cursorDrawable = ShapeDrawable(RectShape())
            cursorDrawable.intrinsicWidth = 2 // 设置光标宽度

//            cursorDrawable.paint.color = ContextCompat.getColor(context, android.R.color.white)
//            // Alternative approach to set cursor color
//            try {
//                val f = TextView::class.java.getDeclaredField("mCursorDrawableRes")
//                f.isAccessible = true
//               // f.set(editText, cursorDrawable)
//                f.set(editText, R.drawable.cursor_drawable) // 使用新创建的资源ID
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
            // 代替尝试使用反射设置光标颜色，可以尝试以下方法：
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                // Android 10+ 支持直接设置光标颜色
                editText.textCursorDrawable = cursorDrawable
            } else {
                try {
                    // 对于较早的Android版本使用反射
                    val f = TextView::class.java.getDeclaredField("mCursorDrawableRes")
                    f.isAccessible = true
                    f.set(editText, R.drawable.cursor_drawable)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            editText.imeOptions = EditorInfo.IME_ACTION_DONE // 在输入法上显示确定的效果
            editText.inputType = InputType.TYPE_CLASS_TEXT // 设置所有输入类型

            creationParams?.let { params ->
                params["isObscure"]?.let { isObscure ->
                    if (isObscure as Boolean) {
                        editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    }
                }
                params["placeholderText"]?.let { placeholder ->
                    editText.hint = placeholder as String
                }
            }

            methodChannel.setMethodCallHandler(this)

            editText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                   // methodChannel.invokeMethod("onChange", s.toString())
                    val currentText = s.toString()
                    if (currentText != lastNotifiedText) {
                        lastNotifiedText = currentText
                        methodChannel.invokeMethod("onChange", currentText)
                    }
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            editText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    methodChannel.invokeMethod("onSubmit", editText.text.toString())
                    // 关闭键盘
                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(editText.windowToken, 0)
                    true
                } else {
                    false
                }
            }
        }
    }

    override fun getView(): View = editText

    override fun dispose() {
        try {
            // 使用存储的 textWatcher 引用
            editText.removeTextChangedListener(textWatcher)

            // 确保在视图销毁时清理资源
            mainHandler.removeCallbacksAndMessages(null)
            methodChannel.setMethodCallHandler(null)

            // 确保移除所有焦点和引用
            editText.clearFocus()

            // 可能需要考虑从父视图中移除
            val parent = editText.parent as? ViewGroup
            parent?.removeView(editText)
        } catch (e: Exception) {
            Log.e("NativeInputView", "Error disposing view", e)
        }
    }

//    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
//        // 如果需要从Flutter端调用原生方法,可以在这里处理
//        result.notImplemented()
//    }
override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
    when (call.method) {
        "setText" -> {
            val text = call.argument<String>("text") ?: ""
            mainHandler.post {
                editText.setText(text)
                result.success(null)
            }
        }
        "requestFocus" -> {
            mainHandler.post {
                editText.requestFocus()
                val imm = editText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
                result.success(null)
            }
        }
        else -> result.notImplemented()
    }
}
}