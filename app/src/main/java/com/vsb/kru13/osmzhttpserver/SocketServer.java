package com.vsb.kru13.osmzhttpserver;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import androidx.annotation.RequiresApi;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Semaphore;

@RequiresApi(api = Build.VERSION_CODES.O)
public class SocketServer extends Thread {

    ServerSocket serverSocket;
    public final int port = 12345;
    boolean bRunning;
    private static final int MAX_AVAILABLE = 100;
    private final Semaphore semaphore = new Semaphore(MAX_AVAILABLE, true);
    private Handler handler;
    private Activity activity;

    public SocketServer(Handler handler, Activity activity) {
        this.handler = handler;
        this.activity = activity;
    }

    public void close() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            Log.d("SERVER", "Error, probably interrupted in accept(), see log");
            e.printStackTrace();
        }
        bRunning = false;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void run() {
        try {
            Log.d("SERVER", "Creating Socket");
            serverSocket = new ServerSocket(port);
            bRunning = true;

            while (bRunning) {
                Log.d("SERVER", "Socket Waiting for connection");
                Socket s = serverSocket.accept();

                if(this.semaphore.tryAcquire(1)) {
                    Log.d("FREE PERMITS", String.valueOf(this.semaphore.availablePermits()));

                    Bundle bundle = new Bundle();
                    bundle.putInt("usersCount", MAX_AVAILABLE - this.semaphore.availablePermits());
                    bundle.putInt("availablePermits", this.semaphore.availablePermits());
                    Message message = handler.obtainMessage();
                    message.setData(bundle);
                    message.sendToTarget();

                    new ClientThread(s, this.semaphore, this.activity).start();
                } else {
                    OutputStream o = s.getOutputStream();
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(o));

                    String content = "<html><h1>Soubor nenalezen</h1></html>";
                    StringBuilder header = this.getErrorHeader(content);
                    out.write(header.toString() + "\n");
                    out.write(content);
                    out.flush();
                    s.close();
                }
            }
        } catch (IOException e) {
            if (serverSocket != null && serverSocket.isClosed())
                Log.d("SERVER", "Normal exit");
            else {
                Log.d("SERVER", "Error");
                e.printStackTrace();
            }
        } finally {
            serverSocket = null;
            bRunning = false;
            this.semaphore.release();
        }
    }

    /**
     * Returns error header
     *
     * @param content
     *
     * @return
     */
    StringBuilder getErrorHeader(String content) {
        StringBuilder sb = new StringBuilder();

        sb.append("HTTP/1.0 503 SERVER TOO BUSY\n");
        sb.append("Date: " + this.getServerTime() +"\n");
        sb.append("Content-Type: text/html\n");
        sb.append("Content-Length: " + content.length() +"\n");

        return sb;
    }

    /**
     * Function that returns server time
     *
     * @return
     */
    String getServerTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime());
    }
}

