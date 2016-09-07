import java.io.Serializable;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

class Message implements Serializable{
	
	int msgId;//to determine type of message
	String clientIP;	//clients IP
	String msg;			//msgs exchanged
	int clientPort;		//client port
	String userName;	//user name to set time
	String password;	//password to set time
	long time;			//time to be set
	int hopCount;		//total number of hop count
	String finalDate;	//date in calender format
	long startTime;		//message send time
	String proxyNames;
	public Message(){}
	
	/*
	 * set the message attributes*/
	public void setMessageValues(int msgid, String clientAdd,int clientP,String uName,String pwd,long t,String m){
		
		msgId = msgid;
		clientIP = clientAdd;	
		msg = m;
		clientPort = clientP;		
		userName = uName;
		password = pwd;		
		time = t;
		proxyNames="";		
	}
	/*
	 * set start time to calculate delay*/
	public void setStartTime(long t){
		startTime = t;
	}
	
	public long getStartTime(){
		return startTime;
	}
	
	public String getProxyNames(){
		return proxyNames;
	}
	public void setProxyNames(String s){
		proxyNames=proxyNames+s;
	}
	
	/*
	 * Return message id*/
	
	public int getmsgId(){
		return msgId;
	}
	
	public void setMsgId(int id){
		msgId = id;
		
	}
	public void setMessage(String m){
		msg = m;
	}
	
	/*
	 * Return ip address of the client machine*/
	public String getClientIp(){
		return clientIP;
	}	
	/*
	 * Return the msg string*/
	public String getMsg(){
		return msg;
	}
	/*
	 * Return port of the client machine*/
	public int getClientPort(){
		return clientPort;
	}
	
		/*
	 * Return the userName string*/
	public String getuserName(){
		return userName;
	}
	
	/*
	 * Return the userName string*/
	public String getPassword(){
		return password;
	}
	
	/*
	 * Return time*/
	public long getTime(){
		return time;
	}
	/**
	 * return hop count*/
	public int getHop(){
		return hopCount;
	}
	/**
	 * *function to set the hop count
	 * @param h
	 */
	public void setHopCount(int h){
		hopCount = h;
	}
	/*format time to calender format
	 * */
	public void formatDate(){
		Date date = new Date(time);
		Format format = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
		finalDate = format.format(date);
		System.out.println("Time in calender format "+ finalDate);
	}
	
	public void PrintMessageValues(){
		//System.out.println("I am here");
		
		System.out.println("msgId::"+msgId+"\nclientIP::"+clientIP+"\nmsg::"+msg+"\nclientPort::"+clientPort+"\nuserName::"+userName+"\npassword::"+password+"\ntime::"+time+"\n");
	}
	
}