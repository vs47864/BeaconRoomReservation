package de.iosb.fraunhofer.baeconroomreservation.auth;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 *
 * @author Viseslav Sako
 */

public class TokenAuthInterceptor implements Interceptor
{
    private String token;

    public TokenAuthInterceptor(String token)
    {
        this.token = token;
    }

    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();
        Request authenticatedRequest = request.newBuilder()
                .header("Authorization", token).build();
        return chain.proceed(authenticatedRequest);
    }
}
