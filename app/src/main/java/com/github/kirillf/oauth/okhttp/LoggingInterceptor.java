package com.github.kirillf.oauth.okhttp;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Copyrigh Mail.ru Games (c) 2015
 * Created by y.bereza.
 */

public class LoggingInterceptor implements Interceptor {

    public interface ILogger {
        void info(String logString);
    }

    private ILogger logger;

    public LoggingInterceptor(ILogger logger) {
        this.logger = logger;
    }

    @Override public Response intercept(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();

        long t1 = System.nanoTime();
        logger.info(String.format("Sending request %s %n%s",
                request.url(), request.headers()));

        Response response = chain.proceed(request);

        long t2 = System.nanoTime();
        logger.info(String.format("Received response for %s in %.1fms%n%s",
                response.request().url(), (t2 - t1) / 1e6d, response.headers()));

        return response;
    }
}