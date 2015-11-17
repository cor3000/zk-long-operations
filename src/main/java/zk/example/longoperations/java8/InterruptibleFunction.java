package zk.example.longoperations.java8;

import java.util.function.Function;

@FunctionalInterface
public interface InterruptibleFunction<T, R> extends Function<T, R> {
	default R apply(T input) {
		try {
			return applyInterruptible(input);
		} catch (InterruptedException e) {
			throw new RuntimeInterruptedException(e);
		}
	}

	R applyInterruptible(T input) throws InterruptedException; 
}