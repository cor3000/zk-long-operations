package zk.example.longoperations.java8;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class LongOperations {


	/**
	 * shortcut to create a long operation directly
	 * @param operation
	 * @return
	 */
	public static LongOperation<Void, Void> create(Runnable operation) {
		return new LongOperation<Void, Void>().onExecute(_void -> {operation.run(); return null;});
	}

	/**
	 * shortcut to create a long operation directly
	 * @param operation
	 * @return
	 */
	public static <T> LongOperation<T, Void> create(Consumer<T> operation) {
		return new LongOperation<T, Void>().onExecute(t -> {operation.accept(t); return null;});
	}
	
	/**
	 * shortcut to create a long operation directly
	 * @param operation
	 * @return
	 */
	public static LongOperation<Void, Void> create(Runnable operation, Runnable onFinish) {
		return create((_void) -> {operation.run(); return null;}, (_void) -> onFinish.run());
	}

	/**
	 * shortcut to create a long operation directly
	 * @param operation
	 * @return
	 */
	public static <R> LongOperation<Void, R> create(Supplier<R> operation, Consumer<R> onFinish) {
		return create((_void) -> {return operation.get();}, onFinish);
	}
	
	public static <T, R> LongOperation<T, R> create(InterruptibleFunction<T, R> operation, Consumer<R> onFinish) {
		return new LongOperation<T, R>()
				.onExecute(operation)
				.onFinish(onFinish);
	}

	public static <T, R> LongOperationBuilder<T, R> of(InterruptibleFunction<T, R> operation, Consumer<R> onFinish) {
		return new LongOperationBuilder<T, R>(() -> {
			return create(operation, onFinish);
		});
	}

	public static <T, R> LongOperationBuilder<T, R> of(InterruptibleFunction<T, R> operation) {
		return new LongOperationBuilder<T, R>(operation);
	}
}
