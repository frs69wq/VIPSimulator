import org.simgrid.msg.Host;
import org.simgrid.msg.HostFailureException;
import org.simgrid.msg.Msg;
import org.simgrid.msg.NativeException;
import org.simgrid.msg.TimeoutException;
import org.simgrid.msg.TransferFailureException;
import org.simgrid.msg.Process;

public class Merge extends Process {

	public Merge(Host host, String name, String[]args) {
		super(host,name,args);
	}

	public void main(String[] args) throws TransferFailureException, 
		HostFailureException, TimeoutException, NativeException {
		if (args.length < 1) {
			Msg.info("Slave needs 1 argument (its number)");
			System.exit(1);
		}
		GateMessage task = new GateMessage(GateMessage.Type.GATE_CONNECT, 
				getHost());
		Msg.info("Register Merge on "+ this.getHost().getName());
		task.send("VIPServer");
	}
}
