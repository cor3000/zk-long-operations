package zk.example.longoperations.example;

import java.util.Arrays;
import java.util.List;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.Command;
import org.zkoss.lang.Threads;
import org.zkoss.zul.ListModelList;

import zk.example.longoperations.api.LongOperation;
import zk.example.longoperations.api.UpdatableLongOperation;

public class UpdatingStatusLongOperationViewModel {

	ListModelList<String> resultModel = new ListModelList<String>();
	
	String status = "idle";
	
    @Command
    public void startLongOperation() {
        LongOperation<Integer, List<String>> longOperation = new UpdatableLongOperation<Integer, String, List<String>>() {
        	@Override
        	protected List<String> execute(Integer input) {
				step("Validating Parameters...", 10, 100 * input);
				step("Fetching Data ...", 40, 300 * input);
				step("Filtering Data...", 60, 350 * input);
				step("Updating Model...", 90, 250 * input);
				return Arrays.asList("aaa", "bbb", "ccc");
            }

            private void step(String message, int progress, int duration) {
            	update(progress+ "% - " + message);
            	Threads.sleep(duration);
			}

            @Override
            protected void onUpdate(String update) {
            	updateStatus(update);
            }
            
            @Override
            protected void onFinish(List<String> result) {
            	resultModel.addAll(result);
            	updateStatus("idle");
            }
        };
        longOperation.start(5);
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