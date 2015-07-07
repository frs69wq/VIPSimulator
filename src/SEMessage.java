import org.simgrid.msg.Msg;
import org.simgrid.msg.Task;
import org.simgrid.msg.MsgException;
import org.simgrid.msg.HostFailureException;
import org.simgrid.msg.TaskCancelledException;

public class SEMessage extends Task {
	public enum Type{
		DOWNLOAD_REQUEST,
		SEND_FILE,
		UPLOAD_FILE,
		UPLOAD_ACK
	};

	private Type type;
	private String logicalName = null;
	private long size = 0;

	public Type getType() {
		return type;
	}

	public String getSenderMailbox(){
		return getSender().getPID()+ "@" + getSource().getName();
	}

	public String getLogicalName() {
		return logicalName;
	}

	public long getSize() {
		return size;
	}

	public SEMessage(Type type, String logicalName, long size) {
		super(type.toString(), 1e6, 100);
		this.type = type;
		this.logicalName=logicalName;
		this.size=size;
	}

	/**
	 * Constructor, builds a new UPLOAD_FILE/SEND_FILE message
	 */
	public SEMessage(Type type, long size){
		super(type.toString(), 0, size);
		this.type = type;
		this.size = size;
	}

	public void execute() throws  HostFailureException,TaskCancelledException{
		super.execute();
	}

	public static SEMessage getFrom (String mailbox) {
		SEMessage message = null;
		try {
			message = (SEMessage) Task.receive("return-"+mailbox);
			Msg.debug("Received a '" + message.type.toString() + 
					"' message from " + mailbox);
			// Simulate the cost of the local processing of the request.
			// Depends on the value set when the Message was created
			message.execute();
		} catch (MsgException e) {
			e.printStackTrace();
		}

		return message;
	}

	public static void sendTo (String destination, Type type, 
			String logicalName, long size) {
		SEMessage m = (logicalName == null) ? 
				new SEMessage (type, size) : 
				new SEMessage (type, logicalName, size);
		try{
			Msg.debug("Send a '" + type.toString() + "' message to " +
					destination);
			m.send(destination);
		} catch (MsgException e) {
			Msg.error("Something went wrong when emitting a '" + 
				type.toString() +"' message to '" + destination + "'");
			e.printStackTrace();
		}
	}

	/**
	 * Specialized send of a UPLOAD_ACK message
	 */
	public static void sendTo (String destination, Type type) {
		sendTo(destination, type, type.toString(), 0);
	}

	/**
	 *  Specialized send of a UPLOAD_FILE/SEND_FILE message
	 */
	public static void sendTo (String destination, Type type, long size) {
		sendTo(destination, type, null, size);
	}
}
