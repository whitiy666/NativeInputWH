import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'native_edit_text_platform_interface.dart';

/// An implementation of [NativeEditTextPlatform] that uses method channels.
class MethodChannelNativeEditText extends NativeEditTextPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('native_edit_text');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
