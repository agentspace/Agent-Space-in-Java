/**
*Class Agent1 extends class Agent and it is an agent with timer.
*/
public class Agent1 extends Agent {

    private int i = 0;

    public Agent1() {
        start();
    }
 
    void init() {
        setTimer(2000);
    }

    void sense_select_act() {
        write("a", new Integer(++i));
        System.out.println("Agent s casovacom zapisuje " + i);
    }

}

