import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Date;
import java.util.Scanner;

/**
 * @author Amruta Chavan(agc9066)
 * This class is to create a tcp/udp client
 * */
public class TimeClient {
	
	String serverAddress;
	Socket TCPClientSocket;
	DatagramSocket UDPClientSocket;
	int serverPort;
	int clientPort;
	long date;
	String uname,pwd;
	int tformat; // if 1 then UTC else if 0 then Date
	int noTimesExecute;
	long RTT;
	public TimeClient(String ip,int sport,int clport,int type,long dt,String un,String pw,int tfor,int n){
		
		serverAddress= ip;
		clientPort = clport;
		serverPort = sport;
		date = dt;
		uname = un;
		pwd = pw;
		tformat = tfor;
		noTimesExecute = n;
		if(type == 1){ 
			try{				
				for(int i=0;i<noTimesExecute;i++){
					TCPClientSocket = new Socket(ip,serverPort);
					requestTCP();
				}
			}catch(Exception e){e.printStackTrace();}
		}			
		if(type == 2 ){
			try{
				//System.out.println("cliet on ip\t"+InetAddress.getLocalHost().getHostAddress());
				//System.out.println("In udp client constructor");
				//starting client on random port
				UDPClientSocket = new DatagramSocket();
				int clientPort = UDPClientSocket.getLocalPort();
				for(int i=0;i<noTimesExecute;i++){
					requestUDP();
				}
			}catch(Exception e){e.printStackTrace();}
			
		}
	}
	//client is sending request
	public void requestTCP(){
		
		System.out.println("SENDING CLIENT REQUEST");
        try{
	        OutputStream opS = TCPClientSocket.getOutputStream();
	        ObjectOutputStream opO = new ObjectOutputStream(opS);
	        Message mout = new Message();            		
	        if(uname==null && pwd==null){
	        	
	        	mout.setMessageValues(1,InetAddress.getLocalHost().getHostAddress(),clientPort,uname,pwd,date,"Request to get time");
	        	mout.setStartTime(System.currentTimeMillis());
	        }
	        else if((!uname.equals(null))&&(!pwd.equals(null))){
	        	mout.setMessageValues(2,InetAddress.getLocalHost().getHostAddress(),clientPort,uname,pwd,date,"Request to change time");
	        	mout.setStartTime(System.currentTimeMillis());
	        }
	        opO.flush();
			opO.writeObject(mout);
			System.out.println("THE REQUEST SENT IS ::");
			mout.PrintMessageValues();
			//opS.close();
			this.responseTCP();
	     }catch(Exception e){e.printStackTrace();}
	}
	//client is waiting for response
	public void responseTCP(){
		try{
			System.out.println("CLIENT WAITING FOR RESPONSE");
			InputStream ipS = TCPClientSocket.getInputStream();
    		ObjectInputStream ipO = new ObjectInputStream(ipS);
			Message mIp=(Message) ipO.readObject();
			if(mIp!=null){
				//read message object and perform operations
				//mIp.PrintMessageValues();
				mIp.formatDate();
				if(tformat==1){
					System.out.println("RESPONSE OBTAINED FROM SERVER");
					System.out.println("THE RESPONSE PACKET IS::");
					mIp.PrintMessageValues();
					mIp.formatDate();
					System.out.println("The hop count is:: "+ mIp.hopCount);
					System.out.println(mIp.getProxyNames());
					System.out.println("The RTT for Client is:: "+(System.currentTimeMillis()-mIp.startTime)+"\n");
				} 
				else{
					System.out.println("RESPONSE RECEIVED FROM SERVER");
					System.out.println("THE RESPONSE PACKET IS::");
					mIp.PrintMessageValues();
					
					System.out.println("Time in UTC:: "+mIp.getTime());
					System.out.println("The hop count is:: "+ mIp.hopCount);
					System.out.println(mIp.getProxyNames());
					System.out.println("The RTT for Client is:: "+(System.currentTimeMillis()-mIp.startTime)+"\n");
				}
				
			}
    	//ipS.close();				
		}catch(Exception e){e.printStackTrace();}
		
		
	}
	
	public void requestUDP(){
		try{
			System.out.println("SENDING CLIENT REQUEST");
			Message mout = new Message();		
			if(uname==null && pwd==null){
	        	//System.out.println("Constructing message with id 1");
				mout.setMessageValues(1,InetAddress.getLocalHost().getHostAddress(),clientPort,uname,pwd,date,"Request to get time");
				mout.setStartTime(System.currentTimeMillis());
	        }
	        else if((!uname.equals(null))&&(!pwd.equals(null))){
	        	//System.out.println("Constructing message with id 2");
	        	mout.setMessageValues(2,InetAddress.getLocalHost().getHostAddress(),clientPort,uname,pwd,date,"Request to change time");
	        	mout.setStartTime(System.currentTimeMillis());
	        }
			//System.out.println(serverAddress);
			InetAddress inetIP = InetAddress.getByName(serverAddress);
			//to get byte array of the object
			ByteArrayOutputStream bop = new ByteArrayOutputStream();
			ObjectOutputStream op = new ObjectOutputStream(bop);
			op.writeObject(mout);
			op.flush();
			
			byte MessageData[] = bop.toByteArray();
			int size = MessageData.length;
			
			//creating the datagram packet
			DatagramPacket dpacket = new DatagramPacket(MessageData, MessageData.length,inetIP,serverPort);
			System.out.println("THE REQUEST IS");
			mout.PrintMessageValues();
			RTT=System.currentTimeMillis();
			UDPClientSocket.send(dpacket);
			
			responseUDP();
		}catch(Exception e){e.printStackTrace();}
		
	}
	
	public void responseUDP(){
		try{
			System.out.println("CLIENT WAITING FOR RESPONSE");
			byte MessageData[] = new byte[4096];			
			//Datagram packet to receive packet
			DatagramPacket receivePacket = new DatagramPacket(MessageData, MessageData.length);
			//Receive packet on UDP socket
			UDPClientSocket.receive(receivePacket);
			Message min= new Message();
			//Convert byte array to Message object again
			ByteArrayInputStream bip = new ByteArrayInputStream(receivePacket.getData());
			ObjectInputStream oi = new ObjectInputStream(bip);
			
			min = (Message)oi.readObject();			
			//time format to be displaye to the user
			if(tformat==1){
				System.out.println("\nRESPONSE OBTAINED FROM SERVER");
				System.out.println("THE RESPONSE PACKET IS::");
				min.PrintMessageValues();
				
				min.formatDate();
				System.out.println("The hop count is:: "+ min.hopCount);
				System.out.println(min.getProxyNames());
				System.out.println("The RTT for client is:: "+(System.currentTimeMillis()-RTT)+"\n");
			} 
			else{
				System.out.println("\nRESPONSE OBTAINED FROM SERVER");
				System.out.println("THE RESPONSE PACKET IS::");
				min.PrintMessageValues();
				
				System.out.println("Time in UTC:: "+min.getTime());
				System.out.println("The hop count is:: "+ min.hopCount);
				System.out.println(min.getProxyNames());
				System.out.println("\nThe RTT for client is:: "+(System.currentTimeMillis()-RTT)+"\n");
			}
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	/**
	public static void main(String[] args){
      
		System.out.println("Enter server address and port");
        Scanner sc=new Scanner(System.in);
        String serverAddress = sc.next();
        int port = sc.nextInt();
        System.out.println("Enter the port to start client");
        int clientport = sc.nextInt();
        
		TimeClient tC1 = new TimeClient(serverAddress,port,clientport,1,0,null,null,0,2);
		
		
	}**/
}