package zk.example.longoperationsj8.example;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.zkoss.bind.annotation.Command;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.ListModelList;

import zk.example.longoperations.java8.LongOperation;
import zk.example.longoperations.java8.LongOperationBuilder;
import zk.example.longoperations.java8.LongOperations;

public class SimpleLongJ8OperationViewModel {
	
	private ListModelList<String> resultModel = new ListModelList<String>();
	private LongOperation<Integer, List<String>> longOp;

	@Command
	public void startLongOperation() {
		longOp = LongOperations.of((Integer numRows) -> {return doExecute(numRows, longOp);})
				.onFinish(resultModel::addAll)
				.onCancel(this::doCancel)
				.onException(this::doError)
				.onCleanup(this::doCleanup).build();
		longOp.start(5);
		
		Clients.showNotification("Result coming in 6 seconds, please wait! You can cancel the operation during the first 3 seconds.", null, null, null, 2500);
	}

	private List<String> doExecute(Integer numRows, LongOperation<?, ?> longOp) throws InterruptedException {
		
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
		LongOperationBuilder<Integer, String> builder;
		builder = LongOperations.of(this::findPerson).onFinish(System.out::println);

		builder.start(300);
		builder.start(1300);
		builder.start(3300);
		builder.start(5000);
	}
	
	private String findPerson(int id) throws InterruptedException {
		Thread.sleep(id);
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
