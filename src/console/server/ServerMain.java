package console.server;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.Vector;
import java.io.*;

import console.*;

public class ServerMain {

	private static int MAX_USERS = 100;
	private static boolean stopFlag = false;

	public static boolean getStopFlag() {
		return stopFlag;
	}

	public static void setStopFlag( boolean value ) {
		stopFlag = value;
	}

	public static void main(String[] args) {

		try ( ServerSocket serv = new ServerSocket( Protocol.PORT  )) {
			System.err.println("initialized");
			ServerStopThread tester = new ServerStopThread();
			tester.start();
			while (true) {
				Socket sock = accept( serv );
				if ( sock != null ) {
//					if ( ServerMain.getNumUsers() < ServerMain.MAX_USERS )
					{
						System.err.println( sock.getInetAddress().getHostName() + " connected" );
						ServerThread server = new ServerThread(sock);
						server.start();
					}
//					else
					{
//						System.err.println( sock.getInetAddress().getHostName() + " connection rejected" );
//						sock.close();
					}
				}
//				if ( ServerMain.getStopFlag() ) {
//					break;
//				}
			}
		} catch (IOException e) {
			System.err.println(e);
		} finally {
//			stopAllUsers();
			System.err.println("stopped");
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
	}

	public static Socket accept( ServerSocket serv ) {
		assert( serv != null );
		try {
			serv.setSoTimeout( 1000 );
			Socket sock = serv.accept();
			return sock;
		} catch (SocketException e) {
		} catch (IOException e) {
		}
		return null;
	}
}

class ServerStopThread extends CommandThread {

	static final String cmd  = "q";
	static final String cmdL = "quit";

	Scanner fin;

	public ServerStopThread() {
		fin = new Scanner( System.in );
		ServerMain.setStopFlag( false );
		putHandler( cmd, cmdL, new CmdHandler() {
			@Override
			public boolean onCommand(int[] errorCode) {	return onCmdQuit(); }
		});
		this.setDaemon(true);
		System.err.println( "Enter \'" + cmd + "\' or \'" + cmdL + "\' to stop server\n" );
	}

	public void run() {

		while (true) {
			try {
				Thread.sleep( 1000 );
			} catch (InterruptedException e) {
				break;
			}
			if ( fin.hasNextLine()== false )
				continue;
			String str = fin.nextLine();
			if ( command( str )) {
				break;
			}
		}
	}

	public boolean onCmdQuit() {
		System.err.print("stop server...");
		fin.close();
		ServerMain.setStopFlag( true );
		return true;
	}
}

class ServerThread extends Thread {
	File location = new File("/");
	Process process;
	OutputStream pos;
	InputStream pis;
	InputStream pes;
    

	private Socket              sock;
	private ObjectOutputStream 	os;
	private ObjectInputStream 	is;
	private InetAddress 		addr;


	public ServerThread(Socket s) throws IOException {
		sock = s;
		s.setSoTimeout(1000);
		os = new ObjectOutputStream( s.getOutputStream() );
		is = new ObjectInputStream( s.getInputStream());
		addr = s.getInetAddress();
		this.setDaemon(true);
	}

	public void run() {
		try {
			ProcessBuilder builder = new ProcessBuilder();
		    builder.directory(location);
		    builder.command("sh", "-i", "-l");
		    
		    process = builder.start();

		    pos = process.getOutputStream();
	        pis = process.getInputStream();
	        pes = process.getErrorStream();
	        
			while ( true ) {
				Message msg = null;
				try {
					if (is.available() > 0)
						msg = ( Message ) is.readObject();
					if(pis.available() > 0)
						sendExecRes();
				} catch (IOException e) {
					e.printStackTrace();
				} 
					catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				if (msg != null) switch ( msg.getID() ) {

					case Protocol.CMD_EXECUTE:
						execute((MessageExec) msg);
						return;
					
				}
			}
		} catch (IOException e) {
			System.err.print("Disconnect...");
		} finally {
			disconnect();
		}
	}

	void execute( MessageExec msg ) throws IOException {
		boolean success = true;
		try(OutputStreamWriter writerS = new OutputStreamWriter(pos)) {
        	writerS.write(msg.command);
        }
		catch (IOException e) {
			success = false;
			os.writeObject( new MessageExecResult(e.toString()));
		}
		if(success)
			os.writeObject( new MessageExecResult());
	}

	public String convertStreamToString(InputStream is_to_convert) throws IOException  {
		 int bufferSize = 1024;
		 char[] buffer = new char[bufferSize];
		 StringBuilder out = new StringBuilder();
		 Reader in = new InputStreamReader(is_to_convert);
		 for (int numRead; (numRead = in.read(buffer, 0, buffer.length)) > 0; ) {
		     out.append(buffer, 0, numRead);
		 }
		 return out.toString();
	}
	
	void sendExecRes() throws IOException  {
		os.writeObject( new MessageExecOutput(convertStreamToString(pis)));
	}


	private boolean disconnected = false;
	public void disconnect() {
		if ( ! disconnected )
		try {
			System.err.println( addr.getHostName() + " disconnected" );
			pos.flush();
	        pos.close();
	        pis.close();
	        pes.close();
			os.close();
			is.close();
			sock.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			this.interrupt();
			disconnected = true;
		}
	}

}

