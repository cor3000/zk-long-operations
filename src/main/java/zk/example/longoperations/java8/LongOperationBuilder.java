package zk.example.longoperations.java8;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class LongOperationBuilder<T, R> {
	private Supplier<LongOperation<T, R>> supplier = LongOperation::new;
	private InterruptibleFunction<T, R> operation;
	private Consumer<R> onFinish;
	private Runnable onCancel;
	private Runnable onCleanup;
	private Consumer<Throwable> onException;
	
	public LongOperationBuilder(Supplier<LongOperation<T, R>> supplier) {
		this.supplier = supplier;
	}
	
	public LongOperationBuilder(InterruptibleFunction<T, R> operation) {
		this.operation = operation;
	}

	public LongOperationBuilder<T, R> onExecute(InterruptibleFunction<T, R> operation) {
		this.operation = operation;
		return this;
	}
	
	public LongOperationBuilder<T, R> onFinish(Consumer<R> onFinish) {
		this.onFinish = onFinish;
		return this;
	}

	public LongOperationBuilder<T, R> onCancel(Runnable onCancel) {
		this.onCancel = onCancel;
		return this;
	}
	
	public LongOperationBuilder<T, R> onCleanup(Runnable onCleanup) {
		this.onCleanup = onCleanup;
		return this;
	}

	public LongOperationBuilder<T, R> onException(Consumer<Throwable> onException) {
		this.onException = onException;
		return this;
	}
	
	public LongOperation<T, R> build() {
		return supplier.get()
				.onExecute(operation)
				.onFinish(onFinish)
				.onCancel(onCancel)
				.onException(onException)
				.onCleanup(onCleanup);
	}

	public LongOperation<T, R> start(T input) {
		LongOperation<T, R> longOperation = build();
		longOperation.start(input);
		return longOperation;			
	}

	public LongOperation<T, R> start() {
		return start(null);
	}
}