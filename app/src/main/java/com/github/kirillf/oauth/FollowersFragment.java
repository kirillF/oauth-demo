package com.github.kirillf.oauth;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.github.kirillf.oauth.github.Followers;
import com.github.kirillf.oauth.okhttp.LoggingInterceptor;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by kirillf on 10/25/16.
 */

public class FollowersFragment extends Fragment {
    private static final String ACCESS_TOKEN = "access_token";
    private static final String USERNAME = "username";

    private String accessToken;
    private String username;

    private TextView textView;
    private Button  showFollowers;
    private boolean asJson;
    private Followers followers;

    private Handler handler = new Handler(Looper.myLooper());

    private LoggingInterceptor.ILogger simpleLogger = new LoggingInterceptor.ILogger() {
        private String log = "";
        @Override
        public void info(final String logString) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    log = log + logString;
                    textView.setText(log);
                }
            });
        }
    };

    public static Fragment create(String accessToken, String username) {
        Bundle args = new Bundle();
        args.putString(ACCESS_TOKEN, accessToken);
        args.putString(USERNAME, username);

        Fragment fragment = new FollowersFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.emails_fragment, container, false);
        textView = (TextView) v.findViewById(R.id.response_content);
        showFollowers = (Button) v.findViewById(R.id.btswitch);

        showFollowers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textView.setText(asJson ? followers.asJSON() : followers.toString());
                asJson = !asJson;
                switchButtonState();
            }
        });
        switchButtonState();

        String followersRequestUrl = getContext().getString(R.string.emails_url);
        parseArguments(getArguments());
        String url = String.format(followersRequestUrl, username, accessToken);
        new EmailsRequest().execute(url);
        return v;
    }

    private void switchButtonState() {
        showFollowers.setEnabled(followers != null);
        showFollowers.setText(asJson ? R.string.show_as_json : R.string.show_as_data);
    }

    private void parseArguments(Bundle arguments) {
        if (arguments.containsKey(ACCESS_TOKEN)) {
            accessToken = arguments.getString(ACCESS_TOKEN);
        }
        if (arguments.containsKey(USERNAME)) {
            username = arguments.getString(USERNAME);
        }
    }

    private class EmailsRequest extends AsyncTask<String, Void, Followers> {

        @Override
        protected Followers doInBackground(String... params) {
            //return getUsingUrlConnection(params[0]);
            return getUsingOkHttp(params[0]);
        }

        @Override
        protected void onPostExecute(Followers res) {
            super.onPostExecute(res);
            if (res != null) {
                followers = res;
                switchButtonState();
            }
            else {
                textView.setText(R.string.error_gettings_followers);
            }
        }

        private Followers getUsingUrlConnection(String url) {
            try {
                URL uri = new URL(url);
                HttpsURLConnection connection = (HttpsURLConnection) uri.openConnection();
                connection.connect();
                int responseCode = connection.getResponseCode();
                if (responseCode != Utils.STATUS_OK) {
                    return null;
                } else {
                    connection.getResponseMessage();
                    return Followers.createFromJSON(Utils.getResponseString(connection.getInputStream()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private Followers getUsingOkHttp(String url) {
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .header("User-Agent", "Technotrack")
                    .build();

            Response response = createClient().newCall(request).execute();
            if (response.isSuccessful()) {
                return Followers.createFromJSON(response.body().string());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private OkHttpClient createClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(new LoggingInterceptor(simpleLogger))
                .build();
    }
}
