// WIP WIP WIP
// put in here everything that is common between GATE and Merge Classes
// In the end I hope to make them inherits from Worker rather than Process.
import org.simgrid.msg.Process;
import org.simgrid.msg.Host;

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

	public Job(Host host, String name, String[]args) {
		super(host,name,args);
		this.closeSE = VIPSimulator.getSEbyName(host.getProperty("closeSE"));
	}

}
