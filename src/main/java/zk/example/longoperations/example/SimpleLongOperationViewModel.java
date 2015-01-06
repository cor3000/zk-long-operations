package zk.example.longoperations.example;

import java.util.Arrays;
import java.util.List;

import org.zkoss.bind.annotation.Command;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.ListModelList;

import zk.example.longoperations.api.LongOperation;

public class SimpleLongOperationViewModel {

	private ListModelList<String> resultModel = new ListModelList<String>();
	private List<String> result;
	
    @Command
    public void startLongOperation() {
    	Clients.showBusy("Result coming in 3 seconds, please wait!");
        new LongOperation() {
        	
        	@Override
        	protected void execute() throws InterruptedException {
        		Thread.sleep(3000);
				result = Arrays.asList("aaa", "bbb", "ccc");
            }
            
        	protected void onFinish() {
        		resultModel.addAll(result);
        	};
        	
            @Override
            protected void onCleanup() {
            	Clients.clearBusy();
            }
        }.start();
    }

	public ListModelList<String> getResultModel() {
		return resultModel;
	}
}