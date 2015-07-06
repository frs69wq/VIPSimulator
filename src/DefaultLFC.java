import org.simgrid.msg.Host;
import org.simgrid.msg.MsgException;

public class DefaultLFC extends LFC {
	// In a simulation deployment file, there is a single host identified as the
	// default Logical File Catalog service. 
	// The sole special behavior of this process is to set a global variable to
	// the name of the host that runs it.
	public DefaultLFC(Host host, String name, String[] args) {
		super(host, name, args);
	}

	public void main(String[] args) throws MsgException {
		VIPSimulator.setDefaultLFC(this);
		super.main(args);
	}
}
