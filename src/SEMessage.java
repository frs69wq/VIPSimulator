import org.simgrid.msg.Msg;
import org.simgrid.msg.MsgException;

public class SEMessage extends Message {

	private long size = 0;

	public long getSize() {
		return size;
	}

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
