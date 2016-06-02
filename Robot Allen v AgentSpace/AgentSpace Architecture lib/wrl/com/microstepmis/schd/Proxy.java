package wrl.com.microstepmis.schd;

public class Proxy {

	private ProxyMonitor monitor;
	private boolean disposed;
	private Thread owner;
	private long timestamp = 0;

	public Proxy(Thread owner, ProxyMonitor monitor) {
		this.owner = owner;
		this.monitor = monitor;
		disposed = false;
	}

	public Proxy(Thread owner) {
		this(owner,new ProxyMonitor());
	}

	public Proxy() {
		this(null,new ProxyMonitor());
	}

	public Proxy receive () {
		Proxy ret = monitor.getProxy();
		if (ret != null) return ret;
		synchronized (monitor) {
			try {
				monitor.wait();
				ret = monitor.getProxy();
			}	catch (InterruptedException e) {
			}
		}
		return ret;
	}
	
	public boolean trigger (long timestamp) {
		this.timestamp = timestamp;
		if (disposed) return false;
		if (owner != null)
			if (!owner.isAlive()) {
				disposed = true;
				return false;
			}
		synchronized (monitor) {
			monitor.setProxy(this);
			monitor.notifyAll();
		}
		return true;
	}
	
	public boolean isTriggerable () {
		if (owner != null) 
			if (!owner.isAlive()) {
				disposed = true;
				return false;
			}
		return true;
	}

	public boolean creceive () {
		return monitor.clear(this);
	}

	public boolean creceiveAll () {
		return monitor.clearAll();
	}
	
	public long getTimestamp () {
		return timestamp;
	}

	public void dispose() {
		disposed = true;
	}

	protected void finalize () throws Throwable {
		System.out.println("proxy finalized");
		super.finalize();
	}

}