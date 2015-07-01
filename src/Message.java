import org.simgrid.msg.Msg;
import org.simgrid.msg.Task;
import org.simgrid.msg.MsgException;
import org.simgrid.msg.HostFailureException;
import org.simgrid.msg.TaskCancelledException;

public class Message extends Task {
	public enum Type{
		CR_INPUT,
		ASK_LOGICAL_FILE,
		SEND_LOGICAL_FILE,
		REGISTER_FILE,
		REGISTER_ACK,
		DOWNLOAD_REQUEST,
		SEND_FILE,
		UPLOAD_REQUEST,
		UPLOAD_ACK,
		FINALIZE
	};

	private Type type;
	private String logicalFileName = null;
	private long logicalFileSize = 0;
	private LogicalFile file = null;

	private void safeSend (String destination){
		try{
			this.send(destination);
		} catch (MsgException e) {
			Msg.error("Something went wrong when emitting a '" + 
				type.toString() +"' message to '" + destination + "'");
			e.printStackTrace();
		}
	}

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

	/**
	 * constructor, builds a new FINALIZE/UPLOAD_ACK/REGISTER_ACK message
	 */
	public Message(Type type) {
		super(type.toString(), 1, 100);
		this.type = type;
	}
	
	/**
	 * Constructor, builds a new DOWNLOAD_REQUEST message
	 */
	public Message(Type type, String logicalFileName,
			long logicalFileSize) {
		super(type.toString(), 1e6, 100);
		this.type = type;
		this.logicalFileName=logicalFileName;
		this.logicalFileSize = logicalFileSize;
	}

	/**
	 * Constructor, builds a new ASK_LOGICAL_FILE message
	 */
	public Message(Type type, String logicalFileName) {
		super(type.toString(), 1e6, 100);
		this.type = type;
		this.logicalFileName=logicalFileName;
	}


	/**
	 * Constructor, builds a new UPLOAD_REQUEST/SEND_FILE message
	 */
	public Message(Type type, long logicalFileSize){
		super(type.toString(), 0, logicalFileSize);
		this.type = type;
		this.logicalFileSize = logicalFileSize;
	}

	/**
	 * Constructor, builds a new CR_INPUT/REGISTER_FILE/SEND_LOGICAL_FILE 
	 * message
	 */
	public Message(Type type, LogicalFile file) {
		// Assume that 1e6 flops are needed on receiving side to process a 
		// request 
		// Assume that a request corresponds to 100 Bytes 
		//TODO provide different computing and communication values depending
		// on the type of message
		super (type.toString(), 1e6, 100);
		this.type = type;
		this.file = file;
	}

	public void execute() throws  HostFailureException,TaskCancelledException{
		super.execute();
	}

	public static Message getFrom (String mailbox) {
		Message message = null;
		try {
			message = (Message) Task.receive(mailbox);
			Msg.debug("Received a '" + message.type.toString() + "' message");
			// Simulate the cost of the local processing of the request.
			// Depends on the value set when the Message was created
			message.execute();
		} catch (MsgException e) {
			e.printStackTrace();
		}

		return message;
	}

	public static void sendTo (String destination, Type type) {
		Message m = new Message(type);
		m.safeSend(destination);
	}
	
	public static void sendTo (String destination, Type type, 
			LogicalFile file) {
		Message m = new Message (type, file);
		m.safeSend(destination);
	}

	public static void sendTo (String destination, Type type, 
			String logicalFileName) {
		Message m = new Message (type, logicalFileName);
		m.safeSend(destination);
	}

	public static void sendTo (String destination, Type type, 
			String logicalFileName, long logicalFileSize) {
		Message m = new Message (type, logicalFileName, logicalFileSize);
		m.safeSend(destination);
	}

	public static void sendAsynchronouslyTo (String destination, Type type, 
			long payload) {
		Message m = new Message (type, payload);
		m.isend(destination);
	}
}
