package at.streamVis;

import java.util.ArrayList;

import at.streamVis.model.Pipe;
import at.streamVis.model.VisStringConverter;

public class StreamVisConsole implements StreamVis {
	private static final double SEC_DURATION = 0;
	private final ArrayList<Pipe<?>> pipes = new ArrayList<>();
	
	public static void main(String[] args) {
		StreamVisConsole console = new StreamVisConsole();
		Pipe.board = console;
		//
		Pipe.generate(10, i->i-1).map(i->i*i).filter(i->i%2!=0).limit(2).sorted(Integer::compare).toArray(Integer[]::new);
	}

	private void printHeader() {
		for(Pipe<?> pipe: pipes) {
			System.out.print("%-20s".formatted(pipe.name));
		}
		System.out.println();
	}

	public void animateFetch(Pipe<?> pipe) {
		try {
			Thread.sleep((long) (SEC_DURATION*1000));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		printHeader();
		for(int i=0;i<pipe.pipeIndex;i++) {
			System.out.print("%-20s".formatted(pipes.get(i).ts));
		}
		System.out.print(pipe.ts);
		String toDisplay = "<--FETCH--";
		int missing = 20-toDisplay.length()-pipe.ts.toString().length();
		System.out.print(" ".repeat(missing/2));
		System.out.printf(toDisplay);
		System.out.print(" ".repeat(missing-missing/2));
		for(int i=pipe.pipeIndex+1;i<pipes.size();i++) {
			System.out.print("%-20s".formatted(pipes.get(i).ts));
		}
		System.out.println();
	}

	public <T> void animate(Pipe<T> pipe, T t) {
		try {
			Thread.sleep((long) (SEC_DURATION*1000));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		String s = VisStringConverter.convert(t);
		printHeader();
		
		for(int i=0;i<pipe.pipeIndex;i++) {
			System.out.print("%-20s".formatted(pipes.get(i).ts));
		}
		System.out.print(pipe.ts);
		String toDisplay = "--%s-->".formatted(s);
		int missing = 20-toDisplay.length()-pipe.ts.toString().length();
		System.out.print(" ".repeat(missing/2));
		System.out.printf(toDisplay);
		System.out.print(" ".repeat(missing-missing/2));
		for(int i=pipe.pipeIndex+1;i<pipes.size();i++) {
			System.out.print("%-20s".formatted(pipes.get(i).ts));
		}
		System.out.println();
	}
	
	@Override
	public void add(Pipe<?> pipe) {
		pipes.add(pipe);
	}
}
