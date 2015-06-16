import org.simgrid.msg.Msg;
import org.simgrid.msg.Host;

public class DefaultSE extends SE{

	public DefaultSE(Host host, String name, String[] args) {
		super(host, name, args);
	}
	
	public void main(String[] args) {
		VIPSimulator.defaultSE = hostName;
		Msg.info("Default SE is \""+ VIPSimulator.defaultSE+ "\""); 
		super.main(args);
	}
}
