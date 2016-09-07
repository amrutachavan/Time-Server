import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;
/**
 * @author Amruta Chavan(agc9066)
 * This class is to create a main server
 * */
class UDPTimeServer extends Thread{
	
	DatagramSocket UDPServerSocket;
	int UDPServerListenPort;
	String userName,password;
	TimeServer ts;
	UDPTimeServer udpServerU;
	long startTime;
	//constructor to initialize variables
	public UDPTimeServer(int uport, String uname, String pass,TimeServer tsIp){
		try{
			UDPServerListenPort =uport;
			UDPServerSocket = new DatagramSocket(uport);                     
			userName = uname;
			password = pass;
			ts = tsIp;
		}catch(Exception e ){
			e.printStackTrace();
		}		
	}
	
	public void run(){
		while(true){
			UDPServerListen();
		}	
	}	
	//this method is used to accept request
	public void UDPServerListen(){
		try{			
			byte MessageData[] =new byte[4096];			
			//Datagram packet to receive packet
			DatagramPacket receivePacket = new DatagramPacket(MessageData, MessageData.length);
			//Receive packet on UDP socket
			UDPServerSocket.receive(receivePacket);
			//store the request receive time to calculate processing time
			startTime = System.currentTimeMillis();
			
			System.out.println("REQUEST RECEIVED BY SERVER");
			replyFromServer(receivePacket);
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	//method to reply to client
	public void replyFromServer(DatagramPacket receivePacket){
		
		try{
			Message min = new Message();
			byte data[]= receivePacket.getData();
			
			ByteArrayInputStream bip = new ByteArrayInputStream(data);
			ObjectInputStream oi = new ObjectInputStream(bip);
			min = (Message)oi.readObject();			
			System.out.println("THE REQUEST RECEIVED IS ::");
			min.PrintMessageValues();
			System.out.println("SERVER IS REPLYING\n\n");
			//min.PrintMessageValues();	
			Message mout = new Message();
			//perform operation accordint to the request id type
			
			if(min.getmsgId()==1){
			//this is get time request
				mout.setMessageValues(3,min.getClientIp(),min.getClientPort(),null,null,ts.time,"");
				mout.setMessage("03 TIME RETRIEVE SUCCESS");
				mout.setHopCount(min.hopCount);
				String hops="";
				//calculate the processing delay
				long t=System.currentTimeMillis()-startTime;
				try{
					//add the server processing delay in message11111
					hops= "\nHop is at main server ::"+InetAddress.getLocalHost().getHostAddress()+"\t The processing delay is::"+t;
				}catch(Exception e){}
				mout.setProxyNames(hops);				
			}
			
			else if(min.getmsgId()==2){
				//this is a set time request
				if(((!userName.equals(null)) && (!password.equals(null)))&&((userName.equals(min.getuserName())&&(password.equals(min.getPassword()))))){
					ts.time= min.getTime();
					//user name and password match so reset time
					mout.setMessageValues(3,min.getClientIp(),min.getClientPort(),null,null,ts.time,"");
					mout.setMessage("03 TIME RESET SUCCESS");
					mout.setMsgId(3);
					mout.setHopCount(min.hopCount);
					String hops="";
					//calcuate the processing delay
					long t=System.currentTimeMillis()-startTime;
					try{
						//add hop to the message
						hops= "\nHop is at main server ::"+InetAddress.getLocalHost().getHostAddress()+"\t The processing delay is::"+t;
					}catch(Exception e){}
					mout.setProxyNames(hops);					
				}
				
				else{
					//user name or password did not match
					mout.setMessageValues(4,InetAddress.getLocalHost().getHostAddress(),0,null,null,ts.time,"");
					mout.setMessage("04 TIME RESET NOT SUCCESS");
					mout.setMsgId(4);				
					mout.setHopCount(min.hopCount);
					String hops="";
					long t=System.currentTimeMillis()-startTime;
					try{
						hops= "\nHop is at main server ::"+InetAddress.getLocalHost().getHostAddress()+"\t The processing delay is::"+t;
					}catch(Exception e){}
					mout.setProxyNames(hops);
					
				}		
			}				
			
			//convert Message object to byte array 
			ByteArrayOutputStream bop = new ByteArrayOutputStream();
			ObjectOutputStream op = new ObjectOutputStream(bop);
			op.flush();
			op.writeObject(mout);
			byte MessageData[] = bop.toByteArray();		
			
			DatagramPacket dpacket = new DatagramPacket(MessageData, MessageData.length,receivePacket.getAddress(),receivePacket.getPort());
			//send the reply message
			UDPServerSocket.send(dpacket);			
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}	

}

class TCPTimeServer extends Thread {
	
	ServerSocket TCPServerSocket;
	Socket TCPClientSocket;
	InputStream ipS;
	ObjectInputStream ipO;
	OutputStream opS;
	ObjectOutputStream opO;
	String userName,password;
	TCPTimeServer T2;
	TimeServer ts;
	int TCPServerPort;
	long startTime;
	public TCPTimeServer(int port,String uname, String pass,TimeServer tsIp) {
		try{
			TCPServerPort=port;
			TCPServerSocket = new ServerSocket(port);
			userName=uname;
			password = pass;
			ts = tsIp;			
		}catch(Exception e){e.printStackTrace();}
	}
	
	public void run(){
		
		while(true) {
			try {
    			Socket socketAccpt = TCPServerSocket.accept();
    			setClientSocket(socketAccpt);
    			readMessage();
    			socketAccpt.close();
			}catch(Exception e){e.printStackTrace();}
		}
		
	}
	
	public void setClientSocket(Socket s) {
		TCPClientSocket = s;		
	}
	//This mehod is to listen on the client request and read messsage	
	public void readMessage() {	
		try{
			
			ipS = TCPClientSocket.getInputStream();
			ipO = new ObjectInputStream(ipS);
			Message mIp=(Message) ipO.readObject();
			startTime= System.currentTimeMillis();
			System.out.println("REQUEST RECEIVED BY SERVER");
			System.out.println("THE REQUEST RECEIVED IS ::");
			mIp.PrintMessageValues();
			if(mIp!=null){
				//read message object and perform operations
				//mIp.PrintMessageValues();
				if(mIp.getmsgId()==1){
					//msgId=1 then send proxy the time
					//System.out.println("Calling write");
					mIp.setMessageValues(3,mIp.getClientIp(),mIp.getClientPort(),null,null,ts.time,"");
					mIp.setMessage("03 TIME RETRIEVE SUCCESS");
					mIp.setMsgId(3);	
					mIp.setHopCount(mIp.hopCount);
					String hops="";
					long t=System.currentTimeMillis()-startTime;
					try{
						hops= "\nHop is at main server ::"+InetAddress.getLocalHost().getHostAddress()+"\t The processing delay is::"+t;
					}catch(Exception e){}
					mIp.setProxyNames(hops);
				}
				else if(mIp.getmsgId()==2){
					//msgId=2 then client wants to set the time. 
					//check the username and password for access
					if(((!userName.equals(null)) && (!password.equals(null)))&&((userName.equals(mIp.getuserName())&&(password.equals(mIp.getPassword()))))){
						ts.time= mIp.getTime();
						mIp.setMessageValues(3,mIp.getClientIp(),mIp.getClientPort(),null,null,ts.time,"");
						mIp.setMessage("03 TIME RESET SUCCESS");
						mIp.setMsgId(3);
						mIp.setHopCount(mIp.hopCount);
						String hops="";
						long t=System.currentTimeMillis()-startTime;
						try{
							hops= "\nHop is at main server ::"+InetAddress.getLocalHost().getHostAddress()+"\t The processing delay is::"+t;
						}catch(Exception e){}
						mIp.setProxyNames(hops);
					}
					else{
						mIp.setMessageValues(4,InetAddress.getLocalHost().getHostAddress(),0,null,null,ts.time,"");
						mIp.setMessage("04 TIME RESET NOT SUCCESS");
						mIp.setMsgId(4);
						mIp.setHopCount(mIp.hopCount);
						String hops="";
						long t=System.currentTimeMillis()-startTime;
						try{
							hops= "\nHop is at main server ::"+InetAddress.getLocalHost().getHostAddress()+"\t The processing delay is::"+t;
						}catch(Exception e){}
						mIp.setProxyNames(hops);
					}
				}
				writeMessage(mIp);
			}
			//ipS.close();
		}catch(Exception e){e.printStackTrace();}		
	}
	
	/*
	 * to send time back to proxy*/
	public void writeMessage(Message mIp) {
		
		try{	
			System.out.println("SERVER IS REPLYING\n\n");
			opS = TCPClientSocket.getOutputStream();
			opO = new ObjectOutputStream(opS); 
			opO.flush();
			opO.writeObject(mIp);
			opO.flush();
			//opS.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}	
}
/*
 * This class is to create UDP and TCP server threads and start the servers
 * */

public class TimeServer {
	
	public static long time;
	TCPTimeServer tcpServer;
	UDPTimeServer udpServer;
	int TCPPort,UDPPort;
	String uname,password;
	
	public TimeServer(long t, String uname,String password,int udpPort, int tcpPort){
		try{
			String tsIp = InetAddress.getLocalHost().getHostAddress();
			System.out.println("\nTHE IP OF MAIN SERVER IS::"+tsIp+"\n");
			time = t;
			TCPPort= tcpPort;
			UDPPort= udpPort;
			this.uname=uname;
			this.password=password;
			tcpServer = new TCPTimeServer(tcpPort,uname, password, this);
			udpServer = new UDPTimeServer(udpPort, uname, password, this);
			Thread tcp = new Thread(tcpServer);
			Thread udp = new Thread(udpServer);
			tcp.start();
			udp.start();		
			display();
		}catch(Exception e){e.printStackTrace();}
	}
	
	public int TPort(){
		return TCPPort;
	}
	
	public int UPort(){
		return UDPPort;
	}
	
	public void display(){
		int p= UPort();
		//System.out.println("\nThe UDP port  is "+p);
		TCPTimeServer T2 = new TCPTimeServer(p,uname, password, this);
		Thread t2= new Thread(T2);
		t2.start();
		int p2= TPort();
		//System.out.println("\n The TCP port is:: "+p2);
		UDPTimeServer udpServerU = new UDPTimeServer(p2, uname, password,this);
		Thread t= new Thread(udpServerU);
		t.start();
	}
	/**
	public static void main(String args[]) {
		
	//TimeServer tS=new TimeServer(5051); 
		try { 
			System.out.println("MainServer started on" + InetAddress.getLocalHost().getHostAddress());
			TimeServer tS = new TimeServer(5L,null, null, 5000,5001);
			
		}catch(Exception e){}
	 }
	**/
}