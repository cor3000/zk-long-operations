package zk.example.longoperations.java8;

class RuntimeInterruptedException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	public RuntimeInterruptedException(InterruptedException e) {
		super(e);
	}
}