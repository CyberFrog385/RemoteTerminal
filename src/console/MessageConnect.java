package console;

import java.io.Serializable;


public class MessageConnect extends Message implements Serializable {

	private static final long serialVersionUID = 1L;

	public MessageConnect() {
		super( Protocol.CMD_CONNECT );
	}

}
