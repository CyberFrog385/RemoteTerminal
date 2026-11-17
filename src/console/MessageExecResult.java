package console;

import java.io.Serializable;

public class MessageExecResult extends MessageResult implements Serializable {

	private static final long serialVersionUID = 1L;

	public int exitCode;
	public String stdout;
	public String stderr;

	public MessageExecResult( String errorMessage ) {
		super( Protocol.CMD_EXECUTE, errorMessage );
	}

	public MessageExecResult( int exitCode, String stdout, String stderr ) {
		super( Protocol.CMD_EXECUTE );
		this.exitCode = exitCode;
		this.stdout = stdout;
		this.stderr = stderr;
	}
}
