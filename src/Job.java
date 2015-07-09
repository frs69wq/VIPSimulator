import org.simgrid.msg.Msg;
import org.simgrid.msg.Process;
import org.simgrid.msg.Host;

public abstract class Job extends Process{
	private String name;
	private SE closeSE;

	protected void setName(){
		name = Integer.toString(this.getPID()) + "@" + getHost().getName();
	}

	public String getName(){
		return name;
	}
	
	public SE getCloseSE() {
		return closeSE;
	}

	public void begin(){
		Msg.debug("Sending a 'BEGIN' message to '" + name +"'");
		GateMessage.sendTo(name, Message.Type.BEGIN, 0);
	}

	public void carryOn (){
		Msg.debug("Sending a 'CARRY_ON' message to '" + name +"'");
		GateMessage.sendTo(name, Message.Type.CARRY_ON, 0);
	}

	public void end(){
		Msg.debug("Sending a 'END' message to '" + name +"'");
		GateMessage.sendTo(name, Message.Type.END, 0);
	}

	public Job(Host host, String name, String[]args) {
		super(host,name,args);
		if (host.getProperty("closeSE")!=null)
			this.closeSE = 
				VIPSimulator.getSEbyName(host.getProperty("closeSE"));
	}

}
