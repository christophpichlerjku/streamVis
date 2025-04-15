package at.streamVis;

import at.streamVis.model.Pipe;

public interface StreamVis {
	public void add(Pipe<?> pipe);

	public void animateFetch(Pipe<?> pipe);

	public <T> void animate(Pipe<T> pipe, T t);
}
