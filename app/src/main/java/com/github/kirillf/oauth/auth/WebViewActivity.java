package com.github.kirillf.oauth.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.github.kirillf.oauth.AuthorizationListener;
import com.github.kirillf.oauth.R;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import static com.github.kirillf.oauth.Utils.STATUS_OK;
import static com.github.kirillf.oauth.Utils.getResponseString;

/**
 * Created by kirillf on 10/25/16.
 */

public class WebViewActivity extends AppCompatActivity implements AuthorizationListener {
    public static final String ACCESS_TOKEN = "access_token";
    public static final String AUTH_ERROR = "error";

    private static final String EXTRA_CLIENT_ID = "client_id";
    private static final String EXTRA_CLIENT_SECRET = "client_secret";

    private String clientId;
    private String clientSecret;

    private String authUrlTemplate;
    private String tokenUrlTemplate;
    private String redirectUrl;

    private WebView webView;

    public static Intent createAuthActivityIntent(Context context, String clientId, String secret) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra(EXTRA_CLIENT_ID, clientId);
        intent.putExtra(EXTRA_CLIENT_SECRET, secret);
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_view_activity);
        webView = (WebView) findViewById(R.id.web_view);

        clientId = getIntent().getStringExtra(EXTRA_CLIENT_ID);
        clientSecret = getIntent().getStringExtra(EXTRA_CLIENT_SECRET);

        authUrlTemplate = getString(R.string.auth_url);
        tokenUrlTemplate = getString(R.string.token_url);
        redirectUrl = getString(R.string.callback_url);
    }

    @Override
    protected void onResume() {
        super.onResume();
        onAuthStarted();
        String url = String.format(authUrlTemplate, clientId, "&", redirectUrl, "&");
        URI uri = URI.create(url);
        webView.setWebViewClient(new OAuthWebClient(this));
        webView.loadUrl(uri.toString());
    }

    @Override
    public void onAuthStarted() {

    }

    @Override
    public void onComplete(String token) {
        Intent intent = new Intent();
        intent.putExtra(ACCESS_TOKEN, token);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void onError(String error) {
        Intent intent = new Intent();
        intent.putExtra(AUTH_ERROR, error);
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
    }

    private class OAuthWebClient extends WebViewClient {
        private AuthorizationListener listener;

        public OAuthWebClient(AuthorizationListener listener) {
            this.listener = listener;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();
            if (url.startsWith(view.getResources().getString(R.string.callback_url))) {
                String[] urls = url.split("=");
                new AccessTokenGetter(listener).execute(urls[1]);
                return true;
            }
            return false;
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            listener.onError(error.toString());
        }
    }

    private class AccessTokenGetter extends AsyncTask<String, Void, String> {
        private AuthorizationListener listener;

        AccessTokenGetter(AuthorizationListener listener) {
            this.listener = listener;
        }

        @Override
        protected String doInBackground(String... params) {
            String url = String.format(tokenUrlTemplate, clientId, "&", clientSecret, "&", params[0]);
            try {
                URL u = new URL(url);
                HttpsURLConnection connection = (HttpsURLConnection) u.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.connect();
                int status = connection.getResponseCode();
                if (status != STATUS_OK) {
                    return "Error with status " + status;
                } else {
                    String response = getResponseString(connection.getInputStream());
                    return getAccessToken(response);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String token) {
            if (token.contains("Error")) {
                listener.onError(token);
            } else {
                listener.onComplete(token);
            }
        }
    }

    private String getAccessToken(String response) {
        String[] params = response.split("&");
        return params[0].split("=")[1];
    }
}
