package wrl.java.util;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.2 $
 *
 * (c) 2004 MicroStep-MIS  www.microstep-mis.com
 */

public class StringComparator implements Comparator {

	public int compare (Object obj1, Object obj2) {
		String s1 = (String) obj1;
		String s2 = (String) obj2;
		return s1.compareTo(s2);
	}

}