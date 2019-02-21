package com.antoinedev.savoirinutiledujour

import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Handler
import com.antoinedev.savoirinutiledujour.UtilsThings.ConnectivityReceiver
import com.antoinedev.savoirinutiledujour.UtilsThings.SingletonHolder
import org.json.JSONObject
import android.os.HandlerThread
import android.util.Log
import com.antoinedev.savoirinutiledujour.UtilsThings.Utils
import java.io.IOException
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL
import kotlin.collections.ArrayList
import android.os.Looper
import android.os.Message
import org.json.JSONException

@SuppressLint("Registered")
class MainController private constructor(context: Context) {

    private val TAG: String = "MyController"
    private var dataCache: DiskCache = DiskCache(context, "items")

    private var handler: Handler
    private lateinit var responseHandler: Handler

    private lateinit var weakRefDataListener: WeakReference<DataListener>

    companion object : SingletonHolder<MainController, Context>(::MainController)

    init {
        val myThread = HandlerThread("get_data")
        myThread.start()
        this.handler = Handler(myThread.looper)
    }

    private fun getItemsFromAPI(): ArrayList<KnowledgeItem> {
        val url = "https://serginho.goodbarber.com/front/get_items/939101/26902416/?local=1" // 5 items
//        val url = "https://serginho.goodbarber.com/front/get_items/939101/26903013/?local=1" //1 item
        val resultList: ArrayList<KnowledgeItem> = ArrayList()
        val m = this.responseHandler.obtainMessage()
        try {
            val urlObject = URL(url)
            val conn = urlObject.openConnection() as HttpURLConnection
            conn.readTimeout = 7000
            conn.connectTimeout = 7000
            conn.requestMethod = "GET"
            conn.doInput = true
            conn.connect()
            val status = conn.getResponseCode()
            if (status != 200) {
                m.obj = ArrayList<KnowledgeItem>()
            } else {
                val inputStream = conn.inputStream
                val jsonAsString = Utils.getTextFromStream(inputStream)
                this.dataCache.saveText(jsonAsString!!, "item_cached")
                Log.d(TAG, jsonAsString)
                if (!jsonAsString.isNullOrEmpty()) m.obj = transformJsonToKnowledgeItem(jsonAsString)
            }
        } catch (e: JSONException){
            Log.e(TAG, e.message)
            m.obj = ArrayList<KnowledgeItem>()
        } catch(e: IOException) {
            Log.e(TAG, e.message)
            m.obj = ArrayList<KnowledgeItem>()
        } finally {
            this.responseHandler.sendMessage(m)
        }
        return resultList
    }

    private fun getItemsFromCache() {
        val itemsCached = this.dataCache.getData("item_cached", false)
        val m = this.responseHandler.obtainMessage()
        if (itemsCached.isNullOrEmpty()) {
            m.obj = ArrayList<KnowledgeItem>()
        } else {
            try {
                m.obj = transformJsonToKnowledgeItem(itemsCached!!)
            } catch (e: JSONException){
                m.obj = ArrayList<KnowledgeItem>()
            }
        }
        this.responseHandler.sendMessage(m)
    }

    fun getData(dataListener: DataListener, isConnected: Boolean){
        // Notify
        this.weakRefDataListener = WeakReference(dataListener)
        this.responseHandler = object : Handler(Looper.getMainLooper()) {
            /*
             * handleMessage() defines the operations to perform when
             * the Handler receives a new Message to process.
             */
            override fun handleMessage(inputMessage: Message) {
                // Gets the image task from the incoming Message object.
                if (inputMessage.obj == null){
                    weakRefDataListener.get()!!.notifyNotRetrieved()
                    return
                }

                val listItem = inputMessage.obj as ArrayList<*>
                if (listItem.isEmpty()){
                    weakRefDataListener.get()!!.notifyNotRetrieved()
                } else {
                    weakRefDataListener.get()!!.notifyRetrieved(listItem as ArrayList<KnowledgeItem>)
                }
            }
        }

        this.handler.post {
            if (isConnected) {
                getItemsFromAPI()
            } else {
                getItemsFromCache()
            }
        }
    }

    fun transformJsonToKnowledgeItem(strJSON: String): ArrayList<KnowledgeItem> {
        val listItems : ArrayList<KnowledgeItem> = ArrayList()

        val jsonObj = JSONObject(strJSON)
        val jsonArray = jsonObj.getJSONArray("items")
        if(jsonArray.length()==0)
            throw JSONException("No items found in the JSON")
        var title: String
        var description: String
        var date: String
        var id: Int

        for (i in 0 until jsonArray.length()) {
            val jsonInner: JSONObject = jsonArray.getJSONObject(i)
            title = jsonInner.get("title").toString()
            description = jsonInner.get("description").toString()
            date = jsonInner.get("date").toString()
            id = jsonInner.get("id").toString().toInt()

            listItems.add(KnowledgeItem(title, description, date, id))
        }
        return listItems
    }

}