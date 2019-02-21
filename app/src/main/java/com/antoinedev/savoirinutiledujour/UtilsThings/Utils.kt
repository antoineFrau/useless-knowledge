package com.antoinedev.savoirinutiledujour.UtilsThings

import org.apache.commons.io.IOUtils
import java.io.InputStream
import java.io.StringWriter
import java.nio.charset.Charset

object Utils {
    // PUT IN GRADLE THIS DEPENDENCY
    // implementation "commons-io:commons-io:+"

    fun getTextFromStream(inputStream: InputStream): String? {
        try {
            val writer = StringWriter()
            IOUtils.copy(inputStream, writer, Charset.forName("UTF-8"))
            return writer.toString()
        } catch (e: Exception) {
            return null
        }
    }
}
