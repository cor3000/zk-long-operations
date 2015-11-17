package zk.example.longoperations.java8;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.DesktopUnavailableException;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.sys.DesktopCtrl;

public class LongOperation<T, R> {
	Logger LOG = LoggerFactory.getLogger(LongOperation.class);
	
	private UUID taskId = UUID.randomUUID();
	private Thread operationThread;
	private WeakReference<Desktop> desktopRef;
	private AtomicBoolean started = new AtomicBoolean(false);
	private AtomicBoolean cancelled = new AtomicBoolean(false);
	
	private InterruptibleFunction<T, R> operation;
	private Optional<Consumer<R>> onFinish = Optional.empty();
	private Optional<Consumer<Throwable>> onException = Optional.empty();
	private Optional<Runnable> onCancel = Optional.empty();
	private Optional<Runnable> onCleanup = Optional.empty();
	
	LongOperation<T, R> onExecute(InterruptibleFunction<T, R> operation) {
		this.operation = operation;
		return this;
	}

	LongOperation<T, R> onFinish(Consumer<R> onFinish) {
		this.onFinish = Optional.ofNullable(onFinish);
		return this;
	}

	LongOperation<T, R> onCancel(Runnable onCancel) {
		this.onCancel = Optional.ofNullable(onCancel);
		return this;
	}

	LongOperation<T, R> onCleanup(Runnable onCleanup) {
		this.onCleanup = Optional.ofNullable(onCleanup);
		return this;
	}

	LongOperation<T, R> onException(Consumer<Throwable> onException) {
		this.onException = Optional.ofNullable(onException);
		return this;
	}

	public void start(T input) {
		if(!started.compareAndSet(false, true)) {
			throw new IllegalStateException("Long operation already started");
		}
		
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
		} finally {
			this.operationThread = null;
			this.operation = null;
			this.onException = null;
			this.onCleanup = null;
			this.onCancel = null;
			this.onFinish = null;
			this.desktopRef = null;
			this.taskId = null;
		}
	}
		
	public void doActivated(Runnable task) throws DesktopUnavailableException, InterruptedException {
		try {
			Executions.activate(getDesktop()); 
			task.run();
		} catch (RuntimeInterruptedException e) {
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
}
