package com.antoinedev.savoirinutiledujour

import android.content.Context
import android.util.Log
import com.antoinedev.savoirinutiledujour.UtilsThings.Utils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.UnsupportedEncodingException

class DiskCache(context: Context, cacheFolder: String) {

    protected var rootDir: File

    init {
        rootDir = context.getDir(cacheFolder, Context.MODE_PRIVATE)
    }


    @Synchronized
    fun saveData(data: ByteArray, filename: String): Boolean {
        try {
            val itemFile = getFile(filename, true)

            val fos = FileOutputStream(itemFile!!)
            fos.write(data)
            fos.close()
            Log.i(TAG, "File written to disk (" + itemFile.absolutePath + ")")

            return true
        } catch (e: Exception) {
            Log.e(TAG, "Impossible to save  $filename", e)
            return false
        }

    }

    @Synchronized
    fun saveText(text: String, filename: String): Boolean {
        try {
            return saveData(text.toByteArray(charset("UTF-8")), filename)
        } catch (e: UnsupportedEncodingException) {
            Log.e(TAG, "Impossible to save  $filename", e)
            return true
        }

    }

    @Synchronized
    fun getFile(filename: String, isForCreation: Boolean): File? {
        return getFile(filename, isForCreation, true)
    }

    @Synchronized
    fun getFile(filename: String, isForCreation: Boolean, isNameHashed: Boolean): File {
        val f = File(rootDir, filename)

        if (isForCreation && !f.parentFile.exists()) {
            f.parentFile.mkdirs()
        }

        return f
    }

    @Synchronized
    fun getData(filename: String, isNameHashed: Boolean): String? {
        val f = getFile(filename, false, isNameHashed)
        var content: String? = null

        if (f.exists()) {
            try {
                val inputStream = FileInputStream(f)
                content = Utils.getTextFromStream(inputStream)
            } catch (e: Exception) {
                Log.e(TAG, "Error while opening to open data file in cache :$filename", e)
            }
        } else {
            Log.e(TAG, "Binary file does not exist in cache :$filename")
        }

        return content
    }

    @Synchronized
    fun getText(filename: String): String? {
        val f = getFile(filename, false)
        var content: String? = null

        if (f != null && f.exists()) {
            try {
                val inputStream = FileInputStream(f)
                content = Utils.getTextFromStream(inputStream)
            } catch (e: Exception) {
                Log.e(TAG, "Error while opening to open text file in cache :$filename", e)
            }

        } else {
            Log.e(TAG, "Text file does not exist in cache :$filename")
        }

        return content
    }

    companion object {
        private val TAG = DiskCache::class.java.simpleName
    }
}
