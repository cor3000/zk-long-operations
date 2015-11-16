package zk.example.longoperationsj8.example;

import java.util.Arrays;
import java.util.List;

import org.zkoss.bind.annotation.Command;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.ListModelList;

public class SimpleLongJ8OperationViewModel {
	
	private ListModelList<String> resultModel = new ListModelList<String>();
	private LongOperation8<Void, List<String>> longOp;

	@Command
	public void startLongOperation() {
		longOp = new LongOperation8<Void, List<String>>()
				.onExecute((v) -> {return doExecute(longOp);})
				.onFinish(resultModel::addAll)
				.onCancel(this::doCancel)
				.onException(this::doError)
				.onCleanup(this::doCleanup);
		longOp.start((Void) null);

		Clients.showNotification("Result coming in 6 seconds, please wait! You can cancel the operation during the first 3 seconds.", null, null, null, 2500);
	}

	private List<String> doExecute(LongOperation8<Void, List<String>> longOp) throws InterruptedException {
		Thread.sleep(3000); //simulate a long backend operation
		if(Math.random() < 0.5) {
			throw new RuntimeException("error in operation");
		}
		longOp.doActivated(() -> Clients.showBusy("50% done"));
		Thread.sleep(3000); //simulate a long backend operation
		return Arrays.asList("aaa", "bbb", "ccc");
	}

	private void doCancel() {
		Clients.showNotification("Cancelled", Clients.NOTIFICATION_TYPE_WARNING, null, null, 1000);
	}

	private void doCleanup() {
		Clients.clearBusy(); 
		longOp = null;
	}

	private void doError(Throwable e) {
		Clients.showNotification("Error: " + e.getMessage(), Clients.NOTIFICATION_TYPE_ERROR, null, null, 1000);
	}

	@Command
	public void cancelLongOperation() {
		longOp.cancel();
	}
	
	public ListModelList<String> getResultModel() {
		return resultModel;
	}
}
