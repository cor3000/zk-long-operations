package zk.example.longoperationsj8.example;

import java.util.Arrays;
import java.util.List;

import org.zkoss.bind.annotation.Command;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.ListModelList;

public class SimpleLongJ8OperationViewModel {
	
	private ListModelList<String> resultModel = new ListModelList<String>();

	@Command
	public void startLongOperation() {
		
		LongOperation8<Void, List<String>> longOp = new LongOperation8<Void, List<String>>();
		
		longOp.doOperation((v) -> {
					Thread.sleep(3000); //simulate a long backend operation
					longOp.doActivated(() -> Clients.showBusy("50% done"));
					Thread.sleep(3000); //simulate a long backend operation
					return Arrays.asList("aaa", "bbb", "ccc");
				})
				.onFinish((result) -> {resultModel.addAll(result);})
				.onCleanup(Clients::clearBusy)
				.onException(Throwable::printStackTrace);
		longOp.start((Void) null);
		
		Clients.showBusy("Result coming in 3 seconds, please wait!");
	}

	
	
	public ListModelList<String> getResultModel() {
		return resultModel;
	}
}
