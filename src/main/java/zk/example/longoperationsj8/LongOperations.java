package zk.example.longoperationsj8;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import zk.example.longoperationsj8.LongOperation8.InterruptibleFunction;

public class LongOperations {

	public static <T, R> LongOperation8<T, R> create(Function<T, R> operation) {
		return new LongOperation8<T, R>()
				.onExecute((InterruptibleFunction<T, R>)operation);
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

	public static <T, R> LongOperationsBuilder<T, R> of(InterruptibleFunction<T, R> operation) {
		return new LongOperationsBuilder<T, R>(() -> {
			return create(operation);
		});
	}
	
	public static class LongOperationsBuilder<T, R> {
		private Supplier<LongOperation8<T, R>> supplier;
		private Consumer<R> onFinish;
		private Runnable onCancel;
		private Runnable onCleanup;
		private Consumer<Throwable> onException;
		public LongOperationsBuilder(Supplier<LongOperation8<T, R>> supplier) {
			this.supplier = supplier;
		}
		
		public LongOperationsBuilder<T, R> onFinish(Consumer<R> onFinish) {
			this.onFinish = onFinish;
			return this;
		}

		public LongOperationsBuilder<T, R> onCancel(Runnable onCancel) {
			this.onCancel = onCancel;
			return this;
		}
		
		public LongOperationsBuilder<T, R> onCleanup(Runnable onCleanup) {
			this.onCleanup = onCleanup;
			return this;
		}

		public LongOperationsBuilder<T, R> onExeption(Consumer<Throwable> onException) {
			this.onException = onException;
			return this;
		}
		
		public LongOperation8<T, R> build() {
			return supplier.get().onFinish(onFinish).onCancel(onCancel).onException(onException).onCleanup(onCleanup);
		}

		public LongOperation8<T, R> start(T input) {
			LongOperation8<T, R> longOperation = build();
			longOperation.start(input);
			return longOperation;			
		}

		public LongOperation8<T, R> start() {
			return start(null);
		}
	}
}
