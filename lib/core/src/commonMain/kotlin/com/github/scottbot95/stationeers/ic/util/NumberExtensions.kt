package com.github.scottbot95.stationeers.ic.util

inline val Number.isTruthy get() = toFloat() != 0f
inline val Number.isFalsy get() = toFloat() == 0f
