import java.util.Vector;

import org.simgrid.msg.Host;
import org.simgrid.msg.HostFailureException;
import org.simgrid.msg.Msg;
import org.simgrid.msg.Process;
import org.simgrid.msg.Task;


public abstract class GridService extends Process {
	protected String name;
	protected Vector<Process> mailboxes;

	protected String findAvailableMailbox(long retryAfter){
		while (true){
			for (Process listener: this.mailboxes){
				String mailbox = listener.getName();
				if (Task.listen(mailbox)){
					Msg.info("Send a message to : " + mailbox + 
							" which is listening");
					return mailbox;
				}
			}
			try {
				Msg.warn("All the listeners are busy. Wait for " + retryAfter +
						"ms and try again");
				Process.sleep(retryAfter);
			} catch (HostFailureException e) {
				e.printStackTrace();
			}
		}
	}

	public String getName() {
		return name;
	}

	public GridService(Host host, String name, String[]args) {
		super(host,name,args);
		this.name = getHost().getName();
		this.mailboxes = new Vector<Process>();
	}

	public void kill(){
		for (Process p : mailboxes)
			p.kill();
	}
}
