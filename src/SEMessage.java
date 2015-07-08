import org.simgrid.msg.Msg;
import org.simgrid.msg.Task;
import org.simgrid.msg.MsgException;
import org.simgrid.msg.HostFailureException;
import org.simgrid.msg.TaskCancelledException;

public class SEMessage extends Task {
	public enum Type{
		DOWNLOAD_REQUEST,
		FILE_TRANSFER,
		UPLOAD_ACK
	};

	private Type type;
	private String fileName = null;
	private long size = 0;

	/**
	 * Constructor, builds a new control (DOWNLOAD_REQUEST/UPLOAD_ACK) message
	 */
	private SEMessage(Type type, String fileName, long size) {
		super(type.toString(), 1e6, 100);
		this.type = type;
		this.fileName=fileName;
		this.size=size;
	}

	/**
	 * Constructor, builds a new FILE_TRANSFER message
	 */
	private SEMessage(Type type, long size){
		super(type.toString(), 0, size);
		this.type = type;
		this.size = size;
	}

	public Type getType() {
		return type;
	}

	public String getSenderName(){
		return getSender().getPID()+ "@" + getSource().getName();
	}

	public String getFileName() {
		return fileName;
	}

	public long getSize() {
		return size;
	}
	
	public void execute() throws  HostFailureException, TaskCancelledException{
		super.execute();
	}

	public static void sendTo (String mailbox, Type type, String fileName, 
			long size) {
		SEMessage message = (fileName == null) ? 
				new SEMessage (type, size) : 
				new SEMessage (type, fileName, size);
		try{
			Msg.debug("Send a '" + type.toString() + "' message to " + mailbox);
			message.send(mailbox);
		} catch (MsgException e) {
			Msg.error("Something went wrong when emitting a '" + 
				type.toString() +"' message to '" + mailbox + "'");
			e.printStackTrace();
		}
	}
}
