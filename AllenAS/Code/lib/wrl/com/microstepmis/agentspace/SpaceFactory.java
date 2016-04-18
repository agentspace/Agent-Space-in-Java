package wrl.com.microstepmis.agentspace;

import wrl.java.util.Map;
import wrl.java.util.HashMap;
import wrl.com.microstepmis.schd.Proxy;

/**
 * Space - prostredie pre agentov.
 *
 * @author $Andrej Lucny$
 * @version $Revision: 1.2 $
 * $Id: SpaceFactory.java,v 1.2 2005/01/31 10:04:57 matog Exp $
 *
 * (c) 2004 MicroStep-MIS  www.microstep-mis.com
 */

public class SpaceFactory {
	protected static final Map spaces = new HashMap();

	public static synchronized Space getInstance( String name ) {
		if( name == null ) name = Space.DEFAULT_SPACE_NAME;
		Space newspace = (Space) spaces.get( name );
		if( newspace == null ) {
			newspace = new LocalSpace( name );
			spaces.put( name, newspace );
		}				
		return newspace;
	}

	public static Space getInstance() {
		return getInstance( Space.DEFAULT_SPACE_NAME );
	}
	
	// main metoda kvoli pustaniu main metod space-ov
	public static void main( String[] args ) {
		Space space;
		if( args.length == 0 ) 
			space = getInstance( Space.DEFAULT_SPACE_NAME );
		else 
			space = getInstance( args[ 0 ] );
		if( space instanceof LocalSpace ) 
			((LocalSpace )space).main( new String[]{} );
	}
}
