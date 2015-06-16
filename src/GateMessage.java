import org.simgrid.msg.Msg;
import org.simgrid.msg.Host;
import org.simgrid.msg.Task;
import org.simgrid.msg.HostFailureException;
import org.simgrid.msg.TaskCancelledException;
import org.simgrid.msg.TimeoutException;
import org.simgrid.msg.TransferFailureException;

public class GateMessage extends Task{
	public enum Type{
		GATE_CONNECT,
		GATE_START,
		GATE_PROGRESS,
		GATE_CONTINUE,
		GATE_STOP
	};
	
	public Type type;
	public Host issuerHost;
	public long particleNumber;

	/**
	 * Constructor, builds a new GATE_CONTINUE/GATE_STOP message
	 */
	public GateMessage(Type type){
		this(type, null, 0);
	}
	
	/**
	 * Constructor, builds a new GATE_START message
	 */
	public GateMessage(Type type, Host issuerHost) {
		this(type, issuerHost, 0);
	}
	/**
	 * Constructor, builds a new GATE_PROGRESS message
	 */
	public GateMessage(Type type, Host issuerHost, long particleNumber) {
		super(type.toString(), 1, 100);
		this.type = type;
		this.issuerHost = issuerHost;
		this.particleNumber = particleNumber;
	}
	
	public void execute() throws  HostFailureException,TaskCancelledException{
		super.execute();
	}

	public static GateMessage process(String mailbox) {
		GateMessage message = null;
		try {
			message = (GateMessage) Task.receive(mailbox);
		} catch (TransferFailureException | HostFailureException| TimeoutException e) {
			e.printStackTrace();
		}
		
		Msg.debug("Received a '" + message.type.toString() + "' message");
		
		// Simulate the cost of the local processing of the request.
		// Depends on the value set when the GateMessage was created
		try {
			message.execute();
		} catch (HostFailureException | TaskCancelledException e) {
			e.printStackTrace();
		}
		
		return message;
	}
	
	public void emit (String mailbox) {
		try{
			this.send(mailbox);
		} catch (TransferFailureException | HostFailureException| TimeoutException e) {
			Msg.error("Something went wrong when emitting a '" + this.type.toString() +"' message to '" + mailbox + "'");
			e.printStackTrace();
		}		
	}
}
