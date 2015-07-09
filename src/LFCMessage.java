import java.util.Vector;

import org.simgrid.msg.Msg;
import org.simgrid.msg.MsgException;

public class LFCMessage extends Message {
	// this class of control messages is dedicated to the interactions with the 
	// Logical File Catalog(s) that happen only through lcg-utils functions.
	// These functions are called by the worker processes.
	private Vector<LogicalFile> fileList = null;

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
		this.fileName=logicalName;
		this.fileList = files;
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
