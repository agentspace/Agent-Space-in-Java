package AllenAS;

import wrl.com.microstepmis.agentspace.Agent;

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
    			if (turn == -1) {
    				space.write("Compass", (float) compass-1*AllenAS.speed);
    				//System.out.println("otacam sa vlavo");
    			}
    			else {
    				space.write("Compass", (float) compass+1*AllenAS.speed);
    				//System.out.println("otacam sa vpravo");
    			}
    		}
    	}
    }
}
