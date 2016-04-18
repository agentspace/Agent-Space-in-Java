package wrl.com.microstepmis.agentspace;

import java.util.Date;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.2 $
 *
 * (c) 2004 MicroStep-MIS  www.microstep-mis.com
 */
public class BlockStatus {
	public String name;
	public Object value;
	public long validFrom;
	public long validTo;

	/**
	 * Creates a new BlockStatus object.
	 *
	 * @param name DOCUMENT ME!
	 * @param value DOCUMENT ME!
	 * @param validFrom DOCUMENT ME!
	 * @param validTo DOCUMENT ME!
	 */
	public BlockStatus(String name, Object value, long validFrom, long validTo) {
		this.name = name;
		this.value = value;
		this.validFrom = validFrom;
		this.validTo = validTo;
	}

	/**
	 * Creates a new BlockStatus object.
	 */
	public BlockStatus() {
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public String getName() {
		return name;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public long getValidFrom() {
		return validFrom;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public long getValidTo() {
		return validTo;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public long getValidFor() {
		if (validTo == Block.FOREVER) {
			return (Block.FOREVER);
		}

		return (validTo - System.currentTimeMillis());
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public boolean isEmpty() {
		return (value == Block.EMPTY);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public String toString() {
		String ret = name + " valid";

		if (validFrom != Block.NOW) {
			ret += (" from " + new Date(validFrom));
		}

		if (validTo == Block.FOREVER) {
			ret += " forever";
		} else {
			ret += (" to " + new Date(validTo));
		}

		ret += (" value: " + value);

		return ret;
	}
	
	public int compareTo (Object obj) {
		BlockStatus st = (BlockStatus) obj;
		return name.compareTo(st.name);
	}
	
}
