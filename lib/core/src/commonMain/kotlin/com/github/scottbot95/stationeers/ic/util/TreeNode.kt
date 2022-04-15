package com.github.scottbot95.stationeers.ic.util

open class TreeNode(val children: List<TreeNode>?, val label: String)

fun TreeNode.forEachDepthFirst(block: (TreeNode) -> Unit, childrenFirst: Boolean = false) {
    if (!childrenFirst) block(this)
    children?.forEach {
        it.forEachDepthFirst(block, childrenFirst)
    }
    if (childrenFirst) block(this)
}

fun TreeNode.toTreeString(): String {
    val sb = StringBuilder()
    print(sb, "", "")
    return "$sb"
}

private fun TreeNode.print(sb: StringBuilder, prefix: String, childrenPrefix: String) {
    sb.append(prefix)
    sb.append(label)
    sb.append('\n')
    children?.dropLast(1)?.forEach {
        it.print(sb, "$childrenPrefix├── ", "$childrenPrefix│   ")
    }
    children?.lastOrNull()?.print(sb, "$childrenPrefix└── ", "$childrenPrefix    ")
}
