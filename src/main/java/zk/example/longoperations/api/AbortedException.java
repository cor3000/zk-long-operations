package zk.example.longoperations.api;

public class AbortedException extends Exception {
	private static final long serialVersionUID = 1L;

	public AbortedException() {
		super();
	}
	
	public AbortedException(String message) {
		super(message);
	}

	public AbortedException(Throwable cause) {
		super(cause);
	}
	
	public AbortedException(String message, Throwable cause) {
		super(message, cause);
	}
}
