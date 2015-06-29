import org.simgrid.msg.Host;
import org.simgrid.msg.HostFailureException;
import org.simgrid.msg.Msg;
import org.simgrid.msg.NativeException;
import org.simgrid.msg.TimeoutException;
import org.simgrid.msg.TransferFailureException;
import org.simgrid.msg.Process;

public class Merge extends Process {
	private String mailbox;

	public Merge(Host host, String name, String[]args) {
		super(host,name,args);
	}
	
	private void setMailbox(){
		this.mailbox = Integer.toString(this.getPID()) + "@" +
				getHost().getName();
	}

	public String getMailbox(){
		return this.mailbox;
	}

	public void main(String[] args) throws TransferFailureException, 
		HostFailureException, TimeoutException, NativeException {
		// Build the mailbox name from the PID and the host name. This might be 
		// useful to distinguish different Gate processes running on a same host
		setMailbox();

		if (args.length < 1) {
			Msg.info("Slave needs 1 argument (its number)");
			System.exit(1);
		}
		GateMessage task = new GateMessage(GateMessage.Type.GATE_CONNECT, 
				getMailbox());
		Msg.info("Register Merge on "+ this.getMailbox());
		task.send("VIPServer");
	}
}
