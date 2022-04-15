package com.github.scottbot95.stationeers.ic.util

private infix fun List<*>?.deepEquals(other: List<*>?): Boolean {
    if (this == null || other == null) return this == other
    return size == other.size && asSequence().mapIndexed { i, it -> it == other[i] }.all { it }
}

abstract class TreeNode<T : TreeNode<T>>(val children: List<T>, val label: String) {
    // TODO this ended up making lots of boilerplate in Expression. Can we avoid the need for this?
    abstract fun copy(children: List<T> = this.children, label: String = this.label): T
}

fun <T : TreeNode<T>> T.forEachDepthFirst(childrenFirst: Boolean = true, block: (T) -> Unit) {
    if (!childrenFirst) block(this)
    children.forEach {
        it.forEachDepthFirst(childrenFirst, block)
    }
    if (childrenFirst) block(this)
}

fun <T : TreeNode<T>> T.mapDepthFirst(block: (T) -> T): T {
    val newChildren = children.map {
        it.mapDepthFirst(block)
    }

    val nodeWithNewChildren = if (newChildren deepEquals children) this else copy(newChildren)
    val newNode = block(nodeWithNewChildren)

    return newNode
}

fun TreeNode<*>.toTreeString(): String {
    val sb = StringBuilder()
    print(sb, "", "")
    return "$sb"
}

private fun TreeNode<*>.print(sb: StringBuilder, prefix: String, childrenPrefix: String) {
    sb.append(prefix)
    sb.append(label)
    sb.append('\n')
    children.dropLast(1).forEach {
        it.print(sb, "$childrenPrefix├── ", "$childrenPrefix│   ")
    }
    children.lastOrNull()?.print(sb, "$childrenPrefix└── ", "$childrenPrefix    ")
}
