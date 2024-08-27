import 'package:flutter_test/flutter_test.dart';
import 'package:native_edit_text/native_edit_text.dart';
import 'package:native_edit_text/native_edit_text_platform_interface.dart';
import 'package:native_edit_text/native_edit_text_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockNativeEditTextPlatform
    with MockPlatformInterfaceMixin
    implements NativeEditTextPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final NativeEditTextPlatform initialPlatform = NativeEditTextPlatform.instance;

  test('$MethodChannelNativeEditText is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelNativeEditText>());
  });

  test('getPlatformVersion', () async {
    NativeEditText nativeEditTextPlugin = NativeEditText();
    MockNativeEditTextPlatform fakePlatform = MockNativeEditTextPlatform();
    NativeEditTextPlatform.instance = fakePlatform;

    expect(await nativeEditTextPlugin.getPlatformVersion(), '42');
  });
}
