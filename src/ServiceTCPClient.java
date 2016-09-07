import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Date;
/*
 * @author Amruta Chavan
 * 
 * This class is to serve each individul TCP request
 * */
public class ServiceTCPClient extends Thread {
	
	Socket clientSocket;
	TCPTimeProxy ProxySocket;
	Message m;
	InputStream ipS;
	ObjectInputStream ipO;
	OutputStream opS;
	ObjectOutputStream opO;
	String userType;
	long RTTStartTime;
	//constructor to initialize variables
	public ServiceTCPClient(Socket s, String type, TCPTimeProxy ProxyAsClientSocket){
		
		clientSocket = s;
		ProxySocket = ProxyAsClientSocket;
		userType = type;  //this is temp to keep track who is calling
	}
	
	public void run(){
		readMsg();		
	}
	/*
	 * This method is used to get the client request
	 * */
	public void readMsg(){
	
		try{
			System.out.println("PROXY RECEIVED REQUEST MESSAGE");
			ipS = clientSocket.getInputStream();
			ipO = new ObjectInputStream(ipS);
			Message mIp=(Message) ipO.readObject();
			//Save the request time to calculate RTT 
			RTTStartTime=System.currentTimeMillis();
			if(mIp!=null){
				ProxySocket.requestToTCPServer(this,mIp);
			}
			//ipS.close();
		}catch(Exception e){e.printStackTrace();}
	}
	
	/*
	 * This method is used to reply to the client*/
	public void writeMsg(Message mout){
		
		try{	
			System.out.println("PROXY SENT RESPONSE TO ITS CLIENT");
			opS = clientSocket.getOutputStream();
    		opO = new ObjectOutputStream(opS); 
    		opO.flush();
    		//System.out.println("The hop count is :"+mout.hopCount);
    		int h=mout.getHop();
    		//increment the hop count
			mout.setHopCount((h+1));
			String hops="";
			//calculate the RTT
			long t=System.currentTimeMillis()-RTTStartTime;
			try{
				hops= "\nHop is at proxy IP::"+InetAddress.getLocalHost().getHostAddress()+"\t The RTT for it is::"+t;
			}catch(Exception e){}
			mout.setProxyNames(hops);
			//send the reply to the client
			opO.writeObject(mout);
    		opO.flush();
    		//opS.close();			
		}catch(Exception e){e.printStackTrace();}
	}
	
}	
