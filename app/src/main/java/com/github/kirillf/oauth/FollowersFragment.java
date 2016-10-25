package com.github.kirillf.oauth;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by kirillf on 10/25/16.
 */

public class FollowersFragment extends Fragment {
    private static final String ACCESS_TOKEN = "access_token";
    private static final String USERNAME = "username";

    private String accessToken;
    private String username;

    private TextView textView;

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
        String followersRequestUrl = getContext().getString(R.string.emails_url);
        parseArguments(getArguments());
        String url = String.format(followersRequestUrl, username, accessToken);
        new EmailsRequest().execute(url);
        return v;
    }

    private void parseArguments(Bundle arguments) {
        if (arguments.containsKey(ACCESS_TOKEN)) {
            accessToken = arguments.getString(ACCESS_TOKEN);
        }
        if (arguments.containsKey(USERNAME)) {
            username = arguments.getString(USERNAME);
        }
    }

    private class EmailsRequest extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                String url = params[0];
                URL uri = new URL(url);
                HttpsURLConnection connection = (HttpsURLConnection) uri.openConnection();
                connection.connect();
                int responseCode = connection.getResponseCode();
                if (responseCode != Utils.STATUS_OK) {
                    return "Error with code " + responseCode;
                } else {
                    connection.getResponseMessage();
                    return Utils.getResponseString(connection.getInputStream());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s != null) {
                textView.setText(s);
            } else {
                textView.setText("Something goes wrong");
            }
        }
    }
}
