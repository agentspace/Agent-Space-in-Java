package wrl.com.microstepmis.agentspace;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.2 $
 *
 * (c) 2004 MicroStep-MIS  www.microstep-mis.com
 */

public class BlockComparator implements wrl.java.util.Comparator {

	public int compare (Object obj1, Object obj2) {
		Block b1 = (Block) obj1;
		Block b2 = (Block) obj2;
		int ret = b1.compareTo(b2);
		return ret;
	}

}