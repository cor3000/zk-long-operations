package zk.example.longoperations.example;

import java.util.List;

import org.zkoss.bind.annotation.Command;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.ListModelList;

import zk.example.longoperations.LongOperation;

public class DataFilterLongOperationViewModel {

	private ListModelList<String> resultModel = new ListModelList<String>();
	private int filterParameter = 3;

	@Command
	public void startLongOperation() {
		LongOperation longOperation = new DataFilterLongOperation(filterParameter) {
			@Override
			protected void onResult(List<String> result) {
				resultModel.clear();
				resultModel.addAll(result);
			}

			@Override
			protected void onCleanup() {
				Clients.clearBusy();
			}
		};
		Clients.showBusy("started with parameter: " + filterParameter);
		longOperation.start();
	}

	public int getFilterParameter() {
		return filterParameter;
	}

	public void setFilterParameter(int filterParameter) {
		this.filterParameter = filterParameter;
	}

	public ListModelList<String> getResultModel() {
		return resultModel;
	}
}
