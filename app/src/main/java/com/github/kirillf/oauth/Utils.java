package com.github.kirillf.oauth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by kirillf on 10/25/16.
 */

public class Utils {
    public static int STATUS_OK = 200;

    public static String getResponseString(InputStream stream) throws IOException {
        StringBuilder sb = new StringBuilder();
        try {
            String line;
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            bufferedReader.close();
        } finally {
            stream.close();
        }
        return sb.toString();
    }
}
