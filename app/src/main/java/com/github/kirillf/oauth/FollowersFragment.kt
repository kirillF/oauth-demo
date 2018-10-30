package com.github.kirillf.oauth

import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView

import com.github.kirillf.oauth.github.Followers
import com.github.kirillf.oauth.okhttp.LoggingInterceptor
import okhttp3.*

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

import javax.net.ssl.HttpsURLConnection

/**
 * Created by kirillf on 10/25/16.
 */

class FollowersFragment : Fragment() {

    private var accessToken: String? = null
    private var username: String? = null

    private lateinit var textView: TextView
    private lateinit var showFollowers: Button
    private var asJson: Boolean = false
    private var followers: Followers? = null

    private val handler = Handler(Looper.myLooper())

    private var logString = ""

    private fun simpleLogger(log : String) {
        handler.post {
            logString += log
            textView.text = log
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.emails_fragment, container, false)
        textView = v.findViewById<View>(R.id.response_content) as TextView
        showFollowers = v.findViewById<View>(R.id.btswitch) as Button

        showFollowers.setOnClickListener {
            textView.text = if (asJson) followers!!.asJSON() else followers!!.toString()
            asJson = !asJson
            switchButtonState()
        }
        switchButtonState()

        val followersRequestUrl = context!!.getString(R.string.emails_url)
        parseArguments(arguments!!)
        val url = String.format(followersRequestUrl, username, accessToken)
        EmailsRequest().execute(url)
        return v
    }

    private fun switchButtonState() {
        showFollowers.isEnabled = followers != null
        showFollowers.setText(if (asJson) R.string.show_as_json else R.string.show_as_data)
    }

    private fun parseArguments(arguments: Bundle) {
        if (arguments.containsKey(ACCESS_TOKEN)) {
            accessToken = arguments.getString(ACCESS_TOKEN)
        }
        if (arguments.containsKey(USERNAME)) {
            username = arguments.getString(USERNAME)
        }
    }

    private inner class EmailsRequest : AsyncTask<String, Void, Followers>() {

        override fun doInBackground(vararg params: String): Followers? {
            //return getUsingUrlConnection(params[0]);
            return getUsingOkHttp(params[0])
        }

        override fun onPostExecute(res: Followers?) {
            super.onPostExecute(res)
            if (res != null) {
                followers = res
                switchButtonState()
            } else {
                textView.setText(R.string.error_gettings_followers)
            }
        }

        private fun getUsingUrlConnection(url: String): Followers? {
            try {
                val uri = URL(url)
                val connection = uri.openConnection() as HttpsURLConnection
                connection.connect()
                val responseCode = connection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    return null
                } else {
                    connection.responseMessage
                    return Followers.createFromJSON(connection.inputStream.asString())
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return null
        }
    }

    private fun getUsingOkHttp(url: String): Followers {
        try {
            val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "Technotrack")
                    .build()

            val response = createClient().newCall(request).execute()
            if (response.isSuccessful) {
                return Followers.createFromJSON(response.body()!!.string())
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return Followers()
    }

    private fun crateClientWithBasicAuth() : OkHttpClient {
        return OkHttpClient.Builder().authenticator(object : Authenticator {
            override fun authenticate(route: Route, response: Response): Request? {
                val credentials = Credentials.basic("login", "password")
                return response.request().newBuilder().addHeader("Authorization", credentials).build()
            }

        }).build()
    }

    private fun createClient(): OkHttpClient {
        return OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(LoggingInterceptor(::simpleLogger))
                .build()
    }

    companion object {
        private val ACCESS_TOKEN = "access_token"
        private val USERNAME = "username"

        fun create(accessToken: String, username: String): Fragment {
            val args = Bundle()
            args.putString(ACCESS_TOKEN, accessToken)
            args.putString(USERNAME, username)

            val fragment = FollowersFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
