package com.vsb.kru13.osmzhttpserver;
import android.app.Activity;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;
import androidx.annotation.RequiresApi;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Semaphore;

@RequiresApi(api = Build.VERSION_CODES.O)
public class ClientThread extends Thread {
    private Socket socket;
    private Semaphore semaphore;
    private TelemetryHolder telemetryHolder;

    ClientThread(Socket s, Semaphore semaphore, TelemetryHolder telemetryHolder) {
        this.socket = s;
        this.semaphore = semaphore;
        this.telemetryHolder = telemetryHolder;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void run() {
        try {
            Log.d("SERVER", "Socket Accepted");
            Log.d("CLIENT", "Starting thread");
            OutputStream o = this.socket.getOutputStream();
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(o));
            BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            String location = this.getLocation(in);
            String pathToSD = Environment.getExternalStorageDirectory().getAbsolutePath();

           if (location.equals("/streams/telemetry/data")) {
                this.telemetryHolder.updateGPS();

                byte[] dataByte = this.telemetryHolder.getData().getBytes();
                StringBuilder header = this.getOkHeader("application/json", dataByte);
                out.write(header + "\n");
                out.flush();
                o.write(dataByte);
                o.flush();
           } else {
               if (location.equals("/streams/telemetry")) {
                   location = "/telemetry.html";
               }

               String filePath = pathToSD + location;

                try {
                    String type = MimeTypeMap.getFileExtensionFromUrl(location);
                    type = type.equals("html") ? ("text/html") : ("image/" + type);
                    byte[] dataByte = Files.readAllBytes(Paths.get(filePath));
                    StringBuilder header = this.getOkHeader(type, dataByte);
                    out.write(header + "\n");
                    out.flush();
                    o.write(dataByte);
                    o.flush();
                } catch (NoSuchFileException|FileNotFoundException e) {
                    this.generateNoSuchFile(e, out);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            out.flush();
            this.socket.close();
            Log.d("SERVER", "Socket Closed");
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            this.semaphore.release();
        }
    }
    /**
     * Returns header when file is found
     * @param type
     * @param data
     * @return
     */
    StringBuilder getOkHeader(String type, byte[] data) {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.0 200 OK\n");
        sb.append("Date:" + this.getServerTime()+"\n");
        sb.append("Content-Type: "+type+"\n"); // MIME typ souboru
        sb.append("Content-Length: "+data.length+"\n"); // delka souboru
        return sb;
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
        sb.append("HTTP/1.0 404 NOT FOUND\n");
        sb.append("Date: " + this.getServerTime() +"\n");
        sb.append("Content-Type: text/html\n");
        sb.append("Content-Length: " + content.length() +"\n");
        return sb;
    }
    /**
     * Function that returns location of file
     *
     * @param in
     * @return
     * @throws IOException
     */
    String getLocation(BufferedReader in) throws IOException {
        String location = "";
        for (String line = in.readLine(); !line.isEmpty(); line = in.readLine()) {
            if (line.startsWith("GET")) {
                String[] split = line.split("\\s+");
                location = split[1];
            }
            Log.d("SERVER-REQUEST", line);
        }
        return location.equals("/") ? "/index.html" : location;
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
    /**
     *
     * @param e
     * @param out
     * @throws IOException
     */
    private void generateNoSuchFile(Exception e, BufferedWriter out) throws IOException {
        e.printStackTrace();
        String content = "<html><h1>Soubor nenalezen</h1></html>";
        StringBuilder header = this.getErrorHeader(content);
        out.write(header.toString() + "\n");
        out.write(content);
    }
}