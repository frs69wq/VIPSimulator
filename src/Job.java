import org.simgrid.msg.Msg;
import org.simgrid.msg.MsgException;
import org.simgrid.msg.Process;
import org.simgrid.msg.Host;
import org.simgrid.msg.Task;

public abstract class Job extends Process{
	private String mailbox;
	private SE closeSE;

	protected void setMailbox(){
		mailbox = Integer.toString(this.getPID()) + "@" +
				getHost().getName();
	}

	public String getMailbox(){
		return mailbox;
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

	public void begin(){
		Msg.debug("Sending a 'START' message to '" + mailbox +"'");
		GateMessage.sendTo(mailbox, Message.Type.START, 0);
	}

	public void carryOn (){
		Msg.debug("Sending a 'CARRY_ON' message to '" + mailbox +"'");
		GateMessage.sendTo(mailbox, Message.Type.CARRY_ON, 0);
	}

	public void end(){
		Msg.debug("Sending a 'END' message to '" + mailbox +"'");
		GateMessage.sendTo(mailbox, Message.Type.STOP, 0);
	}

	public Job(Host host, String name, String[]args) {
		super(host,name,args);
		if (host.getProperty("closeSE")!=null)
			this.closeSE = 
				VIPSimulator.getSEbyName(host.getProperty("closeSE"));
	}

}
