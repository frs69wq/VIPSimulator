import org.simgrid.msg.Host;
import org.simgrid.msg.Msg;
import org.simgrid.msg.MsgException;
import org.simgrid.msg.Process;

public class Merge extends Process {
	private String mailbox;
//	private String closeSEName;

	private void setMailbox(){
		this.mailbox = Integer.toString(this.getPID()) + "@" +
				getHost().getName();
	}

	public String getMailbox(){
		return this.mailbox;
	}

	public Merge(Host host, String name, String[]args) {
		super(host,name,args);
//		this.closeSEName = host.getProperty("closeSE");
	}

	public void main(String[] args) throws MsgException {
		boolean stop = false;
		// Build the mailbox name from the PID and the host name. This might be 
		// useful to distinguish different Gate processes running on a same host
		setMailbox();

		if (args.length < 1) {
			Msg.info("Slave needs 1 argument (its number)");
			System.exit(1);
		}
		Msg.info("Register Merge on '" + mailbox + "'");
		// Use of some simulation magic here, every worker knows the mailbox of 
		// the VIP server
		GateMessage.sendTo("VIPServer",GateMessage.Type.MERGE_CONNECT);

		while (!stop){
			GateMessage message = GateMessage.getFrom(mailbox);

			switch(message.getType()){
			case MERGE_START:
				Msg.info("Processing Merge");

				// Ask the LFC for the list of files to merge
				Message.sendTo(VIPSimulator.getDefaultLFC(), 
						Message.Type.ASK_MERGE_LIST);
				Msg.info("asked for list of files to merge. waiting for reply from " + VIPSimulator.getDefaultLFC());
//				Message.getFrom(VIPSimulator.getDefaultLFC());
//				Msg.info("ack recv?");
				Process.sleep(5000);
				Msg.verb("Goodbye!");
				stop = true;
				break;
			default:
				break;
			}
		}
	}
}
