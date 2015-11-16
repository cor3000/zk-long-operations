package zk.example.longoperationsj8;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.DesktopUnavailableException;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.sys.DesktopCtrl;

public class LongOperation8<T, R> {
	Logger LOG = LoggerFactory.getLogger(LongOperation8.class);
	
	private UUID taskId = UUID.randomUUID();
	private Thread operationThread;
	private WeakReference<Desktop> desktopRef;
	private AtomicBoolean cancelled = new AtomicBoolean(false);
	
	private InterruptibleFunction<T, R> operation;
	private Optional<Consumer<R>> onFinish = Optional.empty();
	private Optional<Consumer<Throwable>> onException = Optional.empty();
	private Optional<Runnable> onCancel = Optional.empty();
	private Optional<Runnable> onCleanup = Optional.empty();
	
	public LongOperation8<T, R> onExecute(InterruptibleFunction<T, R> operation) {
		this.operation = operation;
		return this;
	}

	public LongOperation8<T, R> onFinish(Consumer<R> onFinish) {
		this.onFinish = Optional.ofNullable(onFinish);
		return this;
	}

	public LongOperation8<T, R> onCancel(Runnable onCancel) {
		this.onCancel = Optional.ofNullable(onCancel);
		return this;
	}

	public LongOperation8<T, R> onCleanup(Runnable onCleanup) {
		this.onCleanup = Optional.ofNullable(onCleanup);
		return this;
	}

	public LongOperation8<T, R> onException(Consumer<Throwable> onException) {
		this.onException = Optional.ofNullable(onException);
		return this;
	}

	public void start(T input) {
		this.desktopRef = new WeakReference<Desktop>(Executions.getCurrent().getDesktop());

		enableServerPushForThisTask();
		operationThread = new Thread(() -> {run(input);});
		operationThread.start();
	}

	public void cancel() {
		if(cancelled.compareAndSet(false, true)) {
			operationThread.interrupt();
		}
	}

	/**
	 * Checks if the task thread has been interrupted. Use this to check whether or not to exit a busy operation in case.  
	 * @throws InterruptedException when the current task has been cancelled/interrupted
	 */
	protected final void checkCancelled() throws InterruptedException {
		if(Thread.currentThread() != this.operationThread) {
			throw new IllegalStateException("this method can only be called in the worker thread (i.e. during execute)");
		}
		if(Thread.interrupted() || cancelled.get()) {
			cancelled.set(true);
			throw new InterruptedException();
		}
	}
	
	private void run(T input) {
		List<Runnable> finalizeTasks = new ArrayList<>();
		try{
			try {
				checkCancelled();
				R result = operation.applyInterruptible(input);
				checkCancelled();
				onFinish.ifPresent((handler) -> finalizeTasks.add(() -> handler.accept(result)));
			} catch (InterruptedException e) {
				cancelled.set(true);
				onCancel.ifPresent(finalizeTasks::add);
			} catch (DesktopUnavailableException e) {
				throw e;
			} catch (Exception e) {
				onException.orElseThrow(() -> e);
				finalizeTasks.add(() -> onException.get().accept(e));
			} finally {
				onCleanup.ifPresent(finalizeTasks::add);
				doActivated(() -> finalizeTasks.forEach(Runnable::run));
				disableServerPushForThisTask();
			}
		} catch (DesktopUnavailableException e) {
			LOG.warn("Unable to finalize Long Operation: Desktop unavailable.",  e);
		} catch (Exception e) {
			LOG.error("Unexpected Exception during Long Operation.",  e);
		}
	}
		
	public void doActivated(Runnable task) throws DesktopUnavailableException, InterruptedException {
		try {
			Executions.activate(getDesktop()); 
			task.run();
		} catch (InterruptedRuntimeException e) {
			throw (InterruptedException)e.getCause();
		} finally {
			Executions.deactivate(getDesktop());
		}
	}
	
	private void enableServerPushForThisTask() {
		((DesktopCtrl)getDesktop()).enableServerPush(true, taskId);
	}

	private void disableServerPushForThisTask() {
		((DesktopCtrl)getDesktop()).enableServerPush(false, taskId);
	}

	private Desktop getDesktop() {
		return Optional.ofNullable(desktopRef.get()).orElseThrow(DesktopUnavailableException::new);
	}

	public static interface InterruptibleFunction<A, B> extends Function<A, B> {
		default B apply(A input) {
			try {
				return applyInterruptible(input);
			} catch (InterruptedException e) {
				throw new InterruptedRuntimeException(e);
			}
		}

		B applyInterruptible(A input) throws InterruptedException; 
	}
	
	static class InterruptedRuntimeException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		public InterruptedRuntimeException(InterruptedException e) {
			super(e);
		}
	}

}
