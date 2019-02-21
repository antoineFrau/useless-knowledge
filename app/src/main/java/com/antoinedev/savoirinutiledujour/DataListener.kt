package com.antoinedev.savoirinutiledujour

interface DataListener {
    fun notifyRetrieved(knowledgeItems: ArrayList<KnowledgeItem>)
    fun notifyNotRetrieved()
}