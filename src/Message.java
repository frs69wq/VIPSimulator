import org.simgrid.msg.Msg;
import org.simgrid.msg.Task;
import org.simgrid.msg.MsgException;
import org.simgrid.msg.HostFailureException;
import org.simgrid.msg.TaskCancelledException;

public class Message extends Task {
	public enum Type{
		// Control messages
		ASK_LOGICAL_FILE,
		SEND_LOGICAL_FILE,
		REGISTER_FILE,
		DOWNLOAD_REQUEST,
		ASK_MERGE_LIST,
		// Data transfers
		SEND_FILE,
		UPLOAD_FILE,
		// ACK and Signal
		REGISTER_ACK,
		UPLOAD_ACK,
		FINALIZE
	};

	private Type type;
	private String logicalFileName = null;
	private long logicalFileSize = 0;
	private LogicalFile file = null;

	public Type getType() {
		return type;
	}

	public String getSenderMailbox(){
		return getSender().getPID()+ "@" + getSource().getName();
	}

	public String getLogicalFileName() {
		return logicalFileName;
	}

	public long getLogicalFileSize() {
		return logicalFileSize;
	}

	public LogicalFile getFile() {
		return file;
	}

	public Message(Type type, String logicalFileName,
			long logicalFileSize, LogicalFile file) {
		super(type.toString(), 1e6, 100);
		this.type = type;
		this.logicalFileName=logicalFileName;
		this.logicalFileSize = logicalFileSize;
		this.file =file;
	}

	/**
	 * Constructor, builds a new UPLOAD_REQUEST/SEND_FILE message
	 */
	public Message(Type type, long logicalFileSize){
		super(type.toString(), 0, logicalFileSize);
		this.type = type;
		this.logicalFileSize = logicalFileSize;
	}

	public void execute() throws  HostFailureException,TaskCancelledException{
		super.execute();
	}

	public static Message getFrom (String mailbox) {
		Message message = null;
		try {
			message = (Message) Task.receive(mailbox);
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
			String logicalFileName, long logicalFileSize, LogicalFile file) {
		Message m = new Message (type, logicalFileName, logicalFileSize, file);
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
	 * Specialized send of a FINALIZE/UPLOAD_ACK/REGISTER_ACK message
	 */
	public static void sendTo (String destination, Type type) {
		sendTo(destination, type, null, 0, null);
	}

	/**
	 *  Specialized send of a REGISTER_FILE/SEND_LOGICAL_FILE message
	 */
	public static void sendTo (String destination, Type type, 
			LogicalFile file) {
		sendTo(destination, type, null, 0, file);
	}

	/**
	 * Specialized send of a ASK_LOGICAL_FILE message
	 */
	public static void sendTo (String destination, Type type, 
			String logicalFileName) {
		sendTo(destination, type, logicalFileName, 0, null);
	}

	/**
	 *  Specialized send of a DOWNLOAD_REQUEST message
	 */
	public static void sendTo (String destination, Type type, 
			String logicalFileName, long logicalFileSize) {
		sendTo(destination, type, logicalFileName, logicalFileSize, null);
	}

	public static void sendAsynchronouslyTo (String destination, Type type, 
			long payload) {
		Message m = new Message (type, payload);
		m.isend(destination);
	}
}
