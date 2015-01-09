package zk.example.longoperations.example;

import java.util.Arrays;
import java.util.List;

import zk.example.longoperations.ResultLongOperation;

public abstract class DataFilterLongOperation extends ResultLongOperation<List<String>> {

	private int filterParameter;

	public DataFilterLongOperation(int filterParameter) {
		super();
		this.filterParameter = filterParameter;
	}

	@Override
	protected List<String> getResult() throws InterruptedException {
		//call your DB to retrieve lots of data, affected by filterParameter
		Thread.sleep(1000 * filterParameter);
		return Arrays.asList("data that took", filterParameter + " seconds", " to filter");
	}
}
