import 'package:flutter/services.dart';
import 'package:flutter/material.dart';

typedef TextChangedCallback = void Function(String text);
typedef TextSubmittedCallback = void Function(String text);

class NativeInputWidget extends StatefulWidget {
  final TextChangedCallback? onChange;
  final TextSubmittedCallback? onSubmit;
  final bool isObscure;
  final String placeholderText;

  NativeInputWidget({
    Key? key,
    this.onChange,
    this.onSubmit,
    this.isObscure = false,
    this.placeholderText = '',
  }) : super(key: key);

  @override
  _NativeInputWidgetState createState() => _NativeInputWidgetState();
}

class _NativeInputWidgetState extends State<NativeInputWidget> {
  late TextEditingController _controller;

  @override
  void initState() {
    super.initState();
    _controller = TextEditingController();
    _controller.addListener(() {
      if (widget.onChange != null) {
        widget.onChange!(_controller.text);
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return TextField(
      controller: _controller,
      obscureText: widget.isObscure,
      decoration: InputDecoration(
        hintText: widget.placeholderText,
      ),
      onSubmitted: widget.onSubmit,
    );
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }
}
