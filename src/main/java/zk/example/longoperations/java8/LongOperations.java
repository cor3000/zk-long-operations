package zk.example.longoperations.java8;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * helper class to directly create {@link LongOperation}s and {@link LongOperationBuilder}s 
 * @author Robert
 *
 */
public class LongOperations {
	/**
	 * shortcut to create a LongOperation<Void, Void> directly (no input, no result)
	 * @param operation
	 * @return
	 */
	public static LongOperation<Void, Void> create(Runnable operation) {
		return of((Void _void) -> {operation.run(); return (Void)null;}).build();
	}

	/**
	 * shortcut to create a LongOperation<T, Void> directly (just input, no result)
	 * @param operation
	 * @return
	 */
	public static <T> LongOperation<T, Void> create(Consumer<T> operation) {
		return of((T t) -> {operation.accept(t); return (Void)null;}).build();
	}
	
	/**
	 * shortcut to create a LongOperation<Void, Void> directly (no input, no result)
	 * with a callback onFinish
	 * @param operation
	 * @return
	 */
	public static LongOperation<Void, Void> create(Runnable operation, Runnable onFinish) {
		return create((_void) -> {operation.run(); return null;}, (_void) -> onFinish.run());
	}

	/**
	 * shortcut to create a LongOperation<Void, R> directly (no input, just result)
	 * with a callback onFinish
	 * @param operation
	 * @return
	 */
	public static <R> LongOperation<Void, R> create(Supplier<R> operation, Consumer<R> onFinish) {
		return create((_void) -> {return operation.get();}, onFinish);
	}
	
	/**
	 * shortcut to create a LongOperation<T, R> directly (both input and result)
	 * with a callback onFinish
	 * @param operation
	 * @return
	 */
	public static <T, R> LongOperation<T, R> create(InterruptibleFunction<T, R> operation, Consumer<R> onFinish) {
		return of(operation).onFinish(onFinish).build();
	}

	/**
	 * create a LongOperationBuilder<T, R> of an operation
	 * enables adding optional callbacks before building/starting the operation
	 * @param operation
	 * @return
	 */
	public static <T, R> LongOperationBuilder<T, R> of(InterruptibleFunction<T, R> operation) {
		return new LongOperationBuilder<T, R>(operation);
	}
}
