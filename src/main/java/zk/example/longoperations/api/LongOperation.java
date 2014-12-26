package zk.example.longoperations.api;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;

public abstract class LongOperation<INPUT, RESULT> {

	private boolean abortable;
    private AtomicBoolean aborted;
	
    private RESULT result;
    private final String tempWorkingQueueName = "workingQueue" + UUID.randomUUID();

    public LongOperation() {}

    public LongOperation(boolean abortable) {
        this.abortable = abortable;
        if(abortable) {
        	aborted = new AtomicBoolean();
        }
    }

    public void abort() {
        if(!abortable) {
            throw new IllegalStateException("Long operation is not abortable");
        }
        aborted.set(true);
    }

    protected abstract RESULT execute(INPUT input);

    protected abstract void onFinish(RESULT result);

    public void start(final INPUT input) {
        final EventQueue<Event> eventQueue = EventQueues.lookup(tempWorkingQueueName);

        eventQueue.subscribe(new EventListener<Event>() {
	            @Override
	            public void onEvent(Event event) throws Exception {
	            	result = execute(input);
	            }
	        }, new EventListener<Event>() {
				@Override
	            public void onEvent(Event event) throws Exception {
            		finish();
	            }
	        });

        eventQueue.publish(new Event("start"));
    }

    /**
     * cleanup long operation here, consider overriding onFinish instead for your result processing
     * @param tempWorkingQueueName
     * @param event
     */
    protected void finish() {
    	onFinish(result);
    	EventQueues.remove(tempWorkingQueueName);
    }

    public boolean isAbortable() {
        return abortable;
    }

    public boolean isAborted() {
        if(!abortable) {
            throw new IllegalStateException("Long operation is not abortable");
        }
        return aborted.get();
    }

}
