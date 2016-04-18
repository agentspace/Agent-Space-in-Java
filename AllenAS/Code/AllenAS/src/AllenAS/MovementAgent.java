package AllenAS;

import java.util.Random;

import wrl.com.microstepmis.agentspace.Agent;

public class MovementAgent extends Agent {
	
     
    public MovementAgent() {
    	
        attachTimer(250);
        delay(1000);
    }
    /*
    @Override
    public void init(String[] args) {
    	this.AllenCanvas = (AllenAS.AllenCanvas)(Object)args[0];
    	System.out.println("AllenCanvas v Agentovi: "+AllenCanvas);
    }*/

    @Override
    public void senseSelectAct() {

        float[] sonar = (float[]) space.read("Sonar");
        float compass = (float) space.read("Compass");
        Random rnd = new Random();
        
    	for (int i = 0; i < 12; i++) {
    		// find an obstacle in current heading and its nearest neighbours in sonar
    		if (i == 0) {
    			if ((sonar[i] < (float) 50 && compass >= 30*i && compass <= 30*(i+1)) ||
    					(sonar[1] < (float) 50 && compass >= 30*1 && compass <= 30*(2))	||
    						(sonar[11] < (float) 50 && compass >= 30*11 && compass <= 30*(12))) {
    				decideTurn(sonar, compass);
    			}
    		}
    		if (i == 11) {
    			if ((sonar[i] < (float) 50 && compass >= 30*i && compass <= 30*(i+1)) ||
    					(sonar[0] < (float) 50 && compass >= 30*0 && compass <= 30*1)	||
    						(sonar[10] < (float) 50 && compass >= 30*10 && compass <= 30*11)) {
    				decideTurn(sonar, compass);
    			}    			
    		}
    		if (i != 0 && i != 11) {
    			if (sonar[i] < (float) 50 && compass >= 30*i && compass <= 30*(i+1) ||
    				(sonar[i-1] < (float) 50 && compass >= 30*(i-1) && compass <= 30*i) ||
    					(sonar[i+1] < (float) 50 && compass >= 30*(i+1) && compass <= 30*(i+2))) {
    						decideTurn(sonar, compass);
    			}
    		}
    		
    		checkForWay(i);
    	}
    	// random wandering
    	if (AllenAS.layer > 1 && (float) space.read("Forward") == 1) {
    		if (rnd.nextInt(5) == 0) {
    			if ((Integer) space.read("Turn") == 0) {
    				if (rnd.nextInt(2) == 0) space.write("Turn", new Integer(-1));
    				else space.write("Turn", new Integer(1));
    			}
    			else {
    				if (rnd.nextInt(5) == 0) space.write("Turn", (Integer) space.read("Turn")*-1);
    			}
    		}
    	}
    }
    // decides whether it is more advantageous to turn clockwise or counterclockwise
    public void decideTurn(float[] sonar, float compass) {
		space.write("Forward", (float) 0);
		if ((Integer) space.read("Turn") == 0) {
			float x = (float) space.read("PositionX");
			float y = (float) space.read("PositionY");
			
			if (compass >= 270 && compass <= 360) {
				if (x > 635) space.write("Turn", new Integer(-1));
				else space.write("Turn", (Integer) 1);
			}
			if (compass >= 0 && compass <= 90) {
				if (x > 635) space.write("Turn", (Integer) 1);
				else space.write("Turn", new Integer(-1));
			}
			if (compass >= 90 && compass <= 180) {
				if (x < 165) space.write("Turn", new Integer(-1));
				else space.write("Turn", (Integer) 1);
			}
			if (compass >= 180 && compass <= 270) {
				if (x < 165) space.write("Turn", (Integer) 1);
				else space.write("Turn", new Integer(-1));
			}
		}
    }
    
    // checks if a way in current heading is available
    public void checkForWay(int i) {
        float[] sonar = (float[]) space.read("Sonar");
        float compass = (float) space.read("Compass");
		if (sonar[i] > (float) 150 && compass >= 30*i && compass <= 30*(i+1)) {
			if (i == 0) {
				if (sonar[i+1] > (float) 150 && sonar[11] > (float) 150) {
					space.write("Forward", (float) 1);
					space.write("Turn", (Integer) 0);
				}
			}
			else if (i == 11) {
				if (sonar[i-1] > (float) 150 && sonar[0] > (float) 150) {
					space.write("Forward", (float) 1);
					space.write("Turn", (Integer) 0);
				}
			}
			else if (sonar[i+1] > (float) 150 && sonar[i-1] > (float) 150) {
				
				space.write("Forward", (float) 1);
				space.write("Turn", (Integer) 0);
			}
		}
    }
}
