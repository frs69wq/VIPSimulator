import java.util.Vector;

import org.simgrid.msg.Host;
import org.simgrid.msg.Msg;
import org.simgrid.msg.MsgException;
import org.simgrid.msg.Process;

public class Merge extends Job {

	private void connect (){
		// Use of some simulation magic here, every worker knows the mailbox of 
		// the VIP server
		GateMessage.sendTo("VIPServer",Message.Type.MERGE_CONNECT, 0);
	}

	public Merge(Host host, String name, String[]args) {
		super(host,name,args);
	}

	public void main(String[] args) throws MsgException {
		boolean stop = false;
		// Build the mailbox name from the PID and the host name. This might be 
		// useful to distinguish different Gate processes running on a same host
		setName();

		if (args.length < 1) {
			Msg.info("Slave needs 1 argument (its number)");
			System.exit(1);
		}
		Msg.info("Register Merge on '" + getName() + "'");
		this.connect();

		while (!stop){
			GateMessage message = (GateMessage) Message.getFrom(getName());

			switch(message.getType()){
			case BEGIN:
				Msg.info("Processing Merge");

				Vector<String> fileNameList = 
						LCG.ls(VIPSimulator.getDefaultLFC(),"results/");

				Msg.info("Files to merge:" + fileNameList.toString());
				for (String fileName : fileNameList){
					LCG.cp(fileName, "/scratch/" + fileName, 
							VIPSimulator.getDefaultLFC());
				}
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
