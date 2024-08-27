package com.whitiy.native_edit_text



import android.content.Context
import android.graphics.Color
import android.text.InputType
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView.OnEditorActionListener
import androidx.core.widget.addTextChangedListener
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView


import io.flutter.embedding.engine.plugins.FlutterPlugin

import io.flutter.plugin.platform.PlatformViewFactory
import io.flutter.plugin.platform.PlatformViewRegistry


import io.flutter.plugin.common.BinaryMessenger



class NativeInputWidgetFactory(private val messenger: BinaryMessenger) : PlatformViewFactory(StandardMessageCodec.INSTANCE) {
    override fun create(context: Context, id: Int, args: Any?): PlatformView {
        val creationParams = args as? Map<String, Any?>
        return NativeInputWidget(context, id, creationParams, MethodChannel(messenger, "native_input_widget"))
    }
}

class NativeInputWidgetPlugin : FlutterPlugin {

    private lateinit var methodChannel: MethodChannel

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        methodChannel = MethodChannel(flutterPluginBinding.binaryMessenger, "native_input_widget")

        flutterPluginBinding
            .platformViewRegistry
            .registerViewFactory("native_input_widget_view", NativeInputWidgetFactory(flutterPluginBinding.binaryMessenger))
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        methodChannel.setMethodCallHandler(null)
    }
}


class NativeInputWidget(context: Context, id: Int, creationParams: Map<String, Any?>?, channel: MethodChannel) : PlatformView {
    private val editText: EditText

    init {
        editText = EditText(context).apply {
            // 设置输入文字颜色
            setTextColor(Color.WHITE)
            // 设置光标颜色
            setHighlightColor(Color.WHITE)
            // 设置 placeholderText
            hint = creationParams?.get("placeholderText") as? String ?: ""
            setHintTextColor(Color.GRAY)
            // 设置密码输入类型
            if (creationParams?.get("isObscure") as? Boolean == true) {
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }

            // onChange 事件监听
            addTextChangedListener {
                channel.invokeMethod("onChange", it.toString())
            }

            // onSubmit 事件监听
            setOnEditorActionListener(OnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    channel.invokeMethod("onSubmit", text.toString())
                    true
                } else {
                    false
                }
            })
        }

        // 设置 Layout 参数
        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )

        val frameLayout = FrameLayout(context)
        frameLayout.addView(editText, layoutParams)
    }

    override fun getView() = editText

    override fun dispose() {}
}
