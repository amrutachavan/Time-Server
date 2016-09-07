import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/*
 * @author Amruta Chavan (agc9066)
 * This is the main class  
 * */
public class tsapp {
	

	public static void main(String args[]){
		
		String opt = args[0];
		switch(opt){
			case ("-s") :
				//if server then perform following operations
				long t=0;
				int udpPort=0, tcpPort=0;
				String uname=null,password=null;
				int p=1;
				udpPort = Integer.parseInt(args[args.length-2]);
				System.out.println("UDP SERVER STARTING ON PORT::"+udpPort);
				tcpPort = Integer.parseInt(args[args.length-1]);
				System.out.println("TCP SERVER STARTING ON PORT::"+tcpPort);
				if(args[p].equals("-T")){
					//get time to be set
					t = Long.valueOf(args[p+1]).longValue();
					p=p+2;
				}else{
					System.out.println("Wrong arguments -T is missing!!");
					System.exit(0);
				}
				
				if((args[p].equals("--user"))&&(args[p+2].equals("--pass"))){
					//get user name password if any
					uname = args[p+1];
					password = args[p+3];
				
				}
				
				TimeServer tS = new TimeServer(t,uname,password,udpPort,tcpPort);
			break;
			
			case ("-c"):
				//if client then perform following operations
				String serverIp;
				int serverPort ;
				int type=2;//if type =1 then tcp; type = 2
				String un=null, pass=null;
				int tformat=1;// if tformat = 1 then calender else UTC
				long timeToSet=0;
				int noReq=1;
				int i=1;
				serverIp=args[i];
				i++;
				serverPort= Integer.parseInt(args[args.length-1]);
				if(args[i].equals("-z")){
					//get the time format 
					tformat=0;
					i++;
				}
				//System.out.println("\n Time format is ::"+tformat);
				if(args[i].equals("-T")){
					//get the time to reset to if set request
					timeToSet = Integer.parseInt(args[i+1]);
				
					i=i+2;					
				}
				
				if((args[i].equals("--user"))&&(args[i+2].equals("--pass"))){
					un = args[i+1];
					pass = args[i+3];
					i=i+4;
				}
					if(args[i].equals("-n")){
					noReq = Integer.parseInt(args[i+1]);
					i=i+2;
				}				
				if(args[i].equals("-u")||args[i].equals("-t")){
					if(args[i].equals("-u")){
						type=2;
					}else if(args[i].equals("-t")){
						type=1;
					}
					i++;
				}			
				
				TimeClient tC1 = new TimeClient(serverIp,serverPort,4000,type,timeToSet,un,pass,tformat,noReq);	
			break;
			
			case("-p"):
				//if proxy server then perform following operations	
			String servIp;
			int UDPToServPort=-1, TCPToServPort=-1,ProxyUDP,ProxyTCP;
			int k=1;
			String proxyType="-d";
			servIp = args[k];
			ProxyUDP = Integer.parseInt(args[args.length-2]);
			System.out.println("UDP PROXY STARTING ON PORT::"+ProxyUDP);
			ProxyTCP = Integer.parseInt(args[args.length-1]);
			System.out.println("TCP PROXY STARTING ON PORT::"+ProxyTCP);
			
			k++;
			
			if((args[k].equals("-t"))||(args[k].equals("-u"))){
				proxyType= args[k];
				k++;
			}
			
			//get the tcp and udp server proxy
			if((args[k].equals("--proxy-udp"))||(args[k].equals("--proxy-tcp"))){
				if(args[k].equals("--proxy-udp")){					
					UDPToServPort = Integer.parseInt(args[k+1]);					
					if(args[k+2].equals("--proxy-tcp")){
						TCPToServPort = Integer.parseInt(args[k+3]);
						
						k++;
					}
					k++;
				}
				
				else{
					if(args[k].equals("--proxy-tcp")){
						TCPToServPort = Integer.parseInt(args[k+1]);						
						if(args[k+2].equals("--proxy-udp")){							
							UDPToServPort = Integer.parseInt(args[k+3]);
							k++;
						}
						k++;
					}
				}
			}
			
			TimeProxy tPS=new TimeProxy(servIp,UDPToServPort,TCPToServPort,ProxyUDP,ProxyTCP,proxyType); 
			break;
			default: System.out.println("Wrong input");
			System.exit(0);
		}
	}
	
}
