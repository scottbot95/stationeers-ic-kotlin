package com.github.scottbot95.stationeers.ic.util

import com.github.scottbot95.stationeers.ic.dsl.LineReference

/**
 * Simplified version of a [MutableMap] to manage labels in a script
 */
interface LabelContainer {
    /**
     * Gets the [LineReference] for a given [label] or null if none found
     */
    operator fun get(label: String): LineReference?

    /**
     * Checks if [label] is already in use
     */
    operator fun contains(label: String): Boolean

    /**
     * Adds the given [reference] to the [LabelContainerImpl].
     * Ignores [reference]s without a [LineReference.label]
     */
    fun add(reference: LineReference)
}

/**
 * Basic implemenatin of [LabelContainer]
 */
class LabelContainerImpl : LabelContainer {
    private val labels = mutableMapOf<String, LineReference>()

    override operator fun get(label: String) = labels[label]

    override operator fun contains(label: String) = label in labels

    override fun add(reference: LineReference) {
        val label = reference.label ?: return
        if (label in this) {
            throw IllegalStateException("Cannot have more than one label with name `$label`.")
        }
        labels[label] = reference
    }
}

/**
 * Class that delegates implementation of [LabelContainer] to delegate
 */
class DelegatingLabelContainer(delegate: LabelContainer) : LabelContainer by delegate
