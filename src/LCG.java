import java.util.Vector;

import org.simgrid.msg.Msg;

public class LCG {

	public static void crInput(String mailbox, String logicalFileName,
			long logicalFileSize, String SEName, String LFCName) {
		// To prevent message mixing, a specific mailbox is used whose name
		// is the concatenation of LFC's hostName and the given mailbox
		String LFCMailbox = LFCName+mailbox;

		LogicalFile file = new LogicalFile(logicalFileName, logicalFileSize, 
				SEName);
		Msg.info("Ask '"+ LFCName + "' to register " + file.toString());

		LFCMessage.sendTo(LFCName, LFCMessage.Type.REGISTER_FILE, file);
		LFCMessage.getFrom(LFCMailbox);

		Msg.debug("lcg-cr-input of '" + logicalFileName +"' on LFC '" + 
				LFCName +"' completed");
	}

	public static void cr(String mailbox, String SEName, String localFileName,
			long localFileSize, String logicalFileName, String LFCName) {
		Msg.debug("lcg-cr '" + logicalFileName + "' from '" + localFileName + 
				"' using lfc '" + LFCName +"'");
		// To prevent message mixing, a specific mailbox is used whose name
		// is the concatenation of LFC's hostName and the given mailbox
		String LFCMailbox = LFCName+mailbox;

		// upload file to SE
		SEMessage.sendAsynchronouslyTo(SEName, SEMessage.Type.UPLOAD_FILE, 
				localFileSize);

		//waiting for upload to finish
		Msg.info("Sent upload request to SE '" + SEName + "' for file '" + 
				logicalFileName +"' of size " + localFileSize +". Waiting for" +
				" an ack");

		SEMessage.getFrom(mailbox);
		Msg.info("SE '"+ SEName + "' replied with an ACK");


		// Register file into LFC
		LogicalFile file = new LogicalFile(logicalFileName, localFileSize, 
				SEName);
		Msg.info("Ask '"+ LFCName + "' to register " + file.toString());

		LFCMessage.sendTo(LFCName, LFCMessage.Type.REGISTER_FILE, file);
		LFCMessage.getFrom(LFCMailbox);
		
		Msg.info("LFC '"+ LFCName + "' registered " + file.toString());

		Msg.debug("lcg-cr of '" + logicalFileName +"' on LFC '" + LFCName +
				"' completed");
	}

	public static void cp(String mailbox, String logicalFileName,
			String localFileName, String LFCName){
		String SEName = null;
		long logicalFileSize = 0;
		// To prevent message mixing, a specific mailbox is used whose name
		// is the concatenation of LFC's hostName and the given mailbox
		String LFCMailbox = LFCName+mailbox;

		Msg.info("lcg-cp '" + logicalFileName + "' to '" + localFileName +
				"' using LFC '" + LFCName + "'");

		// get information on Logical File from the LFC
		LFCMessage.sendTo(LFCName, LFCMessage.Type.ASK_LOGICAL_FILE, 
				logicalFileName);

		Msg.info("Asked about '" + logicalFileName + "' to LFC '" + LFCName + 
				"'. Waiting for information ...");
		
		LFCMessage getFileInfo = LFCMessage.getFrom(LFCMailbox);

		SEName = getFileInfo.getFile().getSEName();
		logicalFileSize = getFileInfo.getFile().getSize();

		Msg.info("LFC '"+ LFCName + "' replied: " + 
				getFileInfo.getFile().toString()); 

		// Download physical File from SE
		Msg.info("Downloading file '" + logicalFileName + "' from SE '" + 
				SEName + "' using LFC '" + LFCName +"'");

		SEMessage.sendTo(SEName, SEMessage.Type.DOWNLOAD_REQUEST, 
				logicalFileName, logicalFileSize);

		Msg.info("Sent download request for " + 
				getFileInfo.getFile().toString() + 
				". Waiting for reception ...");

		SEMessage.getFrom(mailbox);

		Msg.info("SE '"+ SEName + "' sent " + getFileInfo.getFile().toString());

		Msg.debug("lcg-cp of '" + logicalFileName +"' to '" + localFileName +
				"' completed");
	};

	public static Vector<String> ls(String mailbox, String directoryName, 
			String LFCName){
		Vector<String> results = new Vector<String>();
		String LFCMailbox = LFCName+mailbox;

		// Ask the LFC for the list of files to merge
		LFCMessage.sendTo(LFCName, LFCMessage.Type.ASK_LS, directoryName);
		Msg.info("asked for list of files to merge. waiting for reply from " + 
				LFCName);
		LFCMessage m = LFCMessage.getFrom(LFCMailbox);
		
		for (LogicalFile f : m.getFileList()) 
			results.add (f.getName());

		return results;
	}

}
