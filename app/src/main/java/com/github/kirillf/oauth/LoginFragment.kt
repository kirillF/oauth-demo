package com.github.kirillf.oauth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast

import com.github.kirillf.oauth.auth.WebViewActivity

/**
 * Created by kirillf on 10/25/16.
 */

class LoginFragment : Fragment() {

    private var accessToken = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.login_fragment, container, false)

        val login = v.findViewById<View>(R.id.login_button) as Button
        val showFollowers = v.findViewById<View>(R.id.show_followers) as Button

        val loginClickListener = LoginClickListener()
        login.setOnClickListener(loginClickListener)

        val listener = ShowCommitsListener()
        showFollowers.setOnClickListener(listener)
        return v
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val token = data?.getStringExtra(WebViewActivity.ACCESS_TOKEN) ?: ""
                Toast.makeText(context, token, Toast.LENGTH_SHORT).show()
                accessToken = token
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                if (data?.hasExtra(WebViewActivity.AUTH_ERROR) == true) {
                    val error = data.getStringExtra(WebViewActivity.AUTH_ERROR)
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private inner class LoginClickListener : View.OnClickListener {
        override fun onClick(v: View) {
            val resources = v.resources
            val clientId = resources.getString(R.string.client_id)
            val secret = resources.getString(R.string.client_secret)
            val intent = WebViewActivity.createAuthActivityIntent(v.context, clientId, secret)
            startActivityForResult(intent, REQUEST_CODE)
        }
    }

    private inner class ShowCommitsListener : View.OnClickListener {

        override fun onClick(v: View) {
            val fragment = FollowersFragment.create(accessToken!!, USERNAME)
            requireActivity().supportFragmentManager.beginTransaction().replace(R.id.container, fragment)
                    .addToBackStack(null)
                    .commit()
        }
    }

    companion object {
        private val REQUEST_CODE = 100

        private val USERNAME = "ybereza"
    }
}
