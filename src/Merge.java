import java.util.Vector;

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

				Vector<String> fileNameList = 
						LCG.ls(mailbox,"results/",VIPSimulator.getDefaultLFC());

				Msg.info("Files to merge:" + fileNameList.toString());
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
