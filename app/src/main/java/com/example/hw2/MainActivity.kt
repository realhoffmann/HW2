package com.example.hw2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
        const val STATE_KEY = "TEXT"
    }

    lateinit var resultTextView: TextView
    var page:Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.e(TAG, "onCreate")

        val loadDataButton = findViewById<Button>(R.id.loadData)
        resultTextView = findViewById(R.id.result_text)
        resultTextView.movementMethod = ScrollingMovementMethod()

        loadDataButton.setOnClickListener {
            loadDataButton.text = getString(R.string.loading_data)
            loadDataButton.isEnabled = false
            resultTextView.text = null

            lifecycleScope.launch(Dispatchers.IO) {
                var intermediateResult = ""
                try {
                    intermediateResult = getContentFromWeb()
                } catch (io: IOException) {
                    withContext(Dispatchers.Main) {
                        val toast = Toast.makeText(applicationContext, "Error, please try again later", Toast.LENGTH_LONG)
                        toast.show()
                        loadDataButton.isEnabled = true
                    }
                }
                if (!intermediateResult.isBlank()) {
                    val result = async(Dispatchers.Default) {
                        parseJsonAddToCardList(intermediateResult)
                    }
                    withContext(Dispatchers.Main) {
                        resultTextView.text = sortedListToString(result.await())
                        loadDataButton.text = getString(R.string.load_data)
                        loadDataButton.isEnabled = true
                    }
                }
            }
        }
    }



    private fun getContentFromWeb(): String {
        page++
        findViewById<TextView>(R.id.page).text = buildString { append("Page: "); append(page) }

        if(URL("https://api.magicthegathering.io/v1/cards?page=$page").toString().isBlank()){
            page = 1
        }

        val url = URL("https://api.magicthegathering.io/v1/cards?page=$page")
        val connection = url.openConnection() as HttpURLConnection


        try {
            val resultAsString = connection.run {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000

                String(inputStream.readBytes())
            }
            return resultAsString
        } finally {
            connection.disconnect()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_KEY, resultTextView.text.toString())
        outState.putString("page", page.toString())
        Log.e(TAG, "onSaveInstanceState")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        resultTextView.text = savedInstanceState.getString(STATE_KEY)
        page = savedInstanceState.getString("page", "1")!!.toInt()
        findViewById<TextView>(R.id.page).text = buildString { append("Page: "); append(page) }
        Log.e(TAG, "onRestoreInstanceState")
    }

}
