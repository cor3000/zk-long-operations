package zk.example.longoperationsj8;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import zk.example.longoperationsj8.LongOperation8.InterruptibleFunction;

public class LongOperations {
	public static LongOperation8<Void, Void> create(Runnable operation) {
		return create((_void) -> {operation.run(); return null;}, null);
	}

	public static LongOperation8<Void, Void> create(Runnable operation, Runnable onFinish) {
		return create((_void) -> {operation.run(); return null;}, (r) -> onFinish.run());
	}

	public static <R> LongOperation8<Void, R> create(Supplier<R> operation, Consumer<R> onFinish) {
		return create((_void) -> {return operation.get();}, onFinish);
	}
	
	public static <T, R> LongOperation8<T, R> create(Function<T, R> operation, Consumer<R> onFinish) {
		return new LongOperation8<T, R>()
				.onExecute((InterruptibleFunction<T, R>)operation)
				.onFinish(onFinish);
	}

	public static <T, R> LongOperationsBuilder<T, R> of(InterruptibleFunction<T, R> operation, Consumer<R> onFinish) {
		return new LongOperationsBuilder<T, R>(() -> {
			return create(operation, onFinish);
		});
	}
	
	public static class LongOperationsBuilder<T, R> {
		private Supplier<LongOperation8<T, R>> supplier;
		private Runnable onCancel;
		private Runnable onCleanup;
		private Consumer<Throwable> onException;
		public LongOperationsBuilder(Supplier<LongOperation8<T, R>> supplier) {
			this.supplier = supplier;
		}
		
		public LongOperationsBuilder<T, R> withOnCancel(Runnable onCancel) {
			this.onCancel = onCancel;
			return this;
		}

		public LongOperationsBuilder<T, R> withOnCleanup(Runnable onCleanup) {
			this.onCleanup = onCleanup;
			return this;
		}

		public LongOperationsBuilder<T, R> withOnExeption(Consumer<Throwable> onException) {
			this.onException = onException;
			return this;
		}
		
		public LongOperation8<T, R> build() {
			return supplier.get().onCancel(onCancel).onException(onException).onCleanup(onCleanup);
		}

		public void start(T input) {
			build().start(input);
		}

		public void start() {
			start(null);
		}
	}
}
