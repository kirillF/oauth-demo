package com.github.kirillf.oauth.github;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Arrays;
import java.util.List;

/**
 * Copyrigh Mail.ru Games (c) 2015
 * Created by y.bereza.
 */

public class Followers {
    private List<User> users;

    public static Followers createFromJSON(String json) {
        Gson gson = new GsonBuilder().create();
        Followers followers = new Followers();
        followers.users = Arrays.asList(gson.fromJson(json, User[].class));

        return followers;
    }

    public String asJSON() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        return gson.toJson(users);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (User user : users) {
            sb.append("name: ");
            sb.append(user.getLogin());
            sb.append("\n");
            sb.append("type: ");
            sb.append(user.getType());
            sb.append("\n");
            sb.append("admin: ");
            sb.append(user.getSiteAdmin());
            sb.append("\n");
        }
        return sb.toString();
    }
}
