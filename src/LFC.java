import java.util.Vector;
import java.util.Iterator;

import org.simgrid.msg.Msg;
import org.simgrid.msg.Host;
import org.simgrid.msg.Process;

public class LFC extends Process {

	private String hostName;
	private Vector<LFCFile> fileList;

	private void addFile(String logicalFileName, long logicalFileSize,
			String seName){
		LFCFile newFile = new LFCFile(logicalFileName, logicalFileSize, seName);
		this.fileList.add(newFile);
	}

	private void handleRegisterFile(Message message) {
		addFile(message.getLogicalFileName(), message.getLogicalFileSize(), 
				message.getSEName());

		Message registerAck = new Message(Message.Type.REGISTER_ACK);
		registerAck.emit(message.getMailbox());
		Msg.debug("LFC '"+ this.hostName + "' sent back an ack to '" +
				message.getMailbox() + "'");	
	}

	private void handleAskFileInfo(Message message) {
		String logicalFileName = message.getLogicalFileName();
		
		String SEName = getSEName(logicalFileName);
		long logicalFileSize = getLogicalFileSize(logicalFileName);

		if(SEName == null){	
			Msg.error("File '" + logicalFileName + 
					"' is stored on no SE. Exiting with status 1");
			System.exit(1);
		}

		Message replySEName = new Message(Message.Type.SEND_FILE_INFO, SEName,
				logicalFileSize);

		replySEName.emit(message.getMailbox());
		Msg.debug("LFC '"+ this.hostName + "' sent SE name '" + SEName +
				"' back to '" + message.getMailbox() + "'");

	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public LFC(Host host, String name, String[]args) {
		super(host,name,args);
		this.setHostName(this.getHost().getName());
		this.fileList = new Vector<>();
	}

	public void main(String[] args) {
		boolean stop = false;
		Msg.debug("Register LFC on "+ this.hostName);
		VIPSimulator.seList.add(this.getHost());

		while (!stop){
			Message message = Message.process(hostName);

			switch(message.getType()){
			case CR_INPUT:
				// Register the information sent in the message into the LFC by
				// adding a new File
				addFile(message.getLogicalFileName(),
						message.getLogicalFileSize(), message.getSEName());

				Msg.info("LFC '"+ this.hostName + "' registered file '" + 
						message.getLogicalFileName() + "', of size " + 
						message.getLogicalFileSize() + ", stored on SE '" + 
						message.getSEName() + "'");
				break;
			case REGISTER_FILE:
				handleRegisterFile(message);
				break;
			case ASK_FILE_INFO:
				handleAskFileInfo(message);
				break;
			case FINALIZE:
				Msg.verb("Goodbye!");
				stop = true;
				break;
			default:
				break;
			}
		}
	}

	public String getSEName (String logicalFileName){
		String SEName = null;
		Iterator<LFCFile> it = this.fileList.iterator();

		while (it.hasNext() && SEName == null){
			LFCFile current = it.next();
			if (current.getLogicalFileName() == logicalFileName){
				SEName = current.getSEName();
			}
		}

		if (SEName == null)
			Msg.error("Logical file '" + logicalFileName + 
					"' not found on LFC '" + this.hostName + "'");

		return SEName;
	}
	
	public long getLogicalFileSize (String logicalFileName){
		long logicalFileSize = 0;
		Iterator<LFCFile> it = this.fileList.iterator();

		while (it.hasNext() && logicalFileSize == 0){
			LFCFile current = it.next();
			if (current.getLogicalFileName() == logicalFileName){
				logicalFileSize = current.getLogicalFileSize();
			}
		}

		if (logicalFileSize == 0)
			Msg.error("Logical file '" + logicalFileName + 
					"' not found on LFC '" + this.hostName + "'");

		return logicalFileSize;
	}

	public String getLogicalFileList () {
		String fileList = "";
		Iterator<LFCFile> it = this.fileList.iterator();

		while (it.hasNext())
			fileList.concat(it.next().getLogicalFileName() + ",");

		//removing last comma
		if (fileList.charAt(fileList.length()-1)==',')
			fileList = fileList.substring(0, fileList.length()-1);

		return fileList;
	}
}