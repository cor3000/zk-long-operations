package zk.example.longoperationsj8.example;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.zkoss.bind.annotation.Command;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.ListModelList;

import zk.example.longoperationsj8.LongOperation8;
import zk.example.longoperationsj8.LongOperations;
import zk.example.longoperationsj8.LongOperations.LongOperationsBuilder;

public class SimpleLongJ8OperationViewModel {
	
	private ListModelList<String> resultModel = new ListModelList<String>();
	private LongOperation8<Integer, List<String>> longOp;

	@Command
	public void startLongOperation() {
		longOp = new LongOperation8<Integer, List<String>>()
				.onExecute((numRows) -> {return doExecute(numRows, longOp);})
				.onFinish(resultModel::addAll)
				.onCancel(this::doCancel)
				.onException(this::doError)
				.onCleanup(this::doCleanup);
		longOp.start(5);
		
		Clients.showNotification("Result coming in 6 seconds, please wait! You can cancel the operation during the first 3 seconds.", null, null, null, 2500);
	}

	private List<String> doExecute(Integer numRows, LongOperation8<?, ?> longOp) throws InterruptedException {
		
		Thread.sleep(3000); //simulate a long backend operation
		if(Math.random() < 0.5) {
			throw new RuntimeException("error in operation");
		}
		longOp.doActivated(() -> Clients.showBusy("50% done"));
		Thread.sleep(3000); //simulate a long backend operation
		return Stream.iterate(0, (i -> ++i)).limit(numRows)
				.map((i -> "Result " + i))
				.collect(Collectors.toList());

	}

	@Command
	public void startFindPerson() {
//		LongOperations.of(this::findPerson, Clients::showNotification).start((int)(Math.random() * 100));
//		LongOperation8<Integer, String> start = LongOperations.of(this::findPerson).onFinish(Clients::showNotification)
//			.start(300);
		LongOperationsBuilder<Integer, String> myOp = LongOperations.of(this::findPerson).onFinish(System.out::println);
		myOp.start(300);
		myOp.start(50000);
	}
	
	private String findPerson(int id) throws InterruptedException {
		Thread.sleep(1000);
		return "Peter (" + id + ")";
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
