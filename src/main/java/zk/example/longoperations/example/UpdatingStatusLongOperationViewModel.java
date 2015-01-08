package zk.example.longoperations.example;

import java.util.Arrays;
import java.util.List;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.Command;
import org.zkoss.zul.ListModelList;

import zk.example.longoperations.LongOperation;

public class UpdatingStatusLongOperationViewModel {

	private static final String IDLE = "idle";
	private String status = IDLE;

	private ListModelList<String> resultModel = new ListModelList<String>();

	@Command
	public void startLongOperation() {
		LongOperation longOperation = new LongOperation() {
			private List<String> result;

			@Override
			protected void execute() throws InterruptedException {
				step("Validating Parameters...", 10, 500);
				step("Fetching Data ...", 40, 1500);
				step("Filtering Data...", 60, 1750);
				step("Updating Model...", 90, 750);
				result = Arrays.asList("aaa", "bbb", "ccc");
			}

			private void step(String message, int progress, int duration) throws InterruptedException {
				activate();
				updateStatus(progress+ "% - " + message);
				deactivate();
				Thread.sleep(duration); //simulate processing time for the current step
			}

			@Override
			protected void onFinish() {
				resultModel.addAll(result);
				updateStatus(IDLE);
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