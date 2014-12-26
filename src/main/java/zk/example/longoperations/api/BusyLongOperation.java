package zk.example.longoperations.api;

import org.zkoss.zk.ui.util.Clients;


public abstract class BusyLongOperation<INPUT, RESULT> extends UpdatableLongOperation<INPUT, String, RESULT> {

	protected void showBusy(String busyMessage) {
		update(busyMessage);
	}
	
	@Override
	protected void onUpdate(String busyMessage) {
		Clients.showBusy(busyMessage);
	}

	@Override
	protected void finish() {
		Clients.clearBusy();
		super.finish();
	}
}