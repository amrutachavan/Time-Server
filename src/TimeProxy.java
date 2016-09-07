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

/**
 * @author Amruta Chavan(agc9066)
 * This class is to create a proxy server
 * */
class UDPTimeProxy extends Thread{

	DatagramSocket ProxyAsUDPServer; //udp socket to listen to client messages
	DatagramSocket ProxyAsUDPClient; // udp socket to send and receiving message from client
	String serverAddress;
	int UDPProxyListenPort;
	int UDPProxyToServerPort;
	TimeProxy tPrxy;
	InetAddress msgfrom;
	int portfrom;
	UDPTimeProxy udpServerU;
	long RTTStartTime;
	/*
	 * Constructor to initialise the udp proxy server
	 * */
	public UDPTimeProxy(int UDPToServerPort, int UDPListenPort, String ip,TimeProxy tP){
		serverAddress = ip;
		this.UDPProxyToServerPort = UDPToServerPort;
		this.UDPProxyListenPort = UDPListenPort;
		tPrxy = tP;
		try{
			ProxyAsUDPServer = new DatagramSocket(UDPProxyListenPort);
			ProxyAsUDPClient = new DatagramSocket();
		}catch(Exception e){e.printStackTrace();}		
	}

	public void run(){
		
		while(true){
			UDPClientlisten();
		}	
	}
	/**
	 * This function is fo the server to listen to clients*/

	public synchronized void UDPClientlisten(){
		try{
			byte MessageData[] = new byte[4096];			

			//Datagram packet to receive packet
			DatagramPacket receivePacket = new DatagramPacket(MessageData, MessageData.length);
			
			//Receive packet on UDP socket
			ProxyAsUDPServer.receive(receivePacket);
			RTTStartTime=System.currentTimeMillis();
			System.out.println("PROXY RECEIVED REQUEST MESSAGE");

			msgfrom = receivePacket.getAddress();
			portfrom =receivePacket.getPort();	

			Message min;
			byte data[] = receivePacket.getData();
			ByteArrayInputStream bip = new ByteArrayInputStream(data);
			ObjectInputStream oi = new ObjectInputStream(bip);
			min = (Message)oi.readObject();
			//this is for condition 4 if proxy type is tcp or udp only
			requestToUDPServer(receivePacket);		
		}catch(Exception e){e.printStackTrace();}

	} 
	/*
	 * this message is used to send data to udp Server
	 * */
	public synchronized void requestToUDPServer(DatagramPacket dpack){
		try{
			Message min;
			byte data[] = dpack.getData();
			ByteArrayInputStream bip = new ByteArrayInputStream(data);
			ObjectInputStream oi = new ObjectInputStream(bip);
			min = (Message)oi.readObject();
			//incement the hop count
			min.setHopCount(min.getHop()+1);

			//min.PrintMessageValues();		
			ByteArrayOutputStream bop = new ByteArrayOutputStream();
			ObjectOutputStream op = new ObjectOutputStream(bop);
			op.writeObject(min);
			op.flush();

			byte MessageData[] = bop.toByteArray();
			int size = MessageData.length;

			InetAddress srIP = InetAddress.getByName(serverAddress);
			//creating the datagram packet
			DatagramPacket dpacket = new DatagramPacket(MessageData, MessageData.length,srIP,UDPProxyToServerPort);
			//Proxy is sending request to Server
			System.out.println("PROXY FORWARDED REQUEST TO ITS SERVER");
			ProxyAsUDPClient.send(dpacket);
			responseFromUDPServer();
		}catch(Exception e){
			e.printStackTrace();
		}

	}
	/*
	 * This method is used to receive Packet from server
	 * */

	public synchronized void responseFromUDPServer(){
		try{
			System.out.println("PROXY RECEIVED RESPONSE FROM ITS SERVER");
			byte MessageData[] = new byte[4096];			
			//Datagram packet to receive packet
			DatagramPacket receivePacket = new DatagramPacket(MessageData, MessageData.length);
			//Receive packet on UDP socket
			ProxyAsUDPClient.receive(receivePacket);
			//receiving the packet 	
			UDPClientResponse(receivePacket);

		}catch(Exception e){e.printStackTrace();}
	}

	/**
	 * This method is used to send datapackets to client
	 * */
	public synchronized void UDPClientResponse(DatagramPacket dpack){
		Message mout = new Message();
		try{
			byte msg[] = dpack.getData();
			byte replyMsg[] = dpack.getData();
			ByteArrayInputStream bip = new ByteArrayInputStream(dpack.getData());
			ObjectInputStream oi = new ObjectInputStream(bip);
			mout = (Message)oi.readObject();
			int h=mout.getHop();
			mout.setHopCount((h+1));
			String hops="";
			long t=System.currentTimeMillis()-RTTStartTime;
			try{
				hops= "\nHop is at proxy IP::"+InetAddress.getLocalHost().getHostAddress()+"\t The RTT for it is::"+t;
			}catch(Exception e){}
			mout.setProxyNames(hops);
			ByteArrayOutputStream bop = new ByteArrayOutputStream();
			ObjectOutputStream op = new ObjectOutputStream(bop);
			op.writeObject(mout);
			op.flush();

			byte MessageData[] = bop.toByteArray();
			
			DatagramPacket sendPacket = new DatagramPacket(MessageData, MessageData.length,msgfrom,portfrom);
			//send packet to client
			System.out.println("PROXY SENT RESPONSE TO ITS CLIENT\n\n");
			ProxyAsUDPServer.send(sendPacket);

		}catch(Exception e){e.printStackTrace();}

	}

}
/*
 * This class is used to create tcp proxy 
 * */
class TCPTimeProxy extends Thread{

	Socket ProxyAsTCPClientSoc; // this socket is used by proxy as client to communicate with server
	ServerSocket ProxyAsTCPServerSoc;	//this socket is used by proxy as server to accept client requests
	String serverAddress;
	int TCPProxyListenPort;
	int TCPProxyToServerPort;
	TCPTimeProxy tcpServerT;
	TimeProxy tPrxy;
	
	/**
	 * Constructor for TCPProxy
	 * */
	public TCPTimeProxy(int TCPToServerPort, int TCPListenPort, String ip,TimeProxy Pt){
		serverAddress = ip;
		this.TCPProxyToServerPort = TCPToServerPort;
		this.TCPProxyListenPort = TCPListenPort;
		tPrxy = Pt;
		serverAddress = ip;		
		try{
			ProxyAsTCPServerSoc = new ServerSocket(TCPProxyListenPort);
			
			}catch(Exception e){e.printStackTrace();}			
	}

	
	public void run(){
		try{
			while(true) {
				Socket socketAccpt = this.ProxyAsTCPServerSoc.accept();
				//System.out.println("Port---->"+TCPProxyToServerPort);
				ProxyAsTCPClientSoc = new Socket(serverAddress,TCPProxyToServerPort);
				ServiceTCPClient cS = new ServiceTCPClient(socketAccpt,"ProxyServer",this);
				cS.start();
				//socketAccpt.close();
			}	
		}catch(Exception e){
		//e.printStackTrace();
		}		
	}

	/**
	 * this method is used to send request to Main TCP server*/
	public void requestToTCPServer(ServiceTCPClient obj,Message m){
		try{
			System.out.println("PROXY FORWARDED REQUEST TO ITS SERVER");
			OutputStream opS = ProxyAsTCPClientSoc.getOutputStream();
			ObjectOutputStream opO = new ObjectOutputStream(opS);
			m.setHopCount(m.getHop()+1);
			opO.flush();
			//sending message to TCPServer
			opO.writeObject(m);
			opO.flush();
			ResponseFromTCPServer(obj);
			} catch(Exception e){
				e.printStackTrace();
			}
		}	

	/*
	 * This method is used to get Response from server
	 * */

	public void ResponseFromTCPServer(ServiceTCPClient obj){
		try{
			//System.out.println("In proxy response");
			InputStream ipS = ProxyAsTCPClientSoc.getInputStream();
			ObjectInputStream ipO = new ObjectInputStream(ipS);
			//reading  the replpy from stream
			Message mIp=(Message) ipO.readObject();
			if(mIp!=null){
				obj.writeMsg(mIp);
			}
			System.out.println("PROXY RECEIVED RESPONSE FROM ITS SERVER");
			//ipS.close();			
		}catch(Exception e){e.printStackTrace();}

	}

	Socket getProxyAsClientSocket(){
		return ProxyAsTCPClientSoc;
	}
}

/**
 * main class having the objects of both tcpServer and UDP server
 * */
public class TimeProxy {


	Date proxyD;
	String serverAddress;
	TCPTimeProxy tcpServer;
	UDPTimeProxy udpServer;
	String proxyType;
	int TCPPrxyPt,UDPPrxyPt,USPort,TSPort;
	//Socket TCPListen, TCPReq, UDPListen, UDPReq;
	
	//This is constructor for TCP Proxy
	public TimeProxy(String ip, int UDPSport,int TCPSport, int UDPPrxyPort, int TCPPrxyPort,String ProxyType){
		if(UDPSport==-1){
			UDPSport= TCPSport;
		}
		if(TCPSport==-1){
			TCPSport= UDPSport;
		}
		
		serverAddress = ip;		
		tcpServer = new TCPTimeProxy(TCPSport, TCPPrxyPort, serverAddress,this);
		
		udpServer = new UDPTimeProxy(UDPSport, UDPPrxyPort, ip,this);
		
		TCPPrxyPt = TCPPrxyPort;
		UDPPrxyPt=UDPPrxyPort;
		USPort= UDPSport;
		TSPort=TCPSport;
		proxyType=ProxyType;
		try{
			System.out.println("PROXY STARTED ON IP::" + InetAddress.getLocalHost().getHostAddress()+"\n");
			
		}catch(Exception e){e.printStackTrace();}
		//starting tcp and udp servers
		Thread tcp = new Thread(tcpServer);
		Thread udp = new Thread(udpServer);	
		tcp.start();
		udp.start();
	}

	public int tPport(){
		return TCPPrxyPt;
	}

	public int uPport(){
		return UDPPrxyPt;
	}

	public int tSport(){
		return TSPort;
	}
	
	public int uSport(){
		return USPort;
	}
	
	
	public String PrxyType(){
		return proxyType;
	}

	public String ProxyIp(){
		return serverAddress;
	}
	
	public void display(){
		//System.out.println("\nThe tcp proxy port is "+tPport);
		int ps = tSport();
		//System.out.println("\nThe tcp proxy server port is "+tSport);
		int pp = tPport();
		UDPTimeProxy udpServerU= new UDPTimeProxy(ps, pp, serverAddress,this);
		Thread t = new Thread(udpServerU);
		//System.out.println("\nThe udp proxy port is "+tPport);
		t.start();
		//System.out.println("\nThe udp proxy port is "+tPport);
		int ps1 = uSport();
		//System.out.println("\nThe udp proxy port is "+tPport);
		int pp1 = uPport();
		TCPTimeProxy tcpServerT= new TCPTimeProxy(ps1, pp1, serverAddress,this);
		Thread t1= new Thread(tcpServerT);
		t1.start();
	}
	
	/**public static void main(String args[]){

		System.out.println("Enter server address and port");
		Scanner sc = new Scanner(System.in);
		String s = sc.next(); 
		int Sport = sc.nextInt();
		//System.out.println("Enter port to start proxy");
		int Pport = sc.nextInt();
		TimeProxy tS=new TimeProxy(s,5000,5001,4000,4001); 
		//Thread t1 = new Thread(new TimeProxy(s,Sport,Pport));

		try{
			System.out.println("ProxyServer started on" + InetAddress.getLocalHost().getHostAddress());
		}catch(Exception e){}

     } **/   
}