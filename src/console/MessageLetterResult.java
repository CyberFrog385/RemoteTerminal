package console;

import java.io.Serializable;

public class MessageLetterResult extends MessageResult implements Serializable {

	private static final long serialVersionUID = 1L;

	public MessageLetterResult( String errorMessage ) {

		super( Protocol.CMD_LETTER, errorMessage );
	}

	public MessageLetterResult() {

		super( Protocol.CMD_LETTER );
	}
}
