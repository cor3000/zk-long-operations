package zk.example.longoperations.example;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.lang.Threads;
import org.zkoss.zk.ui.util.Clients;

import zk.example.longoperations.api.AbortedException;
import zk.example.longoperations.api.LongOperation;
import zk.example.longoperations.api.UpdatableLongOperation;

public class AbortableLongOperationViewModel {

	private static final String IDLE = "idle";
	
	private LongOperation<String, String> currentOperation;
    private String status = IDLE; 
	
    @Command
    public void startLongOperation(@BindingParam("abortable") final boolean abortable) {
    	
        currentOperation = new UpdatableLongOperation<String, String, String>(abortable) {
        	protected String execute(String query) {
            	try {
					step("Starting query (" + query + "). This might take a about 7.5 million Years ...", 2000);
					step("Executing your query (1 million years passed) please wait...", 500);
					step("Executing your query (2 million years passed) please wait...", 500);
					step("Executing your query (3 million years passed) please wait...", 500);
					step("Executing your query (4 million years passed) please wait...", 500);
					step("Executing your query (5 million years passed) please wait...", 500);
					step("Executing your query (6 million years passed) please wait...", 500);
					step("Executing your query (7 million years passed) getting closer...", 500);
					step("Executing your query (7.1 million years passed) almost there...", 500);
					step("Executing your query (7.2 million years passed) just a bit more...", 500);
					step("Executing your query (7.4999 million years passed) now here it is...", 1000);
				} catch (AbortedException e) {
					return "Now you'll never know...";
				}
            	return "The answer is 42";
            }

            private void step(String message, int duration) throws AbortedException {
            	checkAborted();
            	update(message);
            	Threads.sleep(duration);
			}

			private void checkAborted() throws AbortedException {
				if(isAbortable() && isAborted()) {
					throw new AbortedException();
				}
			}

			@Override
           	protected void onUpdate(String statusMessage) {
				updateStatus(statusMessage);
            }
            
            @Override
            protected void onFinish(String result) {
            	updateStatus(IDLE);
            	Clients.showNotification(result);
            	currentOperation = null;
            	BindUtils.postNotifyChange(null, null, AbortableLongOperationViewModel.this, "runningAbortable");
            }

        };

        currentOperation.start("WHAT is the 'ANSWER';");
        BindUtils.postNotifyChange(null, null, this, "runningAbortable");
    }

    @Command
    public void abortOperation() {
    	if(currentOperation != null && currentOperation.isAbortable()) {
    		currentOperation.abort();
    	}
    }
    
    private void updateStatus(String message) {
    	status = message;
    	BindUtils.postNotifyChange(null, null, this, "status");
    }
    
    public boolean isRunningAbortable() {
    	return currentOperation != null && currentOperation.isAbortable() && !currentOperation.isAborted(); 
    }
    
	public String getStatus() {
		return status;
	}
}
