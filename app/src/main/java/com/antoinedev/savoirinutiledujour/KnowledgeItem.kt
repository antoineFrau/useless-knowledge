package com.antoinedev.savoirinutiledujour

import java.util.*

class KnowledgeItem(var title: String, var description: String, var date: String, var id: Int) {
    override fun toString(): String {
        return "KnowledgeItem(title='$title', description='$description', date='$date', id=$id)"
    }
}