import org.simgrid.msg.Msg;
import org.simgrid.msg.Host;

public class DefaultLFC extends LFC {
	public DefaultLFC(Host host, String name, String[] args) {
		super(host, name, args);
	}
	
	public void main(String[] args) {
		VIPSimulator.defaultLFC = this.getHostName();
		Msg.info("Default LFC is \""+ VIPSimulator.defaultLFC + "\""); 
		super.main(args);
	}
}
