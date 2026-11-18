package console.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.TreeMap;

import console.*;


public class ClientMain {
	public static void main(String[] args)  {
		if (args.length > 1) {
			System.err.println(	"Invalid number of arguments\n" + "Use: [host]" );
			waitKeyToStop();
			return;
		}

		String host = (args.length == 0 ? "localhost" : args[0]);

		try ( Socket sock = new Socket( InetAddress.getByName(host), Protocol.PORT ) ) {
			System.err.println("initialized");
			session(sock);
		} catch ( Exception e) {
			System.err.println(e);
		} finally {
			System.err.println("bye...");
		}
	}

	static void waitKeyToStop() {
		System.err.println("Press a key to stop...");
		try {
			System.in.read();
		} catch (IOException e) {
		}
	}

	static class Session {
		boolean connected = false;
		Session() {
		}
	}
	static void session(Socket s) {
		try ( Scanner in = new Scanner(System.in);
			  ObjectInputStream is = new ObjectInputStream(s.getInputStream());
			  ObjectOutputStream os = new ObjectOutputStream(s.getOutputStream())) {
			os.flush();
			Session ses = new Session();
			if ( openSession( ses, is, os, in )) {
				try {
					while (true) {
						Message msg = getCommand(ses, in);
						if (! processCommand(ses, msg, is, os)) {
							break;
						}
					}
				} finally {
					closeSession(ses, os);
				}
			}
		} catch ( Exception e) {
			System.err.println(e);
		}
	}

	static boolean openSession(Session ses, ObjectInputStream is, ObjectOutputStream os, Scanner in)
			throws IOException, ClassNotFoundException {
		System.err.println("Press <Enter> to continue...");
		if( in.hasNextLine())
			in.nextLine();
		ses.connected = true;
		return true;
	}

	static void closeSession(Session ses, ObjectOutputStream os) throws IOException {
		
		if ( ses.connected ) {
			ses.connected = false;
		}
	}

	static Message getCommand(Session ses, Scanner in) {
		while (true) {
			printPrompt();
			if (in.hasNextLine()== false)
				break;
			String str = in.nextLine();
			if ( translateCmd(str) == -1 )
				return null;
			if ( str.trim().length() > 0 )
				return new MessageExec(str);
		}
		return null;
	}


	static TreeMap<String,Byte> commands = new TreeMap<String,Byte>();
	static {
		commands.put("q", new Byte((byte) -1));
		commands.put("quit", new Byte((byte) -1));
		commands.put("exit", new Byte((byte) -1));
	}

	static byte translateCmd(String str) {
		str = str.trim();
		Byte r = commands.get(str);
		return (r == null ? 0 : r.byteValue());
	}

	static void printPrompt() {
		System.out.println();
		System.out.print("(q)uit / commands > ");
		System.out.flush();
	}

	static boolean processCommand(Session ses, Message msg,
			                      ObjectInputStream is, ObjectOutputStream os)
            throws IOException, ClassNotFoundException {
		if ( msg != null )
		{
			os.writeObject(msg);
			os.flush();
			while (true) {
				Message res = (Message) is.readObject();
				if (res.getID() == Protocol.CMD_EXUCUTE_OUTPUT) {
					printExec((MessageExecOutput) res);
					continue;
				}
				MessageResult result = (MessageResult) res;
				if ( result.Error()) {
					System.err.println(result.getErrorMessage());
				}
				return true;
			}
		}
		return false;
	}

	static void printExec(MessageExecOutput m) {
		System.out.print(m.txt);
		System.out.flush();
	}
}
