package zk.example.longoperationsj8.example;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.DesktopUnavailableException;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.sys.DesktopCtrl;

public class LongOperation8<T, R> {
	private UUID taskId = UUID.randomUUID();
	private Thread operationThread;
	private WeakReference<Desktop> desktopRef;
	
	private InterruptibleFunction<T, R> operation;
	private Optional<Consumer<R>> onFinish = Optional.empty();
	private Optional<Consumer<Throwable>> onException = Optional.empty();
	private Optional<Runnable> onCancel = Optional.empty();
	private Optional<Runnable> onCleanup = Optional.empty();
	
	public LongOperation8<T, R> doOperation(InterruptibleFunction<T, R> operation) {
		this.operation = operation;
		return this;
	}

	public LongOperation8<T, R> onFinish(Consumer<R> onFinish) {
		this.onFinish = Optional.of(onFinish);
		return this;
	}

	public LongOperation8<T, R> onCancel(Runnable onCancel) {
		this.onCancel = Optional.of(onCancel);
		return this;
	}

	public LongOperation8<T, R> onCleanup(Runnable onCleanup) {
		this.onCleanup = Optional.of(onCleanup);
		return this;
	}

	public LongOperation8<T, R> onException(Consumer<Throwable> onException) {
		this.onException = Optional.of(onException);
		return this;
	}

	public void start(T input) {
		this.desktopRef = new WeakReference<Desktop>(Executions.getCurrent().getDesktop());

		enableServerPushForThisTask();
		operationThread = new Thread(() -> {run(input);});
		operationThread.start();
	}

	private void run(T input) {
		List<Runnable> finalizeTasks = new ArrayList<>();
		try{
			try {
				R result = operation.applyInterruptible(input);
				onFinish.ifPresent((handler) -> finalizeTasks.add(() -> handler.accept(result)));
			} catch (InterruptedException e) {
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
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
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

	interface InterruptibleFunction<A, B> extends Function<A, B> {
		default B apply(A input) {
			try {
				return applyInterruptible(input);
			} catch (InterruptedException e) {
				throw new InterruptedRuntimeException(e);
			}
		}

		B applyInterruptible(A input) throws InterruptedException; 
	}

	interface InterruptibleConsumer<A> extends Consumer<A> {
		default void accept(A input) {
			try {
				acceptInterruptible(input);
			} catch (InterruptedException e) {
				throw new InterruptedRuntimeException(e);
			}
		}

		void acceptInterruptible(A input) throws InterruptedException; 
	}

	static class InterruptedRuntimeException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		public InterruptedRuntimeException(InterruptedException e) {
			super(e);
		}
	}

}
