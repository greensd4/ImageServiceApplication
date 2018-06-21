package com.example.green.imageserviceapp;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TcpClient {
    private Socket socket;
    private final static Lock mutex = new ReentrantLock(true);

    public TcpClient(int port, String ip)throws Exception{
        InetAddress serverAddr = InetAddress.getByName(ip);
        try {
            this.socket = new Socket(ip, port);
        } catch (Exception e) {
            throw  e;
        }
    }

    public void disconnect(){
        try {
            socket.close();
        }catch (Exception e){
            Log.e("TCP", "Socket: Error", e);
        }
    }
    public void sendFile(File file) throws Exception{
        final File f = file;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("TCP Client", "M: Sending a file..." +f.getName());
                    OutputStream output = socket.getOutputStream();
                    InputStream input = socket.getInputStream();
                    //Write name to the server
                    mutex.lock();
                    output.write(f.getName().getBytes());
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
                    byte[] byteFile = getFileByBytes(f);
                    output.write(byteFile);
                    output.flush();
                    mutex.unlock();;
                } catch (Exception e) {
                    Log.e("TCP", "C: Error", e);
                }
            }
        });
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

    private String[] createArgs(byte[] imageAsBytes, String imageName) {
        String[] args = new String[2];
        String s = new String(imageAsBytes);
        args[0] = s;
        args[1] = imageName;
        return args;
    }
}
