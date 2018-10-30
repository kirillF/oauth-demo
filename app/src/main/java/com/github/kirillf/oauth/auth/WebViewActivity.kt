package com.github.kirillf.oauth.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

import com.github.kirillf.oauth.AuthorizationListener
import com.github.kirillf.oauth.R
import com.github.kirillf.oauth.asString

import java.io.IOException
import java.net.URI
import java.net.URL

import javax.net.ssl.HttpsURLConnection

class WebViewActivity : AppCompatActivity(), AuthorizationListener {

    private lateinit var clientId: String
    private lateinit var clientSecret: String

    private lateinit var authUrlTemplate: String
    private lateinit var tokenUrlTemplate: String
    private lateinit var redirectUrl: String

    private lateinit var webView: WebView

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.web_view_activity)
        webView = findViewById(R.id.web_view)

        clientId = intent.getStringExtra(EXTRA_CLIENT_ID)
        clientSecret = intent.getStringExtra(EXTRA_CLIENT_SECRET)

        authUrlTemplate = getString(R.string.auth_url)
        tokenUrlTemplate = getString(R.string.token_url)
        redirectUrl = getString(R.string.callback_url)
    }

    override fun onResume() {
        super.onResume()
        onAuthStarted()
        val url = String.format(authUrlTemplate, clientId, "&", redirectUrl, "&")
        val uri = URI.create(url)
        webView.webViewClient = OAuthWebClient(this)
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(uri.toString())
    }

    override fun onAuthStarted() {

    }

    override fun onComplete(token: String) {
        val intent = Intent()
        intent.putExtra(ACCESS_TOKEN, token)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onError(error: String) {
        val intent = Intent()
        intent.putExtra(AUTH_ERROR, error)
        setResult(Activity.RESULT_CANCELED, intent)
        finish()
    }

    private inner class OAuthWebClient(private val listener: AuthorizationListener) : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            val url = request.url.toString()
            if (url.startsWith(view.resources.getString(R.string.callback_url))) {
                val urls = url.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                AccessTokenGetter(listener).execute(urls[1])
                return true
            }
            return false
        }

        override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
            listener.onError(error.toString())
        }
    }

    private inner class AccessTokenGetter(private val listener: AuthorizationListener) : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg params: String): String? {
            val url = String.format(tokenUrlTemplate, clientId, "&", clientSecret, "&", params[0])
            try {
                val u = URL(url)
                val connection = u.openConnection() as HttpsURLConnection
                connection.requestMethod = "POST"
                connection.doInput = true
                connection.doOutput = true
                connection.connect()
                val status = connection.responseCode
                if (status != HttpsURLConnection.HTTP_OK) {
                    return "Error with status $status"
                } else {
                    val response = connection.inputStream.asString()
                    return getAccessToken(response)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return null
        }

        override fun onPostExecute(token: String) {
            if (token.contains("Error")) {
                listener.onError(token)
            } else {
                listener.onComplete(token)
            }
        }
    }

    private fun getAccessToken(response: String): String {
        val params = response.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return params[0].split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
    }

    companion object {
        val ACCESS_TOKEN = "access_token"
        val AUTH_ERROR = "error"

        private val EXTRA_CLIENT_ID = "client_id"
        private val EXTRA_CLIENT_SECRET = "client_secret"

        fun createAuthActivityIntent(context: Context, clientId: String, secret: String): Intent {
            val intent = Intent(context, WebViewActivity::class.java)
            intent.putExtra(EXTRA_CLIENT_ID, clientId)
            intent.putExtra(EXTRA_CLIENT_SECRET, secret)
            return intent
        }
    }
}
