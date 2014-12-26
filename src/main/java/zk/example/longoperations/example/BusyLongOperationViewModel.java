package zk.example.longoperations.example;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.bind.annotation.Command;
import org.zkoss.lang.Threads;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.ListModelList;

import zk.example.longoperations.api.BusyLongOperation;
import zk.example.longoperations.api.LongOperation;

public class BusyLongOperationViewModel {

	private ListModelList<String> resultModel = new ListModelList<String>();
	
    @Command
    public void startLongOperation() {
    	Clients.showBusy("Starting Operation...");
    	LongOperation<Integer, List<String>> longOperation = new BusyLongOperation<Integer, List<String>>() {
			@Override
			protected List<String> execute(Integer numberOfItems) {
				List<String> result = new ArrayList<String>();
				for(int itemNo = 1; itemNo <= numberOfItems; itemNo++) {
					showBusy("Collecting Item " + itemNo);
					Threads.sleep(1000);
					result.add("Item " + itemNo);
				}
				return result;
			}
			
			@Override
			protected void onFinish(List<String> result) {
				resultModel.addAll(result);
			}
		};
        resultModel.clear();
        longOperation.start(5);
    }

	public ListModelList<String> getResultModel() {
		return resultModel;
	}
}
