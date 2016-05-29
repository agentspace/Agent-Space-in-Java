package wrl.com.microstepmis.agentspace;

import wrl.java.util.Comparator;
/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.2 $
 *
 * (c) 2004 MicroStep-MIS  www.microstep-mis.com
 */

public class ValidityFromComparator implements Comparator {

	public int compare (Object obj1, Object obj2) {
		Block b1 = (Block) obj1;
		Block b2 = (Block) obj2;
		if (b1.validFrom == b2.validFrom) return( b1.compareTo(b2));
		if (b1.validFrom == Block.NOW) return(-1);
		if (b2.validFrom == Block.NOW) return(1);
		if (b1.validFrom < b2.validFrom) return(-1);
		else return(1);
	}

}