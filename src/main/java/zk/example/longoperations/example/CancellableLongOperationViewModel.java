package zk.example.longoperations.example;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.Command;
import org.zkoss.zk.ui.util.Clients;

import zk.example.longoperations.LongOperation;

public class CancellableLongOperationViewModel {

	private static final String IDLE = "idle";

	private LongOperation currentOperation;
	private String status = IDLE; 
	private boolean running = false;

	@Command
	public void startLongOperation() {
		currentOperation = new LongOperation() {
			private String result;

			@Override
			protected void execute() throws InterruptedException {
				step("Starting query (WHAT is the 'ANSWER';). This might take a about 7.5 million Years ...", 2000);
				step("Executing your query (1 million years passed) please wait...", 500);
				step("Executing your query (2 million years passed) please wait...", 500);
				step("Executing your query (3 million years passed) please wait...", 500);
				step("Executing your query (4 million years passed) please wait...", 500);
				step("Executing your query (5 million years passed) please wait...", 500);
				step("Executing your query (6 million years passed) please wait...", 500);
				step("Executing your query (7 million years passed) getting closer...", 500);
				step("Executing your query (7.1 million years passed) almost there...", 500);
				step("Executing your query (7.2 million years passed) just a bit more...", 500);
				step("Executing your query (7.4999 million years passed) now here it is...", 1000);
				result = "The answer is 42";
			}

			private void step(String message, int duration) throws InterruptedException {
				//check explicitly if the task was cancelled 
				// if cancelled it will throw an InterruptedException to stop the task
				checkCancelled(); 

				activate(); //would throw an InterruptedException if cancelled
				updateStatus(message);
				deactivate();

				Thread.sleep(duration); //will throw an InterruptedException if cancelled during sleep
			}

			@Override
			protected void onCancel() {
				Clients.showNotification("Now you'll never know... be more patient next time");
			}

			@Override
			protected void onFinish() {
				Clients.showNotification(result);
			}

			@Override
			protected void onCleanup() {
				updateStatus(IDLE);
				updateRunning(false);
				currentOperation = null;
			}
		};

		currentOperation.start();
		updateRunning(true);
	}

	@Command
	public void cancelOperation() {
		currentOperation.cancel();
	}

	private void updateRunning(boolean running) {
		this.running = running;
		BindUtils.postNotifyChange(null, null, this, "running");
	}

	private void updateStatus(String message) {
		status = message;
		BindUtils.postNotifyChange(null, null, this, "status");
	}

	public String getStatus() {
		return status;
	}

	public boolean isRunning() {
		return running;
	}
}
