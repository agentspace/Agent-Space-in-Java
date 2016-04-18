
package AllenAS;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static java.lang.System.gc;
import static java.lang.Thread.sleep;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import wrl.com.microstepmis.agentspace.*;
import wrl.com.microstepmis.schd.*;

public class AllenAS extends Application {
    
    private  AllenCanvas AllenCanvas;
    private Label lbLayer, lbL, lbS, lbF, lbForward, lbTurn, lbT, lbSpeed, lbSp;
    private Button btnLayerPlus, btnLayerMinus, btnSpeedPlus, btnSpeedMinus, btnRestart, btnQuit;
    private Space space = SpaceFactory.getInstance();
    
    public static float speed;
    public static int layer;
    
    @Override
    public void start(Stage primaryStage) {
        
    	speed = 2;
    	layer = 0;
        BorderPane bp = new BorderPane();
        AllenCanvas = new AllenCanvas();
        // AllenCanvas.setFocusTraversable(true);
        bp.setCenter(AllenCanvas);
        
        HBox topPane = new HBox(
                
                btnLayerPlus = new Button("Layer + "),
                btnLayerMinus = new Button("Layer - "),
                btnSpeedPlus = new Button("Speed + "),
                btnSpeedMinus = new Button("Speed - "),
                btnRestart = new Button("Restart"),
                btnQuit = new Button("Quit"));

        
        

        
        HBox bottomPane = new HBox(
                lbS = new Label("Status: "),
                
                lbL = new Label("Layer:"),
                lbLayer = new Label("0 - Forward"),
                
                lbF = new Label("Movement:"),
                lbForward = new Label("Forward"),
        
                lbT = new Label("Turn: "),
                lbTurn = new Label("0"),
        
                lbSp = new Label("Speed:"),
                lbSpeed = new Label(speed+""));


        setButtonsLabels();
        topPane.setSpacing(60);
        bottomPane.setSpacing(5);
        topPane.setStyle("-fx-background-color: rgb(16,135,199);"
                + "-fx-padding: 5;");
        bottomPane.setStyle("-fx-background-color: rgb(16,135,199);"
                + "-fx-padding: 5;");
        bp.setTop(topPane);
        bp.setBottom(bottomPane);
        
        btnLayerPlus.setOnAction(event -> {
            if (layer < 3) {
            	layer += 1;
            	setLayerLabel();
            }
        });
        
        btnLayerMinus.setOnAction(event -> {
            if (layer > 0) {
            	layer -= 1;
            	setLayerLabel();
            	if (layer == 1) {
            		space.write("Turn", (Integer) 0);
            	}
            }
        }); 
        
        btnSpeedPlus.setOnAction(event -> {
            if (speed != 0 && speed < 3) {
            	speed += 1;
            	System.out.println(speed);
            	lbSpeed.setText(speed+"");
            }
        });
        
        btnSpeedMinus.setOnAction(event -> {
            if (speed != 0 && speed > 1) {
            	speed -= 1;
            	System.out.println(speed);
            	lbSpeed.setText(speed+"");
            }
        });
        
        btnRestart.setOnAction(event -> {
            System.out.println("Restart.");
            AllenCanvas.Allen.x = 400;
            AllenCanvas.Allen.y = 250;
            space.write("Compass", (float) 0);
            speed = 2;
            lbSpeed.setText(speed+"");
            layer = 0;
            setLayerLabel();
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
     * Metóda setButtonsLabels vypne všetkým Buttons focus z klávesnice a 
     * nastaví jednotný font pre všetky Labels.
     */
    public void setButtonsLabels() {
        
        btnQuit.setFocusTraversable(false);
        lbS.setFont(Font.font("Verdana", 14));
        
    }
    
    public void setLayerLabel() {
    	switch (layer) {
    		case 0:
    			lbLayer.setText("0 - Forward");
    			break;
    		case 1:
    			lbLayer.setText("1 - Avoiding");
    			break;
    		case 2:
    			lbLayer.setText("2 - Wandering");
    			break;
    		case 3:
    			lbLayer.setText("3 - Exploring");
    			break;
    	}
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
        private int sizeY = 500;
        private ImageView ivAllen = new ImageView(new Image("file:res/Allen.png"));
        private Image imageAllena = new Image("file:res/Allena.png");
        
        
        
      
        /**
         * Konštruktor triedy AllenCanvas. 
         */
        public AllenCanvas() {
            this.setWidth(sizeX);
            this.setHeight(sizeY);
            Allen = new Allen();
            Allen.start();
            
            space.write("Compass",new Float(0));
            space.write("Sonar", new float[12]);
            space.write("Turn", new Integer(0));
            space.write("Forward", new Float(1));
            space.write("PositionX", new Float(0));
            space.write("PositionY", new Float(0));
            
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
             
            drawRotatedTranslated(gc, ivAllen, (float) space.read("Compass"));
        }
        
        private void paint3rdLayer() {
            GraphicsContext gc = getGraphicsContext2D();
            gc.clearRect(0, 0, this.sizeX, this.sizeY);
            gc.setStroke(Color.GRAY);
            gc.setLineWidth(30);
            gc.strokeLine(100, 100, 700, 100); // up
            gc.strokeLine(100, 400, 700, 400); // down
            gc.strokeLine(100, 100, 100, 400); // left
            gc.strokeLine(700, 100, 700, 400); // right
            gc.drawImage(imageAllena, 130, 233); 
            drawRotatedTranslated(gc, ivAllen, (float) space.read("Compass"));
        }
        
        private void drawRotatedTranslated(GraphicsContext gc, ImageView iv, float angle) {
            iv.setRotate(angle);
            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            Image rotatedImage = iv.snapshot(params, null);
            gc.drawImage(rotatedImage, Allen.x-rotatedImage.getWidth()/2, Allen.y-rotatedImage.getHeight()/2);
        }
        class Allen extends Thread {
            public float x, y;
            
            
                       
            /**
             * Konštruktor triedy Allen.
             */
            public Allen() {
                this.x = 400;
                this.y = 250;

                /*for (int i=1; i<37; i++) {
                	calculateDistance(i*10);
                }*/
                

                System.out.println(space);
                

                new SchdProcess("space",wrl.com.microstepmis.agentspace.SpaceFactory.class,new String[]{});
                
                //System.out.println("AllenCanvas pred SchdProcess: "+AllenCanvas);
                
                new SchdProcess("MovementAgent",MovementAgent.class, new String[]{});
                
                new SchdProcess("RotationAgent",RotationAgent.class,new String[]{}); 
                
            }
            
            
            
            public void calculatePosition() {
            	
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
            	//System.out.println("Uhol: "+angle+", nove x: "+this.x+", nove y: "+this.y+".");
            	
            }
            
            public float calculateDistance(float angle) {
            	float adjacent, opposite;
            	adjacent = 685 - this.x;
            	if (angle >= 90 && angle <= 270) adjacent = 115 - this.x; 
            	else adjacent = 685 - this.x;
            	 
            	float cathetusAdjacent = (float) (adjacent/Math.cos(Math.toRadians(angle)));
            	if (cathetusAdjacent < 0 ) cathetusAdjacent *= -1; 
            	//System.out.println("Cathetus adjacent: "+cathetusAdjacent);
            	
            	if (angle >= 0 && angle <= 180) opposite = 385 - this.y; 
            	else opposite = 115 - this.y;
            	
            	float cathetusOpposite = (float) (opposite/Math.sin(Math.toRadians(angle)));
            	if (cathetusOpposite < 0 ) cathetusOpposite *= -1; 
            	//System.out.println("Cathetus opposite: "+cathetusOpposite);
            	
            	if (cathetusAdjacent < cathetusOpposite) return cathetusAdjacent;
            	else return cathetusOpposite;
            	
            }
            
            @Override
            public void run() {
                while(true) {

                    Platform.runLater(new Runnable() {
                        public void run() {

                        	//space.write("Compass", (float) space.read("Compass") + 1);

                        	
                        	calculatePosition();
                        	space.write("PositionX", x);
                        	space.write("PositionY", y);
                        	
                        	float[] sonarArray = new float[12];
                        	for (int i = 0; i < 12; i++) {
                        		sonarArray[i] = calculateDistance(i*30);
                        	}
                        	space.write("Sonar", sonarArray);
                        	//System.out.println(Arrays.toString((float[]) space.read("Sonar")));
                        	
                        	if (((float) space.read("Forward") == 0)) lbForward.setText("Standing");
                        	else lbForward.setText("Forward");
                        	
                        	if (((Integer) space.read("Turn") == 0)) lbTurn.setText("None");
                        	else if (((Integer) space.read("Turn") == 1)) lbTurn.setText("Clockwise");
                        	else lbTurn.setText("Counterclockwise");
                        	
                        	if (layer == 3) paint3rdLayer();
                        	else paint();
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
