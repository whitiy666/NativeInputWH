import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class NativeInputWidget extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return AndroidView(
      viewType: 'native_input_widget_view',
      onPlatformViewCreated: (int id) {
        final methodChannel = MethodChannel('native_input_widget_$id');
        methodChannel.setMethodCallHandler((MethodCall call) async {
          if (call.method == 'onChange') {
            print('Text changed: ${call.arguments}');
          } else if (call.method == 'onSubmit') {
            print('Text submitted: ${call.arguments}');
          }
        });
      },
      creationParams: {
        'placeholderText': 'Enter text',
        'isObscure': false,
      },
      creationParamsCodec: const StandardMessageCodec(),
    );
  }
}
