package gay.lemmaeof.kdlycontent.util;

import java.util.function.Predicate;

import com.google.common.base.Objects;

/**
 * An Optional you can set later.
 */
//Taken from Lib39-core.
public class LatchReference<T> extends AbstractLatchReference<T> {

	private LatchReference() { super(); }

	/**
	 * Test the value of this latch with the given predicate if the latch is set and present.
	 * Otherwise, returns false.
	 */
	public boolean test(Predicate<T> pred) {
		return isPresent() ? pred.test(get()) : false;
	}

	/**
	 * @return {@code true} if this latch is set and present, and the value is equal to the passed value
	 */
	public boolean is(Object t) {
		return isPresent() && Objects.equal(get(), t);
	}

	/**
	 * @return an unset latch
	 */
	public static <T> LatchReference<T> unset() {
		return new LatchReference<>();
	}

	/**
	 * @return a set empty latch
	 */
	public static <T> LatchReference<T> empty() {
		LatchReference<T> lr = new LatchReference<>();
		lr.setEmpty();
		return lr;
	}

	/**
	 * @return a set latch with the given value
	 */
	public static <T> LatchReference<T> of(T t) {
		LatchReference<T> lr = new LatchReference<>();
		lr.set(t);
		return lr;
	}

}
