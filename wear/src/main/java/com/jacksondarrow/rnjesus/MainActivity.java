package com.jacksondarrow.rnjesus;

import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.Random;
import java.util.logging.Handler;

import static android.R.attr.tag;

public class MainActivity extends Activity {
    private TextView mTextView;

    int MESSAGE_CONNECTIVITY_TIMEOUT = 1;
    long NETWORK_CONNECTIVITY_TIMEOUT_MS = 10000;

    private final static String tag = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        final ConnectivityManager mConnManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Network activeNetwork = mConnManager.getActiveNetwork();

        int MIN_BANDWIDTH_KBPS = 320;

        if (activeNetwork != null) {
            int bandwidth = mConnManager.getNetworkCapabilities(activeNetwork).getLinkDownstreamBandwidthKbps();

            if (bandwidth < MIN_BANDWIDTH_KBPS) {
                Log.println(Log.ERROR, tag, "REQUEST A HIGHER BANDWIDTH NETWORK");
                // Request a high-bandwidth network
            }
        } else {
            Log.println(Log.ERROR, tag, "ALREADY ON HIGH BANDWIDTH NETWORK");
            // You already are on a high-bandwidth network, so start your network request
        }

        NetworkRequest request = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        ConnectivityManager.NetworkCallback mNetworkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                if (mConnManager.bindProcessToNetwork(network)) {
                    Log.println(Log.ERROR, tag, "SOCKET CONNECTIONS NOW WORK");
                    // socket connections will now use this network
                } else {
                    Log.println(Log.ERROR, tag, "NO PERMISSION");
                    // app doesn't have android.permission.INTERNET permission
                }
            }
        };

        try {
            mConnManager.requestNetwork(request, mNetworkCallback);
        }catch (Exception e) {
            Log.println(Log.ERROR, tag, "CAUGHT E");
            Log.wtf(tag, e);

        }
    }



    public static int STREAM_SIZE = 1048576/ 2048; // 1<<20

    public void genJavaUtilRandom(View view) {
        /*
        int size = Integer.parseInt(((EditText)findViewById(R.id.size)).getText().toString());
        try (ByteStreamer streamer = new ByteStreamer(view)) {
            Random random = new Random();

            byte[] data = new byte[STREAM_SIZE];
            while (size / STREAM_SIZE > 0) {
                random.nextBytes(data);
                streamer.writeBytes(data);
                size -= STREAM_SIZE;
            }

            byte[] remainder = new byte[size];
            streamer.writeBytes(remainder);
        } catch (IOException e) {
            updateStatus(view, e.toString());
        }
        */
    }

    public void genSecureRandom(View view) {
        int size = Integer.parseInt(((EditText)findViewById(R.id.size)).getText().toString());
        try (ByteStreamer streamer = new ByteStreamer(view)) {
            SecureRandom random = new SecureRandom();

            byte[] data = new byte[STREAM_SIZE];
            while (size / STREAM_SIZE > 0) {
                random.nextBytes(data);
                streamer.writeBytes(data);
                size -= STREAM_SIZE;
            }

            byte[] remainder = new byte[size];
            streamer.writeBytes(remainder);
        } catch (IOException e) {
            updateStatus(view, e.toString());
        }
    }

    public void genDevURandom(View view) {
        int size = Integer.parseInt(((EditText)findViewById(R.id.size)).getText().toString());
        try (ByteStreamer streamer = new ByteStreamer(view)){
            FileInputStream fileInput = new FileInputStream(new File("/dev/urandom"));

            byte[] data = new byte[STREAM_SIZE];
            while (size / STREAM_SIZE > 0) {
                fileInput.read(data);
                streamer.writeBytes(data);
                size -= STREAM_SIZE;
            }

            byte[] remainder = new byte[size];
            streamer.writeBytes(remainder);

        } catch (IOException e) {
            updateStatus(view, e.toString());
            updateStatus(view, e.getMessage());
        }
    }
    public void genDevRandom(View view) {
        int size = Integer.parseInt(((EditText)findViewById(R.id.size)).getText().toString());
        try (ByteStreamer streamer = new ByteStreamer(view)){
            FileInputStream fileInput = new FileInputStream(new File("/dev/random"));

            STREAM_SIZE = 64;
            byte[] data = new byte[STREAM_SIZE];
            while (size / STREAM_SIZE > 0) {
                int bytes_read = fileInput.read(data);
                streamer.writeBytes(data, bytes_read);
                size -= STREAM_SIZE;
            }


        } catch (IOException e) {
            updateStatus(view, e.toString());
            updateStatus(view, e.getMessage());
        }
    }

    public void updateStatus(View view, String line) {
        TextView text = (TextView) findViewById(R.id.largeText);
        String currentText = text.getText().toString();

        text.setText(currentText + "\n" + line);
    }
    public class ByteStreamer implements AutoCloseable {
        public Socket clientSocket;
        public DataOutputStream output;


        public ByteStreamer(View view) throws IOException {
            updateStatus(view, "WE HAVE NETWORK");
            String hostname = ((EditText)findViewById(R.id.hostname)).getText().toString();
            int port = Integer.parseInt(((EditText)findViewById(R.id.port)).getText().toString());

            clientSocket = new Socket(hostname, port);
            output = new DataOutputStream(clientSocket.getOutputStream());
      }

        public void writeBytes(byte[] bytes, int length) throws IOException {
            output.write(bytes, 0, length);
        }
        public void writeBytes(byte[] bytes) throws IOException {
            output.write(bytes);
        }

        public void close() {
            try {
                clientSocket.close();
            } catch (IOException ignored) {}
        }
    }
}
