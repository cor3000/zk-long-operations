package zk.example.longoperations.api;

import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zk.ui.sys.DesktopCache;
import org.zkoss.zk.ui.sys.WebAppCtrl;

public abstract class UpdatableLongOperation<INPUT, UPDATE, RESULT> extends LongOperation<INPUT, RESULT>{

    private DesktopCache desktopCache;
    private String desktopId;

    public UpdatableLongOperation() {
		super();
	}

	public UpdatableLongOperation(boolean abortable) {
		super(abortable);
	}

	protected abstract void onUpdate(UPDATE update);

    @Override
    public void start(INPUT input) {
        this.desktopId = Executions.getCurrent().getDesktop().getId();
        this.desktopCache = ((WebAppCtrl) WebApps.getCurrent()).getDesktopCache(Sessions.getCurrent());
    	super.start(input);
    }
    
    protected final void update(UPDATE update) {
        try {
            Desktop desktop = getDesktop();
            Executions.activate(desktop);
            onUpdate(update);
            Executions.deactivate(desktop);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Desktop getDesktop() {
        return desktopCache.getDesktopIfAny(desktopId);
    }
}
