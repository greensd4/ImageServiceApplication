package com.example.green.imageserviceapp;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class TcpClient {
    private int port;
    private String ip;
    private Socket socket;
    //private File fileToSend;

    public TcpClient(int port, String ip)throws Exception{
        this.port = port;
        this.ip = ip;
        createNewConnection();
    }
    private void createNewConnection() throws Exception{
        new Thread(new Runnable() {
            @Override
            public void run() {
                InetAddress serverAddr;
                try {
                    try{
                        //here you must put your computer's IP address.
                        serverAddr = InetAddress.getByName("10.0.2.2");
                    }catch (Exception e) {
                        Log.e("TCP", "ADD: Error", e);
                        return;
                    }
                    Log.d("TCP Client", "M: Connecting...");
                    //create a socket to make the connection with the server
                    socket = new Socket(serverAddr, 8500);
                    //mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                } catch (Exception e) {
                    Log.e("TCP", "Socket: Error", e);
                }
            }
        }).start();
    }
    public void disconnect(){
        try {
            socket.close();
        }catch (Exception e){
            Log.e("TCP", "Socket: Error", e);
        }
    }
    public void sendFile(File file) throws Exception{

        try {
            Log.d("TCP Client", "M: Sending a file..." +file.getName());

            PrintWriter output = new PrintWriter(socket.getOutputStream());
            InputStream input = socket.getInputStream();
            String nameOfFile = file.getName();
            byte[] byteFile = getFileByBytes(file);
            String[] args = createArgs(byteFile,nameOfFile);
            //creating a new CommandRecievedEventArgs object
            CommandRecievedEventArgs crea = new CommandRecievedEventArgs(7,args,null);

            //cast CommandRecievedEventArgs into JSon
            String bytesAsString = crea.toJson();
            //send Command to service.
            output.print(bytesAsString);
            output.flush();
            //output.close();
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

    String[] createArgs(byte[] imageAsBytes, String imageName) {
        String[] args = new String[2];
        String s = new String(imageAsBytes);
        args[0] = s;
        args[1] = imageName;
        return args;
    }
}
