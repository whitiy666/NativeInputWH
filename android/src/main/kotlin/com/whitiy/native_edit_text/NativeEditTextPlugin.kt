package com.whitiy.native_input_widget

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


class NativeInputWidgetPlugin: FlutterPlugin {
    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        binding.platformViewRegistry.registerViewFactory("com.whitiy.native_input_widget/native_input", NativeInputViewFactory())
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {}
}

class NativeInputViewFactory : PlatformViewFactory(StandardMessageCodec.INSTANCE) {
    override fun create(context: Context, id: Int, args: Any?): PlatformView {
        val creationParams = args as Map<String?, Any?>?
        return NativeInputView(context, id, creationParams)
    }
}

class NativeInputView(context: Context, id: Int, creationParams: Map<String?, Any?>?) : PlatformView, MethodChannel.MethodCallHandler {
    private val editText: EditText
    private val methodChannel: MethodChannel

    init {
        editText = EditText(context)
        editText.setTextColor(ContextCompat.getColor(context, android.R.color.white))
        editText.setHintTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
        editText.textCursorDrawable?.setTint(ContextCompat.getColor(context, android.R.color.white))

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

        methodChannel = MethodChannel(context.applicationContext as FlutterPlugin.FlutterPluginBinding).binaryMessenger, "com.whitiy.native_input_widget/native_input_$id")
        methodChannel.setMethodCallHandler(this)

        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                methodChannel.invokeMethod("onChange", s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                methodChannel.invokeMethod("onSubmit", editText.text.toString())
                true
            } else {
                false
            }
        }
    }

    override fun getView(): View = editText

    override fun dispose() {}

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        // 如果需要从Flutter端调用原生方法,可以在这里处理
        result.notImplemented()
    }
}
