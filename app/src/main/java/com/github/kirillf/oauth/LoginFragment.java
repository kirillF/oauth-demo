package com.github.kirillf.oauth;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.github.kirillf.oauth.auth.WebViewActivity;

/**
 * Created by kirillf on 10/25/16.
 */

public class LoginFragment extends Fragment {
    private static final int REQUEST_CODE = 100;

    private static final String USERNAME = "ybereza";

    private String accessToken;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.login_fragment, container, false);

        Button login = (Button) v.findViewById(R.id.login_button);
        Button showFollowers = (Button) v.findViewById(R.id.show_followers);

        LoginClickListener loginClickListener = new LoginClickListener();
        login.setOnClickListener(loginClickListener);

        ShowCommitsListener listener = new ShowCommitsListener();
        showFollowers.setOnClickListener(listener);
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                String token = data.getStringExtra(WebViewActivity.ACCESS_TOKEN);
                Toast.makeText(getContext(), token, Toast.LENGTH_SHORT).show();
                accessToken = token;
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                if (data != null && data.hasExtra(WebViewActivity.AUTH_ERROR)) {
                    String error = data.getStringExtra(WebViewActivity.AUTH_ERROR);
                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private class LoginClickListener implements View.OnClickListener {
        public void onClick(View v) {
            Resources resources = v.getResources();
            String clientId = resources.getString(R.string.client_id);
            String secret = resources.getString(R.string.client_secret);
            Intent intent = WebViewActivity.createAuthActivityIntent(v.getContext(), clientId, secret);
            startActivityForResult(intent, REQUEST_CODE);
        }
    }

    private class ShowCommitsListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Fragment fragment = FollowersFragment.create(accessToken, USERNAME);
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}
