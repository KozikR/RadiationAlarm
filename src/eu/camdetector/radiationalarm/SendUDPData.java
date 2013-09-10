package eu.camdetector.radiationalarm;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.os.AsyncTask;

public class SendUDPData extends AsyncTask<String, Void, String> {

//    private Exception exception;

    protected String doInBackground(String... IP_data) {
		//int server_port = 12345;
		DatagramSocket s;
		try {
			s = new DatagramSocket();
			InetAddress local;
			try {
				local = InetAddress.getByName(IP_data[0]);
				int msg_length=IP_data[1].length();
				byte[] message = IP_data[1].getBytes();
				int server_port = Integer.parseInt(IP_data[2]);
				DatagramPacket p = new DatagramPacket(message, msg_length,local,server_port);
				try {
					s.send(p);
					return "OK";
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return "E";
				}
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "E";
			}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "E";
		}
    	    	
    }
}