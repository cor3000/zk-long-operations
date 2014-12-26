package zk.example.longoperations.example;

import java.util.Arrays;
import java.util.List;

import org.zkoss.bind.annotation.Command;
import org.zkoss.lang.Threads;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.ListModelList;

import zk.example.longoperations.api.LongOperation;

public class SimpleLongOperationViewModel {

	ListModelList<String> resultModel = new ListModelList<String>();
	
    @Command
    public void startLongOperation() {
        LongOperation<Integer, List<String>> longOperation = new LongOperation<Integer, List<String>>() {
        	@Override
        	protected List<String> execute(Integer input) {
        		Threads.sleep(input * 1000);
				return Arrays.asList("aaa", "bbb", "ccc");
            }
            
            @Override
            protected void onFinish(List<String> result) {
            	Clients.clearBusy();
            	resultModel.addAll(result);
            }
        };
        Clients.showBusy("Result coming in 3 seconds, please wait!");
        longOperation.start(3);
    }

	public ListModelList<String> getResultModel() {
		return resultModel;
	}
}