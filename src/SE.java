import org.simgrid.msg.Msg;
import org.simgrid.msg.Host;
import org.simgrid.msg.Process;

public class SE extends Process {
	
	private String hostName;
	
	private void handleUploadRequest(Message message) {
		//TODO this function should read a file on disk at some point
		Message uploadAck= new Message(Message.Type.UPLOAD_ACK);
		
		uploadAck.emit(message.getMailbox());
	
		Msg.debug("SE '"+ hostName + "' sent ack back to '" + 
				message.getMailbox() + "'");
	}

	private void handleDownloadRequest(Message message) {
		Message sendFile= new Message(Message.Type.SEND_FILE, 
					message.getLogicalFileSize());
		
		sendFile.emit(message.getMailbox());
		
		Msg.debug("SE '"+ hostName + "' sent file '" + 
				message.getLogicalFileName() + "' of size " + 
				message.getLogicalFileSize() + " to '" + 
				message.getMailbox() + "'");
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public SE (Host host, String name, String[]args) {
		super(host,name,args);
		this.setHostName(this.getHost().getName());
	}
	
	public void main(String[] args) {
		boolean stop = false;
		Msg.debug("Register SE on "+ this.hostName);
		VIPSimulator.seList.add(this.getHost());

		while (!stop){
			Message message = Message.process(hostName);
			
			switch(message.getType()){
			case DOWNLOAD_REQUEST:
				handleDownloadRequest(message);
				break;
			case UPLOAD_REQUEST:
				handleUploadRequest(message);
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
}
