package wrl.com.microstepmis.schd;

import wrl.java.util.Map;
import wrl.java.util.HashMap;
import wrl.java.util.Iterator;

public class ProxyMonitor {

	private Map proxies = new HashMap();

	public void setProxy (Proxy proxy) {
		int count = 0;
		if (proxies.containsKey(proxy)) count = ((Integer) proxies.get(proxy)).intValue();
		proxies.put(proxy,new Integer(count+1));
	}

	public Proxy getProxy () {
		Iterator it = proxies.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			int count = ((Integer) entry.getValue()).intValue();
			if (count > 0) {
				entry.setValue(new Integer(count-1));
				return (Proxy) entry.getKey();
			}
		}
		return null;
	}
	
	public boolean clear (Proxy proxy) {
		boolean waiting = false;
		if (proxies.containsKey(proxy)) {
			int count = ((Integer) proxies.get(proxy)).intValue();
			if (count > 0) waiting = true;
			proxies.put(proxy,new Integer(0));
		}
		return waiting;
	}

	public boolean clearAll () {
		Iterator it = proxies.entrySet().iterator();
		boolean waiting = false;
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			int count = ((Integer) entry.getValue()).intValue();
			if (count > 0) waiting = true;
			entry.setValue(new Integer(0));
		}
		return waiting;
	}

}