import org.simgrid.msg.Msg;
import org.simgrid.msg.Task;
import org.simgrid.msg.MsgException;
import org.simgrid.msg.HostFailureException;
import org.simgrid.msg.TaskCancelledException;

public class GateMessage extends Task{
	public enum Type{
		GATE_CONNECT,
		GATE_START,
		GATE_PROGRESS,
		GATE_CONTINUE,
		GATE_STOP,
		GATE_END,
		MERGE_CONNECT,
		MERGE_START
	};

	private Type type;
	private long particleNumber;

	public String getSenderMailbox(){
		return getSender().getPID()+ "@" + getSource().getName();
	}

	public Type getType() {
		return type;
	}

	public long getParticleNumber() {
		return particleNumber;
	}

	public GateMessage(Type type, long particleNumber) {
		super(type.toString(), 1, 100);
		this.type = type;
		this.particleNumber = particleNumber;
	}

	public void execute() throws  HostFailureException,TaskCancelledException{
		super.execute();
	}

	public static GateMessage getFrom(String mailbox) {
		GateMessage message = null;
		try {
			message = (GateMessage) Task.receive(mailbox);
			Msg.debug("Received a '" + message.type.toString() + "' message");
			// Simulate the cost of the local processing of the request.
			// Depends on the value set when the GateMessage was created
			message.execute();
		} catch (MsgException e) {
			e.printStackTrace();
		}
		return message;
	}

	public static void sendTo(String destination, Type type) {
		sendTo(destination,type, 0);
	}

	public static void sendTo(String destination, Type type, 
			long particleNumber) {
		GateMessage m = new GateMessage(type, particleNumber);
		try{
			m.send(destination);
		} catch (MsgException e) {
			Msg.error("Something went wrong when emitting a '" +
				type.toString() +"' message to '" + destination + "'");
			e.printStackTrace();
		}
	}
}
