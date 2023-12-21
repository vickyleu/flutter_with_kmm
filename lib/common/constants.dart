import 'package:flutter/material.dart';

// Colors that we use in our app
const kPrimaryColor = Color(0xFF0C9869);
const kTextColor = Color(0xFF3C4046);
const kBackgroundColor = Color(0xFFF9F8FD);

const double kDefaultPadding = 10.0;


const String InternetGranted = "InternetGranted";

enum SDKNetworkGrantedType { Accessible, Restricted, Unknown ;

  static SDKNetworkGrantedType fromString(String? value) {
    if (value == null) {
      return SDKNetworkGrantedType.Unknown;
    }
    switch (value) {
      case 'Accessible':
        return SDKNetworkGrantedType.Accessible;
      case 'Restricted':
        return SDKNetworkGrantedType.Restricted;
      default:
        return SDKNetworkGrantedType.Unknown;
    }
  }
}
