package zk.example.longoperations.example;

import java.util.Arrays;
import java.util.List;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.Command;
import org.zkoss.zul.ListModelList;

import zk.example.longoperations.api.LongOperation;

public class UpdatingStatusLongOperationViewModel {

	ListModelList<String> resultModel = new ListModelList<String>();
	
	String status = "idle";
	
    @Command
    public void startLongOperation() {
    	
    	final int input = 5;
    	
        LongOperation longOperation = new LongOperation() {
        	private List<String> result;

			@Override
        	protected void execute() throws InterruptedException {
				step("Validating Parameters...", 10, 100 * input);
				step("Fetching Data ...", 40, 300 * input);
				step("Filtering Data...", 60, 350 * input);
				step("Updating Model...", 90, 250 * input);
				result = Arrays.asList("aaa", "bbb", "ccc");
            }

            private void step(String message, int progress, int duration) throws InterruptedException {
            	activate();
            	updateStatus(progress+ "% - " + message);
            	deactivate();
            	Thread.sleep(duration);
			}

            @Override
            protected void onFinish() {
            	resultModel.addAll(result);
            	updateStatus("idle");
            }
        };
        longOperation.start();
    }

    private void updateStatus(String update) {
    	status = update;
    	BindUtils.postNotifyChange(null, null, UpdatingStatusLongOperationViewModel.this, "status");
    }

	public ListModelList<String> getResultModel() {
		return resultModel;
	}

	public String getStatus() {
		return status;
	}
}