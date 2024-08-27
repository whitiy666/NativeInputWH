import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'native_edit_text_method_channel.dart';

abstract class NativeEditTextPlatform extends PlatformInterface {
  /// Constructs a NativeEditTextPlatform.
  NativeEditTextPlatform() : super(token: _token);

  static final Object _token = Object();

  static NativeEditTextPlatform _instance = MethodChannelNativeEditText();

  /// The default instance of [NativeEditTextPlatform] to use.
  ///
  /// Defaults to [MethodChannelNativeEditText].
  static NativeEditTextPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [NativeEditTextPlatform] when
  /// they register themselves.
  static set instance(NativeEditTextPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
