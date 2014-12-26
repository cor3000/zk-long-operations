package zk.example.longoperations.api;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;

public abstract class LongOperation<INPUT, RESULT> {

	private static final String START_LONG_OP = "startLongOp";
	private static final String FINISH_LONG_OP = "finishLongOp";
	private static final String CLEANUP_LONG_OP_1 = "cleanupLongOp1";
	private static final String CLEANUP_LONG_OP_2 = "cleanupLongOp2";
	private boolean abortable;
    private AtomicBoolean aborted;

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
        final String tempWorkingQueueName = "workingQueue" + UUID.randomUUID();
        final EventQueue<Event> eventQueue = EventQueues.lookup(tempWorkingQueueName);

        eventQueue.subscribe(new EventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
            	if(START_LONG_OP.equals(event.getName())) {
            		RESULT result = execute(input);
            		eventQueue.publish(new Event(FINISH_LONG_OP, null, result));
            	} else if(CLEANUP_LONG_OP_1.equals(event.getName())) {
            		eventQueue.publish(new Event(CLEANUP_LONG_OP_2));
             	}
            }
        }, true); 
        
        eventQueue.subscribe(new EventListener<Event>() {
			@Override
            public void onEvent(Event event) throws Exception {
            	if(FINISH_LONG_OP.equals(event.getName())) {
            		finish(event);
            		eventQueue.publish(new Event(CLEANUP_LONG_OP_1));
            	}
            	if(CLEANUP_LONG_OP_2.equals(event.getName())) {
            		EventQueues.remove(tempWorkingQueueName);
             	}
            }
        });

        eventQueue.publish(new Event(START_LONG_OP, null, input));
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

    /**
     * cleanup long operation, consider overriding onFinish instead for your result processing
     * @param tempWorkingQueueName
     * @param event
     */
    protected void finish(Event event) {
    	@SuppressWarnings("unchecked")
    	RESULT result = (RESULT)event.getData();
    	onFinish(result);
    }
}
