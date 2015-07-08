import java.util.Vector;

import org.simgrid.msg.Msg;
import org.simgrid.msg.Task;
import org.simgrid.msg.MsgException;
import org.simgrid.msg.HostFailureException;
import org.simgrid.msg.TaskCancelledException;

public class LFCMessage extends Task {
	// this class of control messages is dedicated to the interactions with the 
	// Logical File Catalog(s) that happen only through lcg-utils functions.
	// These functions are called by the worker processes.
	public enum Type{
		ASK_LOGICAL_FILE,
		SEND_LOGICAL_FILE,
		REGISTER_FILE,
		REGISTER_ACK,
		ASK_LS,
		SEND_LS
	};

	private Type type;
	private String logicalName = null;
	private Vector<LogicalFile> fileList = null;

	public Type getType() {
		return type;
	}

	public String getSenderMailbox(){
		return getSender().getPID()+ "@" + getSource().getName();
	}

	public String getLogicalName() {
		return logicalName;
	}

	public LogicalFile getFile() {
		return fileList.firstElement();
	}

	public Vector<LogicalFile> getFileList() {
		return fileList;
	}

	public LFCMessage(Type type, String logicalName, 
			Vector<LogicalFile> files) {
		super(type.toString(), 1e6, 100);
		this.type = type;
		this.logicalName=logicalName;
		this.fileList = files;
	}

	public void execute() throws  HostFailureException,TaskCancelledException{
		super.execute();
	}

	public static LFCMessage getFrom (String mailbox) {
		LFCMessage message = null;
		try {
			message = (LFCMessage) Task.receive(mailbox);
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
			String logicalName, Vector<LogicalFile> fileList) {
		LFCMessage m = new LFCMessage (type, logicalName, fileList);
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
	 * Specialized send of a REGISTER_ACK/FINALIZE message
	 */
	public static void sendTo (String destination, Type type) {
		sendTo(destination, type, null, null);
	}

	/**
	 *  Specialized send of a REGISTER_FILE/SEND_LOGICAL_FILE message
	 */
	public static void sendTo (String destination, Type type, 
			LogicalFile file) {
		Vector<LogicalFile> list = new Vector<LogicalFile>();
		list.add(file);
		sendTo(destination, type, null, list);
	}

	/**
	 *  Specialized send of a SEND_LS message
	 */
	public static void sendTo (String destination, Type type, 
			Vector<LogicalFile> fileList) {
		sendTo(destination, type, null, fileList);
	}

	/**
	 * Specialized send of a ASK_LOGICAL_FILE message/ASK_LS
	 */
	public static void sendTo (String destination, Type type, 
			String logicalName) {
		sendTo(destination, type, logicalName, null);
	}

}
