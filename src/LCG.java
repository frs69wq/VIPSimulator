import java.util.Vector;

import org.simgrid.msg.Msg;

public class LCG {

	public static void crInput(LFC lfc, String logicalFileName,
			long logicalFileSize, String seName) {

		LogicalFile file = 
				new LogicalFile(logicalFileName, logicalFileSize, seName);
		Msg.info("Ask '"+ lfc.getName() + "' to register " + file.toString());

		lfc.register(file);

		Msg.debug("lcg-cr-input of '" + logicalFileName +"' on LFC '" + 
				lfc +"' completed");

	}

	public static void cr(String mailbox, String seName, String localFileName,
			long localFileSize, String logicalFileName, LFC lfc) {
		Msg.info("lcg-cr '" + logicalFileName + "' from '" + localFileName + 
				"' using lfc '" + lfc +"'");
		// To prevent message mixing, a specific mailbox is used whose name
		// is the concatenation of LFC's hostName and the given mailbox

		// upload file to SE
		SEMessage.sendAsynchronouslyTo(seName, SEMessage.Type.UPLOAD_FILE, 
				localFileSize);

		//waiting for upload to finish
		Msg.info("Sent upload request to SE '" + seName + "' for file '" + 
				logicalFileName +"' of size " + localFileSize +". Waiting for" +
				" an ack");

		SEMessage.getFrom(mailbox);
		Msg.info("SE '"+ seName + "' replied with an ACK");


		// Register file into LFC
		LogicalFile file = 
				new LogicalFile(logicalFileName, localFileSize, seName);
		Msg.info("Ask '"+ lfc.getName() + "' to register " + file.toString());
		
		lfc.register(file);

		Msg.info("lcg-cr of '" + logicalFileName +"' on LFC '" + 
				lfc.getName() + "' completed");
	}

	public static void cp(String mailbox, String logicalFileName,
			String localFileName, LFC lfc){
		Msg.info("lcg-cp '" + logicalFileName + "' to '" + localFileName +
				"' using LFC '" + lfc.getName() + "'");

		// get information on Logical File from the LFC
		LogicalFile file = lfc.getLogicalFile(logicalFileName);

		Msg.info("LFC '"+ lfc.getName() + "' replied: " + file.toString()); 

		// Download physical File from SE
		Msg.info("Downloading file '" + logicalFileName + "' from SE '" + 
				file.getLocation() + "' using LFC '" + lfc.getName() +"'");

		SEMessage.sendTo(file.getLocation(), SEMessage.Type.DOWNLOAD_REQUEST, 
				logicalFileName, file.getSize());

		Msg.info("Sent download request for " + 
				file.toString() + 
				". Waiting for reception ...");

		SEMessage.getFrom(mailbox);

		Msg.info("SE '"+ file.getLocation() + "' sent " + file.toString());

		Msg.debug("lcg-cp of '" + logicalFileName +"' to '" + localFileName +
				"' completed");
	};

	public static Vector<String> ls(LFC lfc, String directoryName){
		Vector<String> results = new Vector<String>();

		// Ask the LFC for the list of files to merge
		Vector<LogicalFile> fileList = lfc.getLogicalFileList(directoryName);
		
		for (LogicalFile f : fileList) 
			results.add (f.getName());

		return results;
	}
}
