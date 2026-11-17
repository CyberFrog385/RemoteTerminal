package console;

import java.io.Serializable;

public class MessageExecOutput extends Message implements Serializable {

	private static final long serialVersionUID = 1L;

	public String txt;

	public MessageExecOutput( String txt ) {

		super( Protocol.CMD_EXUCUTE_OUTPUT );
		this.txt = txt;
	}

}
