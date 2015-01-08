package zk.example.longoperations.example;

import java.util.Arrays;
import java.util.List;

import org.zkoss.bind.annotation.Command;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.ListModelList;

import zk.example.longoperations.LongOperation;

public class SimpleLongOperationViewModel {
	private ListModelList<String> resultModel = new ListModelList<String>();

	@Command
	public void startLongOperation() {
		LongOperation longOperation = new LongOperation() {
			private List<String> result;

			@Override
			protected void execute() throws InterruptedException {
				Thread.sleep(3000); //simulate a long backend operation
				result = Arrays.asList("aaa", "bbb", "ccc");
			}

			protected void onFinish() {
				resultModel.addAll(result);
			};

			@Override
			protected void onCleanup() {
				Clients.clearBusy();
			}
		};

		Clients.showBusy("Result coming in 3 seconds, please wait!");
		longOperation.start();
	}

	public ListModelList<String> getResultModel() {
		return resultModel;
	}
}