package CooperativeDownloader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

public class Merger {

    private long id, sessionId,fileSize,currentFileSize;
    private String fileName;
    private ArrayList<Chunk> myChunks;
    private StatusBroadcaster sendingStatusThread;
    private DataBroadcaster sendingDataThread;
    private DataReceiver receivingDataThread;
    private ControlPacketsReceiver receivePacketsThread;
    private int controlPort;
    private int dataPort;
    private boolean finishedSending = false;

    private HashMap<Long, Participant> participants;
  
      public Merger(long sessionId, long id, int controlPort, int dataPort, String fileName, long fileSize) {
	this.id = id;
	this.sessionId = sessionId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        currentFileSize = 0;
	this.myChunks = readMyChunks();
	sendingStatusThread = new StatusBroadcaster(this);
	receivePacketsThread = new ControlPacketsReceiver(this);
	sendingDataThread = new DataBroadcaster(this);
      	receivingDataThread = new DataReceiver(this); 
	this.controlPort = controlPort;
	this.dataPort = dataPort;
	participants = new HashMap<Long, Participant>();
    }
  
  private ArrayList<Chunk> readMyChunks(){
    /* Read all chunks from file directory */
	File fileDirectory = new File(fileName);
	File[] files = fileDirectory.listFiles();
    	ArrayList<Chunk> allMyChunks = new ArrayList<Chunk>();
    	long start,end;
    	String[] splitted;
    	for(File x: files){
          splitted = x.getName().split("_");
          start = Long.parseLong(splitted[0]);
          end = Long.parseLong(splitted[1]);
          System.out.println("Chunk: "+start+"_"+end);
          allMyChunks.add(new Chunk(start,end,this));
          currentFileSize+=(end-start+1);
    	}
    	return allMyChunks;
  }

    public boolean isFinishedSending() {
	return finishedSending;
    }

    public void setFinishedSending(boolean finishedSending) {
	this.finishedSending = finishedSending;
    }

    public HashMap<Long, Participant> getParticipants() {
	return participants;
    }

    public int getControlPort() {
	return controlPort;
    }

    public void setControlPort(int port) {
	this.controlPort = port;
    }

    public int getDataPort() {
	return dataPort;
    }

    public void setDataPort(int port) {
	this.dataPort = port;
    }

    public void startControlSendThread() {
	sendingStatusThread.start();
    }
  

    public void startControlReceiveThread() {
	receivePacketsThread.start();
    }

    public void startDataSendThread() {
	sendingDataThread.start();
    }
  
    public void startDataReceiveThread() {
	receivingDataThread.start();
    }

    public StatusBroadcaster getSendingStatusThread() {
	return sendingStatusThread;
    }

    public ControlPacketsReceiver getReceivePacketsThread() {
	return receivePacketsThread;
    }

    public long getId() {
	return id;
    }

    public long getSessionId() {
	return sessionId;
    }

    public String getAvailableChunks() {
	StringBuilder str = new StringBuilder();
	for (Chunk x : myChunks) {
	    str.append(x.getStart());
	    str.append(",");
	}
	if (str.length() > 0)
	    str.setLength(str.length() - 1);
	return str.toString();
    }

    /* Should be called to remove any long going process */
    public void finishMerging() {
	sendingStatusThread.stopRunning(); // don't know if you should kill the
					   // thread here!
    }

    public ArrayList<Chunk> getMyChunks() {
	return myChunks;
    }

    public void setMyChunks(ArrayList<Chunk> myChunks) {
	this.myChunks = myChunks;
    }

    public boolean hasChunk(long startAddress) {
	for (Chunk chunk : myChunks) {
	    if (chunk.getStart() == startAddress)
		return true;
	}
	return false;
    }
    
    public void addChunk(Chunk newChunk){
        myChunks.add(newChunk);
      	currentFileSize+=(newChunk.getEnd()-newChunk.getStart()+1);
        if(currentFileSize>=fileSize){
          receivingDataThread.stopRunning();
          // merge file here
          try {
	    mergePartsLocally();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
        }
      
      }
  
  public void startMerging() {
	  startControlSendThread();
          startControlReceiveThread();
          startDataSendThread();
          startDataReceiveThread();
          
          if(currentFileSize>=fileSize){
              receivingDataThread.stopRunning();
              // merge file here
              try {
    	    mergePartsLocally();
    	} catch (IOException e) {
    	    // TODO Auto-generated catch block
    	    e.printStackTrace();
    	}
            }
    }

public String getFileName() {
    return fileName;
}

public void setFileName(String fileName) {
    this.fileName = fileName;
}
  
  private void mergePartsLocally() throws IOException {
	/* Read all chunks from file directory */
	File fileDirectory = new File(fileName);
	File[] files = fileDirectory.listFiles();
	Comparator<File> fileComparator = new Comparator<File>() {

	    public int compare(File o1, File o2) {
                int str1 = Integer.parseInt(o1.getName().split("_")[0]);
                int str2 = Integer.parseInt(o2.getName().split("_")[0]);
		return str1-str2;
	    }
	};
	Arrays.sort(files,fileComparator);
	for(File file:files){
	    System.out.println(file.getName());
	}

	/* Create the output file */
	String mOutputFile = fileName;
	RandomAccessFile outputFile = new RandomAccessFile(
		fileName + "/" + mOutputFile, "rw");
	outputFile.seek(0);

	/* Merge chunks by appending each of them to the output file */
	for (File chunk : files) {
	    appendChunk(outputFile, chunk);
	}

	outputFile.close();
	System.out.println("Done!");
    }

    private void appendChunk(RandomAccessFile output, File chunk)
	    throws IOException {

	FileInputStream fis = null;

	fis = new FileInputStream(chunk);

	BufferedInputStream in = new BufferedInputStream(fis);

	int BUFFER_SIZE = 4096;
	byte data[] = new byte[BUFFER_SIZE];
	int numRead;
	while (((numRead = in.read(data, 0, BUFFER_SIZE)) != -1)) {
	    // write to buffer
	    output.write(data, 0, numRead);
	}

	fis.close();
	in.close();

    }
  
}
