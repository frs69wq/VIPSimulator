import org.simgrid.msg.Host;
import org.simgrid.msg.MsgException;

public class DefaultSE extends SE{
	// In a simulation deployment file, there is a single host identified as the
	// default Storage Element. The sole special behavior of this process is to 
	// set a global variable to the host that runs it.

	public DefaultSE(Host host, String name, String[] args) {
		super(host, name, args);
	}

	public void main(String[] args) throws MsgException {
		VIPServer.setDefaultSE(this);
		super.main(args);
	}
}
