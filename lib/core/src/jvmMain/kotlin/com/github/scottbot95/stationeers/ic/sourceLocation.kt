package com.github.scottbot95.stationeers.ic

actual fun sourceLocation(depth: Int): Location? {
    val e = Exception()
    if (e.stackTrace.size <= depth) return null
    val stackFrame = e.stackTrace[depth + 1]
    return Location(stackFrame.fileName, stackFrame.lineNumber)
}
