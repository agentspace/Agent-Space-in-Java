
package AllenAS;

import java.util.Random;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import wrl.com.microstepmis.agentspace.*;
import wrl.com.microstepmis.schd.*;
/**
*Class AllenAS extends JavaFX Application.
*This application simulates robot Allen.
*AgentSpace architecture is used to control the behaviour.
*/
public class AllenAS extends Application {
    
    private  AllenCanvas AllenCanvas;
    private Button btnLayerPlus, btnLayerMinus, btnSpeedPlus, btnSpeedMinus, btnRestart, btnQuit;
    private Space space = SpaceFactory.getInstance();
    
    public static float speed;
    public static int layer, layer0Active, layer1Active, layer2Active, layer3Active, layer3Found;
    
    Random rnd = new Random();
    
    
    @Override
    public void start(Stage primaryStage) {
        
    	speed = 2;
    	layer = 0;
    	layer0Active = 0;
        BorderPane bp = new BorderPane();
        AllenCanvas = new AllenCanvas();
        bp.setCenter(AllenCanvas);
        
        HBox topPane = new HBox(
                
                btnLayerPlus = new Button("Layer + "),
                btnLayerMinus = new Button("Layer - "),
                btnSpeedPlus = new Button("Speed + "),
                btnSpeedMinus = new Button("Speed - "),
                btnRestart = new Button("Restart"),
                btnQuit = new Button("Quit"));

        setButtonsLabels();
        topPane.setSpacing(60);
        topPane.setStyle("-fx-background-color: rgb(16,135,199);"
                + "-fx-padding: 5;");
        bp.setTop(topPane);
        
        btnLayerPlus.setOnAction(event -> {
            if (layer < 3) {
            	layer += 1;
            }
        });
        
        btnLayerMinus.setOnAction(event -> {
            if (layer > 0) {
            	layer -= 1;
            	if (layer == 0) {
            		layer1Active = 0;
            	}
            	if (layer == 1) {
            		layer2Active = 0;
            	}
            	if (layer == 2) {
            		if (layer3Found == 1) space.write("Forward", new Float (1));
            		layer3Found = 0;
            		layer3Active = 0;
            	}
            }
        }); 
        
        btnSpeedPlus.setOnAction(event -> {
            if (speed != 0 && speed < 3) {
            	speed += 1;
            }
        });
        
        btnSpeedMinus.setOnAction(event -> {
            if (speed != 0 && speed > 1) {
            	speed -= 1;
            }
        });
        
        btnRestart.setOnAction(event -> {
        	float cmp = rnd.nextFloat()*360;
            AllenCanvas.Allen.x = 400;
            AllenCanvas.Allen.y = 250;
            space.write("Compass", cmp);
            space.write("Forward", new Float (1));
            speed = 2;
            layer = 0;
            layer1Active = 0;
            layer3Found = 0;
        });
        
        btnQuit.setOnAction(event -> System.exit(0));

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent e) {
                Platform.exit();
                System.exit(0);
            }
        });
        
        Scene scene = new Scene(bp);				
        primaryStage.setTitle("Allen Agent-Space"); 	
        primaryStage.setScene(scene);
        //Css sheet pre Scene style 
        scene.getStylesheets().add("sceneCSS.css");
        primaryStage.show();
    }
    /**
     * Method setButtonsLabels turns off keyboard focus for buttons.
     */
    
    public void setButtonsLabels() {
        btnQuit.setFocusTraversable(false);   
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    class AllenCanvas extends Canvas  {
    	private Allen Allen;
        private int sizeX = 800;
        private int sizeY = 700;
        private ImageView ivAllen = new ImageView(new Image("file:res/Allen.png"));
        private ImageView ivCompass = new ImageView(new Image("file:res/Compass.png"));
        private Image imageAllena = new Image("file:res/Allena.png");
        private Image imageSonar = new Image("file:res/Sonar.png");
        private Image imageStatus = new Image("file:res/Layers_blocks.png");
        
      
        /**
         * Constructor of class AllenCanvas. 
         */
        private AllenCanvas() {
            this.setWidth(sizeX);
            this.setHeight(sizeY);
            Allen = new Allen();
            Allen.start();
            
            space.write("Compass", new Float(0));
            space.write("Sonar", new float[12]);
            space.write("Turn", new Integer(0));
            space.write("Forward", new Float(1));
            
            float cmp = rnd.nextFloat() * 360;
            space.write("Compass", cmp);
            
            paint();
        }
        
        private void paint() {
            GraphicsContext gc = getGraphicsContext2D();
            gc.clearRect(0, 0, this.sizeX, this.sizeY);
            gc.setStroke(Color.GRAY);
            gc.setLineWidth(30);
            gc.strokeLine(100, 100, 700, 100); // up
            gc.strokeLine(100, 400, 700, 400); // down
            gc.strokeLine(100, 100, 100, 400); // left
            gc.strokeLine(700, 100, 700, 400); // right
            
            if (layer == 3) gc.drawImage(imageAllena, 130, 233); 
             
            drawRotatedTranslated(gc, ivAllen, (float) space.read("Compass"), Allen.x, Allen.y);

            gc.setFont(new Font("Sans", 20));
            gc.fillText("Block Compass:  "+ String.format("%.01f",(float) space.read("Compass")), 70, 550);
            drawRotatedTranslated(gc, ivCompass, (float) space.read("Compass"), 320, 542);
            gc.drawImage(imageSonar, 560, 470); 
            gc.drawImage(imageStatus, 2, 650); 
            paintStatus(gc);
            paintSonarDistances(gc);
            paintLayerRect(gc);
        }
        
        /**
         * Paints distances of block Sonar to Canvas. 
         */        
        private void paintSonarDistances(GraphicsContext gc) {
        	float[] distances = (float[]) space.read("Sonar");
        	Color[] colors = new Color[12]; 
        	gc.setFont(new Font("Sans", 15));
        	for (int i = 0; i < 12; i++) {
        		if (distances[i] < 50) {
        			colors[i] = Color.RED;
        		}
        		else {
        			colors[i] = Color.BLACK;
        		}
        	}
        	gc.setFill(colors[0]);
        	gc.fillText(String.format("%.01f", distances[0]), 695, 542);
        	gc.setFill(colors[1]);
        	gc.fillText(String.format("%.01f", distances[1]), 687, 582);
        	gc.setFill(colors[2]);
        	gc.fillText(String.format("%.01f", distances[2]), 655, 613);
        	gc.setFill(colors[3]);
        	gc.fillText(String.format("%.01f", distances[3]), 608, 625);
        	gc.setFill(colors[4]);
        	gc.fillText(String.format("%.01f", distances[4]), 561, 613);
        	gc.setFill(colors[5]);
        	gc.fillText(String.format("%.01f", distances[5]), 528, 582);
        	gc.setFill(colors[6]);
        	gc.fillText(String.format("%.01f", distances[6]), 521, 542);
        	gc.setFill(colors[7]);
        	gc.fillText(String.format("%.01f", distances[7]), 528, 502);
        	gc.setFill(colors[8]);
        	gc.fillText(String.format("%.01f", distances[8]), 561, 471);
        	gc.setFill(colors[9]);
        	gc.fillText(String.format("%.01f", distances[9]), 608, 459);
        	gc.setFill(colors[10]);
        	gc.fillText(String.format("%.01f", distances[10]), 655, 471);
        	gc.setFill(colors[11]);
        	gc.fillText(String.format("%.01f", distances[11]), 687, 502);
        	gc.setFill(Color.BLACK);
        }
        
        /**
         * Paints the status of block Compass and speed of robot to Canvas. 
         */
        private void paintStatus(GraphicsContext gc) {
        	switch (layer) {
    		case 0:
    			gc.fillText("Layer 0 - Forward", 70, 500);
    			break;
    		case 1:
    			gc.fillText("Layer 1 - Avoiding", 70, 500);
    			break;
    		case 2:
    			gc.fillText("Layer 2 - Wandering", 70, 500);
    			break;
    		case 3:
    			gc.fillText("Layer 3 - Exploring", 70, 500);
    			break;
        	}
        	gc.fillText("Speed: "+speed, 70, 600);
        }
        
        /**
         * Paints the color rectangles based on active layers. 
         */
        private void paintLayerRect(GraphicsContext gc) {
			gc.setStroke(Color.RED);
			gc.setLineWidth(5);
			gc.strokeLine(12, 661, 98, 661); // up
			gc.strokeLine(12, 688, 98, 688); // down
			gc.strokeLine(12, 662, 12, 687); // left
			gc.strokeLine(98, 662, 98, 687); // right
			if (layer > 0) {
				gc.setStroke(Color.GREEN);
				gc.setLineWidth(5);
				gc.strokeLine(109, 661, 195, 661); // up
				gc.strokeLine(109, 688, 195, 688); // down
				gc.strokeLine(109, 662, 109, 687); // left
				gc.strokeLine(195, 662, 195, 687); // right
				if (layer > 1) {
					gc.setStroke(Color.ORANGE);
					gc.setLineWidth(5);
					gc.strokeLine(205, 661, 291, 661); // up
					gc.strokeLine(205, 688, 291, 688); // down
					gc.strokeLine(205, 662, 205, 687); // left
					gc.strokeLine(291, 662, 291, 687); // right
					if (layer > 2) {
						gc.setStroke(Color.PURPLE);
						gc.setLineWidth(5);
						gc.strokeLine(300, 661, 387, 661); // up
						gc.strokeLine(300, 688, 387, 688); // down
						gc.strokeLine(300, 662, 300, 687); // left
						gc.strokeLine(387, 662, 387, 687); // right
					}
				}
			}
			
			gc.setFont(new Font("Verdana", 20));
			gc.fillText(String.format("%.00f", space.read("Forward")), 543, 683);
			gc.fillText(""+space.read("Turn"), 730, 683);
			
			if (layer0Active == 1) {
				gc.setStroke(Color.RED);
				gc.setLineWidth(5);
				gc.strokeLine(396, 661, 583, 661); // up
				gc.strokeLine(396, 688, 583, 688); // down
				gc.strokeLine(396, 662, 396, 687); // left
				gc.strokeLine(583, 662, 583, 687); // right
			}
			if (layer1Active == 1) {
				gc.setStroke(Color.GREEN);
				gc.setLineWidth(5);
				gc.strokeLine(592, 661, 788, 661); // up
				gc.strokeLine(592, 688, 788, 688); // down
				gc.strokeLine(592, 662, 592, 687); // left
				gc.strokeLine(788, 662, 788, 687); // right

			}
			if (layer2Active == 1) {
				gc.setStroke(Color.ORANGE);
				gc.setLineWidth(5);
				gc.strokeLine(592, 661, 788, 661); // up
				gc.strokeLine(592, 688, 788, 688); // down
				gc.strokeLine(592, 662, 592, 687); // left
				gc.strokeLine(788, 662, 788, 687); // right

			}
			if (layer3Active == 1) {
				gc.setStroke(Color.PURPLE);
				gc.setLineWidth(5);
				gc.strokeLine(592, 661, 788, 661); // up
				gc.strokeLine(592, 688, 788, 688); // down
				gc.strokeLine(592, 662, 592, 687); // left
				gc.strokeLine(788, 662, 788, 687); // right
			}
			if (layer3Found == 1) {
				gc.setStroke(Color.PURPLE);
				gc.setLineWidth(5);
				gc.strokeLine(396, 661, 583, 661); // up
				gc.strokeLine(396, 688, 583, 688); // down
				gc.strokeLine(396, 662, 396, 687); // left
				gc.strokeLine(583, 662, 583, 687); // right
			}
		}
        /**
         * Paints rotated ImageView to Canvas. 
         */
        private void drawRotatedTranslated(GraphicsContext gc, ImageView iv, float angle, float x, float y) {
            iv.setRotate(angle);
            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            Image rotatedImage = iv.snapshot(params, null);
            gc.drawImage(rotatedImage, x-rotatedImage.getWidth()/2, y-rotatedImage.getHeight()/2);
        }
        
        class Allen extends Thread {
            private float x, y;
                     
            /**
             * Constructor of class Allen.
             */
            private Allen() {
                this.x = 400;
                this.y = 250;
                
                new SchdProcess("space",wrl.com.microstepmis.agentspace.SpaceFactory.class,new String[]{});
  
                new SchdProcess("MovementAgent",MovementAgent.class, new String[]{});
                
                new SchdProcess("RotationAgent",RotationAgent.class,new String[]{}); 
            }
            /**
             * Calculates the next position of robot based on speed and block Forward. 
             */
            private void calculatePosition() {
            	float forwardSpeed = (float) space.read("Forward")*speed;

            	if ((float) space.read("Compass") >= 360) {
            		space.write("Compass", (float) 0.1);
            	}
            	if ((float) space.read("Compass") <= 0) {
            		space.write("Compass", (float) 359.9);
            	}
            	float angle = (float) space.read("Compass");
            	float sin = (float) Math.sin(Math.toRadians(angle)) * forwardSpeed;
            	this.y += sin;
            	if (angle >= 90 && angle <= 270) this.x -= Math.sqrt(Math.pow(forwardSpeed, 2) - Math.pow(sin, 2));  
            	else this.x += Math.sqrt(Math.pow(forwardSpeed, 2) - Math.pow(sin, 2));
            	space.write("PositionX", x);
            	space.write("PositionY", y);
            }
            /**
             * Calculates the distances of 12 sensors to walls.
             */
            private void calculateDistances() {
            	
				float[] sonarArray = new float[12];

				float adjacent, opposite, angle;

				for (int i = 0; i < 12; i++) {
					angle = i * 30;
					adjacent = 685 - this.x;

					if (angle >= 90 && angle <= 270)
						adjacent = 115 - this.x;
					else
						adjacent = 685 - this.x;

					float cathetusAdjacent = (float) (adjacent / Math.cos(Math
							.toRadians(angle)));
					if (cathetusAdjacent < 0)
						cathetusAdjacent *= -1;

					if (angle >= 0 && angle <= 180)
						opposite = 385 - this.y;
					else
						opposite = 115 - this.y;

					float cathetusOpposite = (float) (opposite / Math.sin(Math
							.toRadians(angle)));
					if (cathetusOpposite < 0)
						cathetusOpposite *= -1;

					if (cathetusAdjacent < cathetusOpposite)
						sonarArray[i] = cathetusAdjacent;
					else
						sonarArray[i] = cathetusOpposite;
				}
            	space.write("Sonar", sonarArray);
            }
            /**
             * Calls methods calculatePosition, calculateDistances and paint, then waits 100ms. 
             */
            @Override
            public void run() {
                while(true) {
                    Platform.runLater(new Runnable() {
                        public void run() {
                        	calculatePosition();
                        	calculateDistances();                     	
                        	paint();
                        }
                    });
                    try {
                        sleep(100);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
