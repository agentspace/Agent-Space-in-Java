package wrl.com.microstepmis.agentspace;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.2 $
 *
 * (c) 2004 MicroStep-MIS  www.microstep-mis.com
 */

public class BlockStatusComparator implements wrl.java.util.Comparator {

	public int compare (Object obj1, Object obj2) {
		BlockStatus st1 = (BlockStatus) obj1;
		BlockStatus st2 = (BlockStatus) obj2;
		return st1.compareTo(st2);
	}

}