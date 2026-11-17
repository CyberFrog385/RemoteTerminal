package console;

import java.io.Serializable;

public class MessageExecResult extends MessageResult implements Serializable {

	private static final long serialVersionUID = 1L;

	public MessageExecResult( String errorMessage ) {
		super( Protocol.CMD_EXECUTE, errorMessage );
	}
	public MessageExecResult() {
		super(Protocol.CMD_EXECUTE);
	}

}
