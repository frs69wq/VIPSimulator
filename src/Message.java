import org.simgrid.msg.Msg;
import org.simgrid.msg.Host;
import org.simgrid.msg.NativeException;
import org.simgrid.msg.Task;
import org.simgrid.msg.HostFailureException;
import org.simgrid.msg.TaskCancelledException;
import org.simgrid.msg.TimeoutException;
import org.simgrid.msg.TransferFailureException;

public class Message extends Task {    	
	public enum Type{
		CR_INPUT,
		ASK_FILE_INFO,
		SEND_FILE_INFO,
		REGISTER_FILE,
		REGISTER_ACK,
		DOWNLOAD_REQUEST,
		SEND_FILE,
		UPLOAD_REQUEST,
		UPLOAD_ACK,
		FINALIZE
	};
	
	public Type type;
	public Host issuerHost;
	public String logicalFileName;
	public long logicalFileSize;
	public String SEName;
	
	/**
	 * Constructor, builds a new UPLOAD/DOWNLOAD_REQUEST message
	 */
	public Message(Type type, Host issuerHost, String logicalFileName, long logicalFileSize) {
		this(type, issuerHost, logicalFileName, logicalFileSize, null);
	}

	/**
     * Constructor, builds a new ASK_FILE_INFO message
     */
	public Message(Type type, Host issuerHost, String logicalFileName) {
		this(type, issuerHost, logicalFileName, 0, null);
	}
	
	/**
     * Constructor, builds a new SEND_FILE_INFO message
	 */
	public Message(Type type,  String SEName, long logicalFileSize) {
		this(type, null, null, logicalFileSize, SEName);
	}
	
	/**
	 * Constructor, builds a new FINALIZE/UPLOAD_ACK/REGISTER_ACK message
	 */
	public Message(Type type) {
		super(type.toString(), 1, 100);
		this.type = type;
		this.issuerHost = null;
		this.logicalFileName = null;
		this.logicalFileSize= 0; 
		this.SEName = null;
	}

	/**
     * Constructor, builds a new SEND_FILE message
    */
	public Message(Type type, long logicalFileSize){
		super(type.toString(), 0, logicalFileSize);
		this.type = type;
		this.issuerHost = null;
		this.logicalFileName = null;
		this.logicalFileSize= logicalFileSize;
		this.SEName =null;
	}

	/**
	 * Constructor, builds a new CR_INPUT/REGISTER_FILE message
	 */
	public Message(Type type, Host issuerHost, String logicalFileName, long logicalFileSize, String SEName) {
		// Assume that 1e6 flops are needed on receiving side to process a request 
		// Assume that a request corresponds to 100 Bytes 
		//TODO provide different computing and communication values depending on the type of message
		super (type.toString(), 1e6, 100);
		this.type = type;
		this.issuerHost = issuerHost;
		this.logicalFileName = logicalFileName;
		this.logicalFileSize= logicalFileSize; 
		this.SEName = SEName;
	}
	
	public void execute() throws  HostFailureException,TaskCancelledException{
		super.execute();
	}
	
	public static Message process (String mailbox) {
		Message message = null;
		try {
			message = (Message) Task.receive(mailbox);
		} catch (TransferFailureException | HostFailureException| TimeoutException e) {
			e.printStackTrace();
		}
		Msg.debug("Received a '" + message.type.toString() + "' message");
		// Simulate the cost of the local processing of the request.
		// Depends on the value set when the Message was created
		try {
			message.execute();
		} catch (HostFailureException | TaskCancelledException e) {
			Msg.error("Execution of a Message failed ...");
			e.printStackTrace();
		}
		return message;
	}
	
	public void emit (String mailbox) {
		try{
			this.send(mailbox);
		} catch (TransferFailureException | HostFailureException| TimeoutException | NativeException e) {
			Msg.error("Something went wrong when emitting a '" + this.type.toString() +"' message to '" + mailbox + "'");
			e.printStackTrace();
		}		
	}
}
