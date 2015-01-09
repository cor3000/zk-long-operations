package zk.example.longoperations;

public abstract class ResultLongOperation<RESULT> extends LongOperation {

	private RESULT result;

	protected abstract RESULT getResult() throws InterruptedException;

	protected abstract void onResult(RESULT result);
	public void setResult(RESULT result) {
		this.result = result;
	}

	@Override
	protected final void execute() throws InterruptedException {
		result = getResult();
	}


	@Override
	protected final void onFinish() {
		onResult(result);
	}
}
