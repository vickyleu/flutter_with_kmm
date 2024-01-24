// 当前文件是各种基本类型的转换工具类

/// 传进来的是任何值,全部转换成boolean类型
bool toBool(dynamic value) {
  if (value is bool) {
    return value;
  }
  if (value is String) {
    return value.toLowerCase() == "true";
  }
  if (value is int) {
    return value != 0;
  }
  if (value is double) {
    return value != 0;
  }
  return false;
}

/// 传进来的是任何值,全部转换成int类型
int toInt(dynamic value) {
  if (value is int) {
    return value;
  }
  if (value is bool) {
    return value?1:0;
  }
  if (value is String) {
    return int.tryParse(value)??0;
  }
  if (value is double) {
    return value.toInt();
  }
  return 0;
}

/// 传进来的是任何值,全部转换成double类型
double toDouble(dynamic value) {
  if (value is double) {
    return value;
  }
  if (value is bool) {
    return value?1:0;
  }
  if (value is String) {
    return double.tryParse(value)??0;
  }
  if (value is int) {
    return value.toDouble();
  }
  return 0;
}

/// 传进来的是任何值,全部转换成String类型
String toString(dynamic value) {
  if (value is String) {
    return value;
  }
  if (value is bool) {
    return value.toString();
  }
  if (value is int) {
    return value.toString();
  }
  if (value is double) {
    return value.toString();
  }
  return "";
}

