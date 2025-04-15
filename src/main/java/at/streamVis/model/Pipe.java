package at.streamVis.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import at.streamVis.StreamVis;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public abstract class Pipe<T> {
	public final String name;
	public final Pipe<?> previous;
	public final int pipeIndex;
	public final ObservableList<T> ts;
	public final ObservableList<String> shapes;
	public static StreamVis board;

	private Pipe(String name, Pipe<?> previous, int pipeIndex) {
		this.name = name;
		this.previous = previous;
		this.pipeIndex = pipeIndex;
		this.ts = FXCollections.observableArrayList();
		this.shapes = FXCollections.observableArrayList();
		this.ts.addListener(new ListChangeListener<>() {

			@Override
			public void onChanged(Change<? extends T> c) {
				updateShapes();
			}
		});
		board.add(this);
	}

	private Pipe(String name, Pipe<?> previous) {
		this(name, previous, previous.pipeIndex + 1);
	}

	private Pipe(String name) {
		this(name, null, 0);
	}

	protected final Optional<T> getElem() {
		board.animateFetch(this);
		fill();
		T t = ts.isEmpty() ? null : ts.removeLast();
		board.animate(this, t);
		return Optional.ofNullable(t);
	}

	protected abstract void fill();

	private void updateShapes() {
		ts.stream().map(VisStringConverter::convert).forEach(shapes::add);
	}

	public static <T> Pipe<T> empty() {
		return new Pipe<T>("empty") {
			@Override
			protected void fill() {

			}
		};
	}

	public static <T> Pipe<T> of(@SuppressWarnings("unchecked") T... elements) {
		return new Pipe<T>("of") {
			boolean done = false;

			@Override
			protected void fill() {
				if (!done) {
					for (T t : elements) {
						this.ts.add(0, t);
					}
					done = true;
				}
			}
		};
	}

	public static <T> Pipe<T> generate(T initial, UnaryOperator<T> generator) {
		return new Pipe<T>("generate") {
			private T cur = null;

			@Override
			protected void fill() {
				if (cur == null) {
					cur = initial;
				} else {
					cur = generator.apply(cur);
				}
				ts.add(0, cur);
			}
		};
	}

	public static <T> Pipe<T> generate(Supplier<T> supplier) {
		return new Pipe<T>("generate") {
			@Override
			protected void fill() {
				ts.add(0, supplier.get());
			}
		};
	}

	public Pipe<T> filter(Predicate<T> predicate) {
		Pipe<T> old = this;
		return new Pipe<T>("filter", old) {
			@Override
			protected void fill() {
				Optional<T> opt = old.getElem();
				while (opt.isPresent() && !predicate.test(opt.get())) {
					opt = old.getElem();
				}
				opt.ifPresent(ts::addFirst);
			}
		};
	}

	public <R> Pipe<R> map(Function<? super T, ? extends R> mapper) {
		Pipe<T> old = this;
		return new Pipe<R>("map", old) {
			@Override
			protected void fill() {
				Optional<T> optT = old.getElem();
				if (optT.isPresent()) {
					ts.add(mapper.apply(optT.get()));
				}
			}
		};
	}

	public Pipe<T> limit(long limit) {
		Pipe<T> old = this;
		return new Pipe<T>("limit(%d)".formatted(limit), old) {
			private long missing = limit;

			@Override
			protected void fill() {
				if (missing > 0) {
					Optional<T> opt = old.getElem();
					if (opt.isPresent()) {
						missing--;
						ts.add(0, opt.get());
					}
				}
			}
		};
	}

	public Pipe<T> sorted(Comparator<T> comparator) {
		Pipe<T> old = this;
		return new Pipe<T>("sorted", old) {
			boolean done = false;

			@Override
			protected void fill() {
				if (done) {
					return;
				}
				Optional<T> opt = old.getElem();
				while (opt.isPresent()) {
					ts.add(opt.get());
					// sort inversely as last elem is removed from ts
					ts.sort(inverse(comparator));
					opt = old.getElem();
				}
				done = true;
			}
		};
	}

	private static <T> Comparator<T> inverse(Comparator<T> comparator) {
		return (a, b) -> comparator.compare(b, a);
	}

	public Pipe<T> distinct() {
		Pipe<T> old = this;
		HashSet<T> seen = new HashSet<>();
		return new Pipe<T>("distinct", old) {
			@Override
			protected void fill() {
				Optional<T> opt = old.getElem();
				if (opt.isPresent() && seen.contains(opt.get())) {
					opt = old.getElem();
				}
				if (opt.isPresent()) {
					seen.add(opt.get());
					ts.add(0, opt.get());
				}
			}
		};
	}

	public long count() {
		return map(t -> 1L).reduce(0L, Long::sum);
	}

	public T reduce(T initial, BinaryOperator<T> combiner) {
		return _reduce(initial, combiner).findFirst().get();
	}
	
	public Pipe<T> _reduce(T initial, BinaryOperator<T> combiner) {
		
		
		Pipe<T> old = this;
		return new Pipe<T>("reduce",old) {

			@Override
			protected void fill() {
				ts.add(initial);
				Optional<T> opt = old.getElem();
				while (opt.isPresent()) {
					T val = ts.removeLast();
					val = combiner.apply(val, opt.get());
					ts.addLast(val);
					opt = old.getElem();
				}
			}};
	}
	
	public Optional<T> findFirst() {
		return getElem();
	}

	public void forEach(Consumer<T> consumer) {
		Optional<T> opt = getElem();
		while (opt.isPresent()) {
			consumer.accept(opt.get());
			opt = getElem();
		}
	}

	public T[] toArray(IntFunction<T[]> generator) {
		return _toArray().findFirst().get().toArray(generator);
	}
	
	public Pipe<List<T>> _toArray() {
		Pipe<T> old = this;
		return new Pipe<List<T>>("toArray",old) {
			@Override
			protected void fill() {
				Optional<T> opt = old.getElem();
				ArrayList<T> elements = new ArrayList<>();
				ts.add(elements);
				while (opt.isPresent()) {
					elements.add(opt.get());
					opt = old.getElem();
				}
			}
		};
	}

}
