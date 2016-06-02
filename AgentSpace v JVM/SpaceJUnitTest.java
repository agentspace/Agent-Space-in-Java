
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SpaceJUnitTest {

    @Test
    public void test() {

        assertNotNull(Space.getInstance());

        Agent1 a1 = new Agent1();
        Agent2 a2 = new Agent2();
        Agent2 a22 = new Agent2();
        Agent2 a222 = new Agent2();

        assertNotNull(a1);
        assertNotNull(a2);

        // Test waits 4100ms
        try {
            Thread.sleep(4100);
        } catch (InterruptedException ex) {
            Logger.getLogger(SpaceJUnitTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Block with mask "a" has 3 agents attached to it.
        assertEquals(3, Space.getInstance().t.get("a").size());

        // Agent1 has timer set to 2000ms so now there should be value of 2
        // stored in block with mask "a".
        assertEquals(2, Space.getInstance().h.get("a"));

        // Test waits 2000ms
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(SpaceJUnitTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        // Now there should be value of 3 stored in block with mask "a".
        assertEquals(3, Space.getInstance().h.get("a"));

    }

}
