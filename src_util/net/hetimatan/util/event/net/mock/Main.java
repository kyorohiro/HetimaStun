package net.hetimatan.util.event.net.mock;


import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.stage.Stage;

public class Main extends Application{

	int width = 400;
	int height = 300;
	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage primStage) throws Exception {

		
		StackPane root = new StackPane();
		Scene scene = new Scene(root, width, height);
		primStage.setScene(scene);
		primStage.show();
		{
			Canvas c = new Canvas(width, height);
			root.getChildren().add(c);
			GraphicsContext context = c.getGraphicsContext2D();
			showDevice(context);

		}
	}

	public void showDevice(GraphicsContext context) {
		int deviceNum = 5;

		int start_x = (width-height)/2;
		int start_y = (50/2);
		int bodySize = height-50;
		int childSize = 30;
		{
			context.setStroke(Color.RED);
			context.strokeArc(start_x, start_y, bodySize, bodySize,
					0, 360, ArcType.ROUND);
		}
		{
			context.setStroke(Color.RED);
			context.strokeArc(
					start_x
					+((bodySize+childSize)/2)*Math.cos(0.0)
					-childSize/2
					+bodySize/2, 
					start_y
					+((bodySize+childSize)/2)*Math.sin(0.0)
					-childSize/2
					+bodySize/2, 
					30,30,
					0, 360, ArcType.OPEN);
			context.setStroke(Color.GREEN);
			context.strokeArc(
					start_x
					+((bodySize+childSize)/2)*Math.cos(0.3)
					-childSize/2
					+bodySize/2, 
					start_y
					+((bodySize+childSize)/2)*Math.sin(0.3)
					-childSize/2
					+bodySize/2, 
					30,30,
					0, 360, ArcType.OPEN);

			context.strokeArc(
					start_x
					+((bodySize+childSize)/2)*Math.cos(2.3)
					-childSize/2
					+bodySize/2, 
					start_y
					+((bodySize+childSize)/2)*Math.sin(2.3)
					-childSize/2
					+bodySize/2, 
					30,30,
					0, 360, ArcType.OPEN);

			context.strokeArc(
					start_x
					+((bodySize+childSize)/2)*Math.cos(3.14+1)
					-childSize/2
					+bodySize/2, 
					start_y
					+((bodySize+childSize)/2)*Math.sin(3.14+1)
					-childSize/2
					+bodySize/2, 
					30,30,
					0, 360, ArcType.OPEN);

		}
	}
}
