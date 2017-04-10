package com.jacksondarrow.rnjesus;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    public static int STREAM_SIZE = 1048576; // 1<<20

    public void genJavaUtilRandom(View view) {
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
