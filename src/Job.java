import org.simgrid.msg.Msg;
import org.simgrid.msg.MsgException;
import org.simgrid.msg.Process;
import org.simgrid.msg.Host;
import org.simgrid.msg.Task;

public abstract class Job extends Process{
	private String mailbox;
	private SE closeSE;

	protected void setMailbox(){
		this.mailbox = Integer.toString(this.getPID()) + "@" +
				getHost().getName();
	}

	public String getMailbox(){
		return this.mailbox;
	}
	
	public SE getCloseSE() {
		return closeSE;
	}

	public GateMessage getFrom (String mailbox) {
		GateMessage message = null;
		try {
			message = (GateMessage) Task.receive(mailbox);
			Msg.debug("Received a '" + message.getType().toString() + 
					"' message from " + mailbox);
			// Simulate the cost of the local processing of the request.
			// Depends on the value set when the Message was created
			message.execute();
		} catch (MsgException e) {
			e.printStackTrace();
		}

		return message;
	}

	public Job(Host host, String name, String[]args) {
		super(host,name,args);
		if (host.getProperty("closeSE")!=null)
			this.closeSE = 
				VIPSimulator.getSEbyName(host.getProperty("closeSE"));
	}

}
