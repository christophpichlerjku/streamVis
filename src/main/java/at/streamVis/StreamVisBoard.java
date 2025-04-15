package at.streamVis;

import at.streamVis.model.Pipe;
import at.streamVis.model.VisStringConverter;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * JavaFX App
 */
public class StreamVisBoard extends Application implements StreamVis {
	private final HBox columns = new HBox(PIPE_DISTANCE);
	private final ObservableList<Pipe<?>> pipes = FXCollections.observableArrayList();

	private static final int PIPE_DISTANCE = 200;
	private static final double RADIUS = 30;
	private static final double SEC_DURATION = 0;

	@Override
	public void start(@SuppressWarnings("exports") Stage stage) {
		Pipe.board = this;
		Button startButton = new Button("Start");
		VBox vBox = new VBox(startButton, columns);
		var scene = new Scene(vBox);
		startButton.setOnAction(e -> {
			Pipe.generate(10, i->i-1).map(i->i*i).filter(i->i%2!=0).limit(2).sorted(Integer::compare).toArray(Integer[]::new);
		});
		pipes.addListener(new ListChangeListener<>() {

			@Override
			public void onChanged(Change<? extends Pipe<?>> c) {
				while (c.next()) {
					for (Pipe<?> p : c.getAddedSubList()) {
						registerPipe(p, p.pipeIndex);
					}
				}
			}
		});

		stage.setScene(scene);
		stage.setWidth(PIPE_DISTANCE*4);
		stage.setHeight(400);
		stage.show();
	}

	private void registerPipe(Pipe<?> p, int idx) {
		TableView<String> table = new TableView<String>(p.shapes);
		table.setMinWidth(PIPE_DISTANCE);
		TableColumn<String, String> col = new TableColumn<>(p.name);
		table.getColumns().add(col);
		columns.getChildren().add(table);
	}

	public void add(Pipe<?> pipe) {
		pipes.add(pipe);
	}

	public static void main(String[] args) {
		launch();
	}
	
	private void printHeader() {
		for(Pipe<?> pipe: pipes) {
			System.out.print("%-20s".formatted(pipe.name));
		}
		System.out.println();
	}

	public void animateFetch(Pipe<?> pipe) {
		//TODO animation
	}

	public <T> void animate(Pipe<T> pipe, T t) {
		
//		Circle circle = new Circle((pipe.pipeIndex+0.5)*PIPE_DISTANCE, 50, RADIUS, Color.LIGHTBLUE);
//		Label label = new Label(s,circle);
//		//TODO animation
//		TranslateTransition transition = new TranslateTransition(Duration.seconds(SEC_DURATION), label);
//		transition.setToX((pipe.pipeIndex+1.5)*PIPE_DISTANCE);
//		transition.setInterpolator(Interpolator.EASE_BOTH);
//		Platform.runLater(transition::play);
		
	}

}