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
		os = new ObjectOutputStream( s.getOutputStream() );
		is = new ObjectInputStream( s.getInputStream());
		addr = s.getInetAddress();
		this.setDaemon(true);
	}

	static final String SENTINEL = "__console_done__";

	public void run() {
		try {
			ProcessBuilder builder = new ProcessBuilder();
		    builder.directory(location);
		    builder.command("sh", "-l");
		    builder.redirectErrorStream(true);

		    process = builder.start();

		    pos = process.getOutputStream();
	        pis = process.getInputStream();
	        pes = process.getErrorStream();

			Thread output = new Thread( new Runnable() {
				public void run() { sendExecRes(); }
			});
			output.setDaemon(true);
			output.start();

			while ( true ) {
				Message msg = ( Message ) is.readObject();
				switch ( msg.getID() ) {

					case Protocol.CMD_EXECUTE:
						execute((MessageExec) msg);
						break;

				}
			}
		} catch (IOException e) {
			System.err.print("Disconnect...");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			disconnect();
		}
	}

	void execute( MessageExec msg ) throws IOException {
		pos.write( ( msg.command + "\n" ).getBytes() );
		pos.write( ( "echo " + SENTINEL + "\n" ).getBytes() );
		pos.flush();
	}

	void sendExecRes() {
		try {
			BufferedReader reader = new BufferedReader( new InputStreamReader(pis) );
			String line;
			while ( ( line = reader.readLine() ) != null ) {
				synchronized ( os ) {
					if ( line.equals( SENTINEL ) )
						os.writeObject( new MessageExecResult() );
					else
						os.writeObject( new MessageExecOutput( line + "\n" ) );
				}
			}
		} catch (IOException e) {
		}
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

