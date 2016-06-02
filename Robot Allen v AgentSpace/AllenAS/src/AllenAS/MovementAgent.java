package AllenAS;

import java.util.Random;

import wrl.com.microstepmis.agentspace.Agent;
/**
 * Class MovementAgent extends class Agent of AgentSpace architecture.
 * Its task is to communicate between application and space and based on
 * active layers control the movement of agent. 
 */
public class MovementAgent extends Agent {
	
     
    public MovementAgent() {
    	
        attachTimer(250);
        delay(1000);
    }

    @Override
    public void senseSelectAct() {

        evaluateSituation();
        if (AllenAS.layer > 1 && (float) space.read("Forward") == 1) {
        	wander();
        	if (AllenAS.layer > 2) {
        		navigate();
        	}
        }
    }
	// find an obstacle in current heading and its nearest neighbours in sonar
	private void evaluateSituation() {
        float[] sonar = (float[]) space.read("Sonar");
        float compass = (float) space.read("Compass");
		for (int i = 0; i < 12; i++) {
			if (i == 0) {
				if ((sonar[i] < (float) 50 && compass >= 30 * i && compass <= 30 * (i + 1))
						|| (sonar[1] < (float) 50 && compass >= 30 * 1 && compass <= 30 * (2))
						|| (sonar[11] < (float) 50 && compass >= 30 * 11 && compass <= 30 * (12))) {
					decideTurn(sonar, compass);
				}
			}
			if (i == 11) {
				if ((sonar[i] < (float) 50 && compass >= 30 * i && compass <= 30 * (i + 1))
						|| (sonar[0] < (float) 50 && compass >= 30 * 0 && compass <= 30 * 1)
						|| (sonar[10] < (float) 50 && compass >= 30 * 10 && compass <= 30 * 11)) {
					decideTurn(sonar, compass);
				}
			}
			if (i != 0 && i != 11) {
				if ((sonar[i] < (float) 50 && compass >= 30 * i && compass <= 30 * (i + 1))
						|| (sonar[i - 1] < (float) 50
								&& compass >= 30 * (i - 1) && compass <= 30 * i)
						|| (sonar[i + 1] < (float) 50 && compass >= 30 * (i) && compass <= 30 * (i + 1))) {
					decideTurn(sonar, compass);
				}
			}
			checkForWay(i);
		}
	}
    // decides whether it is more advantageous to turn clockwise or counterclockwise
    private void decideTurn(float[] sonar, float compass) {
		space.write("Forward", (float) 0);
		AllenAS.layer0Active = 1;
		AllenAS.layer2Active = 0;
		AllenAS.layer3Active = 0;
		if (AllenAS.layer > 0) {
			if (true) {
				float x = (float) space.read("PositionX");
				float y = (float) space.read("PositionY");

				if (compass >= 270 && compass <= 360) {
					if (x > 620)
						space.write("Turn", new Integer(-1));
					else
						space.write("Turn", (Integer) 1);
				}
				if (compass >= 0 && compass <= 90) {
					if (x > 620)
						space.write("Turn", (Integer) 1);
					else
						space.write("Turn", new Integer(-1));
				}
				if (compass >= 90 && compass <= 180) {
					if (x < 180)
						space.write("Turn", new Integer(-1));
					else
						space.write("Turn", (Integer) 1);
				}
				if (compass >= 180 && compass <= 270) {
					if (x < 180)
						space.write("Turn", (Integer) 1);
					else
						space.write("Turn", new Integer(-1));
				}
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
					resetLayers();
				}
			}
			else if (i == 11) {
				if (sonar[i-1] > (float) 150 && sonar[0] > (float) 150) {
					resetLayers();
				}
			}
			else if (sonar[i+1] > (float) 150 && sonar[i-1] > (float) 150) {
				resetLayers();
			}
		}
    }

	private void resetLayers() {
		space.write("Forward", (float) 1);
		space.write("Turn", (Integer) 0);
		AllenAS.layer0Active = 0;
		AllenAS.layer1Active = 0;
		AllenAS.layer2Active = 0;
		AllenAS.layer3Active = 0;
	}
    // random wandering
	private void wander() {
		Random rnd = new Random();
		if (rnd.nextInt(5) == 0) {
			if ((Integer) space.read("Turn") == 0) {
				AllenAS.layer2Active = 1;
				if (rnd.nextInt(2) == 0)
					space.write("Turn", new Integer(-1));
				else
					space.write("Turn", new Integer(1));
			} else {
				if (rnd.nextInt(5) == 0 && AllenAS.layer == 2)
					space.write("Turn", (Integer) space.read("Turn") * -1);
			}
		}

	}
    
    // navigating
	private void navigate() {
		Random rnd = new Random();
		float x = (float) space.read("PositionX");
		float y = (float) space.read("PositionY");
		float cmps = (float) space.read("Compass");
		if (rnd.nextInt(5) == 0 && AllenAS.layer3Found != 1) {
			AllenAS.layer3Active = 1;
			if (cmps > 270 && cmps < 360) {
				space.write("Turn", new Integer(-1));
			}
			if (cmps > 0 && cmps < 90) {
				space.write("Turn", new Integer(1));
			}
			if (y > 250) {
				if (cmps > 90 && cmps < 235) {
					space.write("Turn", new Integer(1));
				}
				if (cmps > 235 && cmps < 290) {
					space.write("Turn", new Integer(-1));
				}
			} else {
				if (cmps > 145 && cmps < 270) {
					space.write("Turn", new Integer(-1));
				}
				if (cmps > 90 && cmps < 145) {
					space.write("Turn", new Integer(1));
				}
			}
		}
		// stop on find
		if ((x > 130) && (x < 180) && (y > 220) && (y < 280)) {
			AllenAS.layer3Found = 1;
			AllenAS.layer1Active = 0;
			AllenAS.layer2Active = 0;
			space.write("Turn", new Integer(0));
			space.write("Forward", new Float(0));
		}
	}
}
