package com.ibrahimelgales.retrofitutils

import android.util.Log
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject

fun ResponseBody?.retrofitCustomErrorResponse(keyErrorsName : String): String? {
    var message = ""
    try {
        if (message.isNotEmpty())
            message = ""
        if (this != null) {
            val body = this.string()

            val jsonObject = JSONObject(body)
            val jsonObjectErrors = jsonObject.getJSONObject(keyErrorsName)
            val keys = jsonObjectErrors.keys()
            while (keys.hasNext()) {
                // loop to get the dynamic key
                val currentDynamicKey = keys.next() as String

                if (jsonObjectErrors.get(currentDynamicKey) is JSONArray) {
                    // get the value of the dynamic key
                    message = if (message.isNotEmpty())
                        "$message . " + jsonObjectErrors.getJSONArray(currentDynamicKey).get(0)
                            .toString() + " "
                    else
                        jsonObjectErrors.getJSONArray(currentDynamicKey).get(0).toString() + " "

                }
                // do something here with the value...
            }
        }
    } catch (e: Exception) {
        Log.e("TAG", "retrofitCustomErrorResponse: ", e)
        e.printStackTrace()
    }

    return if (message.isEmpty()) null else message
}