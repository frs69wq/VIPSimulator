import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;
import java.util.Iterator;

import org.simgrid.msg.Msg;
import org.simgrid.msg.Host;
import org.simgrid.msg.Process;

public class LFC extends Process {

	protected String hostName;
	private Vector<LFCFile> fileList;

	private void register(LFCFile newFile){
		if (fileList.contains((Object) newFile)){
			LFCFile file = fileList.get(fileList.indexOf(newFile));
			if (!file.getLocations().contains((Object) newFile.getSEName())){
				// This has to be a new replica
				Msg.info("New replica for '" + newFile.getName () + "' on '" + 
						newFile.getSEName() + "'");
				file.addLocation(newFile.getSEName());
			} else {
				Msg.debug(file.toString() + "is already registered");
			}
		} else {
			// This file is not registered yet, create and add it
			Msg.debug ("'" + newFile.getName() + "' is not registered yet");
			fileList.add(newFile);
			Msg.info("LFC '"+ hostName + "' registered " + newFile.toString());
		}
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

	private void populateLFC(String catalog){
		BufferedReader br = null;
		String line = "";

		Msg.info("Population of LFC '"+ this.hostName + "'");
		try {
			br = new BufferedReader(new FileReader(catalog));
			while ((line = br.readLine()) != null) {
				String[] fileInfo = line.split(",");
				String[] seNames = fileInfo[2].split(":");
				LFCFile file = new LFCFile(fileInfo[0], 
						Long.valueOf(fileInfo[1]).longValue(), seNames);
				Msg.info("Importing file '" + file.toString());
				fileList.add(file);
			}
		} catch (FileNotFoundException fnf) {
			fnf.printStackTrace();
		} catch (IOException r) {
			r.printStackTrace();
		}
		try {
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public LFC(Host host, String name, String[]args) {
		super(host,name,args);
		this.hostName = getHost().getName();
		this.fileList = new Vector<>();
	}

	public void main(String[] args) {
		boolean stop = false;
		LFCFile file = null;
		
		Msg.debug("Register LFC on "+ hostName);
		VIPSimulator.lfcList.add(getHost());
		String catalog = (args.length > 0 ? args[0] : null);

		if (catalog != null){
			populateLFC(catalog);
		}

		while (!stop){
			Message message = Message.process(hostName);

			switch(message.getType()){
			case CR_INPUT:
				// Register the information sent in the message into the LFC by
				// adding a new File
				file = new LFCFile(message.getLogicalFileName(),
						message.getLogicalFileSize(), message.getSEName());
				register(file);
				break;
			case REGISTER_FILE:
				file = new LFCFile(message.getLogicalFileName(),
						message.getLogicalFileSize(), message.getSEName());
				register(file);
				Message registerAck = new Message(Message.Type.REGISTER_ACK);
				registerAck.emit(message.getMailbox());
				Msg.debug("LFC '"+ hostName + "' sent back an ack to '" +
						message.getMailbox() + "'");
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
			if (current.getName().equals(logicalFileName)){
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
			if (current.getName().equals(logicalFileName)){
				logicalFileSize = current.getSize();
			}
		}

		if (logicalFileSize == 0)
			Msg.error("Logical file '" + logicalFileName + 
					"' not found on LFC '" + this.hostName + "'");

		return logicalFileSize;
	}

	public String getLogicalFileList () {
		String list = "";
		Iterator<LFCFile> it = this.fileList.iterator();

		while (it.hasNext())
			list = list.concat(it.next().toString()).concat(",");

		//removing last comma
		if (list.charAt(list.length()-1) == ',')
			list = list.substring(0, list.length()-1);

		return list;
	}
}