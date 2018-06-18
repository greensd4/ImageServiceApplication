package com.example.green.imageserviceapp;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class TcpClient {
    private int port;
    private String ip;
    private PrintWriter mBufferOut;
    private Socket socket;
    public TcpClient(int port, String ip)throws Exception{
        this.port = port;
        this.ip = ip;
        createNewConnection();
    }
    private void createNewConnection() throws Exception{
        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(this.ip);
            Log.d("TCP Client", "C: Connecting...");
            //create a socket to make the connection with the server
            this.socket = new Socket(serverAddr, this.port);
            mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        } catch (Exception e) {
            Log.e("TCP", "C: Error", e);
            throw e;
        }
    }
    public void disconnect(){
        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }
        try {
            socket.close();
        }catch (Exception e){
            Log.e("TCP", "Socket: Error", e);
        }
    }
    public void sendFile(File f) throws Exception{
        OutputStream output = socket.getOutputStream();
        InputStream input = socket.getInputStream();
        output.write(f.getName().getBytes());
        byte[] retVal = new byte[1];
        byte t = 1;
        if(input.read(retVal) == 1){
            if(retVal[0] == t){
                byte[] byteFile = getFileByBytes(f);
                output.write(byteFile);
                output.flush();
            }
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
