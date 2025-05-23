package com.lzlown.iptv.videocache;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.*;

import static com.lzlown.iptv.videocache.Preconditions.checkArgument;
import static com.lzlown.iptv.videocache.Preconditions.checkNotNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

class Pinger {
    private static String TAG = Pinger.class.getName();
    private static final String PING_REQUEST = "ping";
    private static final String PING_RESPONSE = "ping ok";

    private final ExecutorService pingExecutor = Executors.newSingleThreadExecutor();
    private final String host;
    private final int port;

    Pinger(String host, int port) {
        this.host = checkNotNull(host);
        this.port = port;
    }

    boolean ping(int maxAttempts, int startTimeout) {
        checkArgument(maxAttempts >= 1);
        checkArgument(startTimeout > 0);

        int timeout = startTimeout;
        int attempts = 0;
        while (attempts < maxAttempts) {
            try {
                Future<Boolean> pingFuture = pingExecutor.submit(new PingCallable());
                boolean pinged = pingFuture.get(timeout, MILLISECONDS);
                if (pinged) {
                    return true;
                }
            } catch (TimeoutException e) {
                Log.w(TAG, "Error pinging server (attempt: " + attempts + ", timeout: " + timeout + "). ");
            } catch (InterruptedException | ExecutionException e) {
                Log.e(TAG, "Error pinging server due to unexpected error", e);
            }
            attempts++;
            timeout *= 2;
        }
        String error = String.format(Locale.US, "Error pinging server (attempts: %d, max timeout: %d). " +
                        "If you see this message, please, report at https://github.com/danikula/AndroidVideoCache/issues/134. " +
                        "Default proxies are: %s"
                , attempts, timeout / 2, getDefaultProxies());
        Log.e(TAG, error, new ProxyCacheException(error));
        return false;
    }

    private List<Proxy> getDefaultProxies() {
        try {
            ProxySelector defaultProxySelector = ProxySelector.getDefault();
            return defaultProxySelector.select(new URI(getPingUrl()));
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    boolean isPingRequest(String request) {
        return PING_REQUEST.equals(request);
    }

    void responseToPing(Socket socket) throws IOException {
        OutputStream out = socket.getOutputStream();
        out.write("HTTP/1.1 200 OK\n\n".getBytes());
        out.write(PING_RESPONSE.getBytes());
    }

    private boolean pingServer() throws ProxyCacheException {
        String pingUrl = getPingUrl();
        HttpUrlSource source = new HttpUrlSource(pingUrl);
        try {
            byte[] expectedResponse = PING_RESPONSE.getBytes();
            source.open(0);
            byte[] response = new byte[expectedResponse.length];
            source.read(response);
            boolean pingOk = Arrays.equals(expectedResponse, response);
            Log.i(TAG, "Ping response: `" + new String(response) + "`, pinged? " + pingOk);
            return pingOk;
        } catch (ProxyCacheException e) {
            Log.e(TAG, "Error reading ping response", e);
            return false;
        } finally {
            source.close();
        }
    }

    private String getPingUrl() {
        return String.format(Locale.US, "http://%s:%d/%s", host, port, PING_REQUEST);
    }

    private class PingCallable implements Callable<Boolean> {

        @Override
        public Boolean call() throws Exception {
            return pingServer();
        }
    }

}
