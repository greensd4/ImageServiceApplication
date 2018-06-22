package com.example.green.imageserviceapp;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FileSender {
    private int port;
    private String ip;
    private Socket socket;
    public boolean connected;

    public FileSender(int port, String ip){
        this.ip = ip;
        this.port = port;
        connected = false;
        try {
            createConnection();
        }catch (Exception e) {
            connected = false;
            return;
        }
        connected = true;
    }
    private void createConnection() throws Exception {
        try {
            InetAddress serverAddr = InetAddress.getByName(this.ip);
            try {
                this.socket = new Socket(serverAddr, this.port);
            } catch (Exception e) {
                Log.e("TCP", "S: Error", e);
                throw e;
            }
        } catch (Exception e) {
            Log.e("TCP", "C: Error", e);
        }
    }
    public void sendFile(File file) {
        try {
            Log.d("TCP Client", "M: Sending a file..." + file.getName());
            OutputStream output = socket.getOutputStream();
            InputStream input = socket.getInputStream();
            //Write name to the server
            output.write(file.getName().getBytes());
            //Get confirm from server that it can send the photo
            byte[] confirmation = new byte[1];
            int res = input.read(confirmation);
            //If it read the confirmation byte
            if(res == 1) {
                //if the byte that was returned was 0 can not send photo and return
                if(confirmation[0] == 0)
                    return;
            } else {
                return;
            }
            byte[] byteFile = getFileByBytes(file);
            output.write(byteFile);
            output.flush();
        } catch (Exception e) {
            Log.e("TCP", "C: Error", e);
        }
    }
    private byte[] getFileByBytes(File file) throws Exception{
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        FileInputStream fis = new FileInputStream(file);
        int i;
        while ((i = fis.read(buffer)) != -1) {
            stream.write(buffer, 0, i);
        }
        return stream.toByteArray();
    }

}
