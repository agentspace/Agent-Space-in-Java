package wrl.java.util;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.2 $
 *
 * (c) 2004 MicroStep-MIS  www.microstep-mis.com
 */

public class IntegerComparator implements Comparator {

	public int compare (Object obj1, Object obj2) {
		Integer i1 = (Integer) obj1;
		Integer i2 = (Integer) obj2;
		int k1 = i1.intValue();
		int k2 = i2.intValue();
		if (k1 == k2) return 0;
		else if (k1 < k2) return -1;
		else return 1;
	}

}