package zk.example.longoperations.example;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.bind.annotation.Command;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.ListModelList;

import zk.example.longoperations.BusyLongOperation;
import zk.example.longoperations.LongOperation;

public class BusyLongOperationViewModel {

	private ListModelList<String> resultModel = new ListModelList<String>();

	@Command
	public void startLongOperation() {
		resultModel.clear();
		final int numberOfItems = 5;
		LongOperation longOperation = new BusyLongOperation() {
			private List<String> result;
			@Override
			protected void execute() throws InterruptedException {
				List<String> result = new ArrayList<String>();
				for(int itemNo = 1; itemNo <= numberOfItems; itemNo++) {
					showBusy("Collecting Item " + itemNo);
					Thread.sleep(1000);
					result.add("Item " + itemNo);
				}
				this.result = result;
			}

			@Override
			protected void onFinish() {
				resultModel.addAll(result);
			}
		};

		Clients.showBusy("Starting Operation...");
		longOperation.start();
	}

	public ListModelList<String> getResultModel() {
		return resultModel;
	}
}
