package com.github.scottbot95.stationeers.ic.util

inline val Number?.isTruthy get() = this?.toFloat() != 0f
inline val Number?.isFalsy get() = this == null || toFloat() == 0f
