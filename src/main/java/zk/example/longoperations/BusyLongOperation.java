package zk.example.longoperations;

import org.zkoss.zk.ui.util.Clients;


public abstract class BusyLongOperation extends LongOperation {

	protected void showBusy(String busyMessage) throws InterruptedException {
		activate();
		Clients.showBusy(busyMessage);
		deactivate();
	}

	@Override
	protected void onCleanup() {
		Clients.clearBusy();
	}
}