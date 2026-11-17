package console;

import java.io.Serializable;


public class MessageExec  extends Message implements Serializable {

	private static final long serialVersionUID = 1L;

	public String command;

	public MessageExec( String command ) {
		super( Protocol.CMD_EXECUTE );
		this.command = command;
	}
}
