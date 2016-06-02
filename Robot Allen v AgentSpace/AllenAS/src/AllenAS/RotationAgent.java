package AllenAS;

import wrl.com.microstepmis.agentspace.Agent;
/**
 * Class RotationAgent extends class Agent of AgentSpace architecture.
 * Its task is to change the information in block Compass when there is information 
 * to turn the robot in block Turn. 
 */
public class RotationAgent extends Agent {

    public RotationAgent() {
    	
        attachTimer(250);
        delay(1000);
    }

    @Override
    public void senseSelectAct() {
    	if (AllenAS.layer > 0) {
    		Integer turn = (Integer) space.read("Turn");
    		float compass = (float) space.read("Compass");
    		if (turn != 0) {
    			if ((float) space.read("Forward") == 0) AllenAS.layer1Active = 1;
    			if (turn == -1) {
    				space.write("Compass", (float) compass-1*AllenAS.speed);
    			}
    			else {
    				space.write("Compass", (float) compass+1*AllenAS.speed);
    			}
    		}
    	}
    }
}
