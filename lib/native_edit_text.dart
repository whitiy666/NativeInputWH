import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

class NativeInputWidget extends StatefulWidget {
  final Function(String)? onChange;
  final Function(String)? onSubmit;
  final bool isObscure;
  final String? placeholderText;

  const NativeInputWidget({
    Key? key,
    this.onChange,
    this.onSubmit,
    this.isObscure = false,
    this.placeholderText,
  }) : super(key: key);

  @override
  State<NativeInputWidget> createState() => _NativeInputWidgetState();
}

class _NativeInputWidgetState extends State<NativeInputWidget> {
  @override
  Widget build(BuildContext context) {
    const String viewType = 'com.whitiy.native_input_widget/native_input';
    final Map<String, dynamic> creationParams = <String, dynamic>{
      'isObscure': widget.isObscure,
      'placeholderText': widget.placeholderText,
    };

    return AndroidView(
      viewType: viewType,
      layoutDirection: TextDirection.ltr,
      creationParams: creationParams,
      creationParamsCodec: const StandardMessageCodec(),
      onPlatformViewCreated: _onPlatformViewCreated,
    );
  }

  void _onPlatformViewCreated(int id) {
    if (widget.onChange != null || widget.onSubmit != null) {
      final channel = MethodChannel('com.whitiy.native_input_widget/native_input_$id');
      channel.setMethodCallHandler((call) async {
        switch (call.method) {
          case 'onChange':
            if (widget.onChange != null) {
              widget.onChange!(call.arguments as String);
            }
            break;
          case 'onSubmit':
            if (widget.onSubmit != null) {
              widget.onSubmit!(call.arguments as String);
            }
            break;
        }
      });
    }
  }
}
