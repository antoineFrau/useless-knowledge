package com.antoinedev.savoirinutiledujour.UtilsThings

import com.antoinedev.savoirinutiledujour.KnowledgeItem

fun ArrayList<*>.isHashcodeEquals(other: ArrayList<KnowledgeItem>): Boolean {
    return this.toString().hashCode() == other.toString().hashCode()
}