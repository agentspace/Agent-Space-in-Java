/**
*Class Agent2 extends class Agent and it is an agent with trigger.
*/
public class Agent2 extends Agent {

    public Agent2() {
        start();
    }

    void init() {
        setTrigger("a");
    }

    void sense_select_act() {
        int i = ((Integer) read("a", new Integer(-1))).intValue();
        System.out.println("Agent so spustacom cita " + i);
    }

}
