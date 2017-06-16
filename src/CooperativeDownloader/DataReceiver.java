package CooperativeDownloader;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;

public class DataReceiver implements Runnable {

    private Merger merger;
    private DatagramSocket socket;
    private Thread thread;
    private boolean isRunning;

    public DataReceiver(Merger merger) {
	this.merger = merger;
	thread = new Thread(this);
	isRunning = true;
    }

    public void start() {
	thread.start();
    }

    public void stopRunning() {
	isRunning = false;
    }

    public void run() {
	try {
	    // Keep a socket open to listen to all the UDP traffic that is
	    // destined for this port
	    // Default port 8888
	    socket = new DatagramSocket(merger.getDataPort(),
		    InetAddress.getByName("0.0.0.0"));
	    socket.setBroadcast(true);

	    while (isRunning) {
		// System.out.println(getClass().getName()
		// + ">>>Ready to receive data broadcast packets!");

		// Receive a packet
		byte[] recvBuf = new byte[65536];
		DatagramPacket packet = new DatagramPacket(recvBuf,
			recvBuf.length);
		// socket.setReceiveBufferSize(256 * 1024);
		System.out.println(
			"Before receive Hereeeeeeeeeeeeeeeeeeee in data receiver");

		socket.receive(packet);

		System.out.println(
			"After receive Hereeeeeeeeeeeeeeeeeeee in data receiver");

		// Packet received
		System.out.println(
			">> ID = " + merger.getId() + " " + getClass().getName()
				+ ">>>$$$$$$$$$$$$$$Data packet received from: "
				+ packet.getAddress().getHostAddress());
		// System.out.println(getClass().getName() +
		// ">>>Packet received; data: " + new String(packet.getData()));

		// Split byte[] array
		byte[] packetByteArray = packet.getData();
		System.out.println(
			"$$$$$$$$$$$$$$$$$$$$$ packetByteArray.length = "
				+ packetByteArray.length);

		byte[] header = Arrays.copyOfRange(packetByteArray, 0, 50);
		System.out.println("$$$$$$$$$$$$$$$$$$$$$ header.length = "
			+ header.length);

		// See if the packet holds the right command (message)
		String message = new String(header).trim();
		System.out.println("$$$$$$$$$$$$$$$$$$$$$ header = " + message);
		// if (message.equals("DISCOVER_FUIFSERVER_REQUEST")) {
		if (message.length() >= 2 && message.charAt(0) == 'D'
			&& message.charAt(1) == 'P') {
		    String[] messageParts = message.split(" "); // DP SessionId
								// Id
								// StartAddress
								// dataLength
								// ChunkData;
		    long id = Long.parseLong(messageParts[2]);
		    long sessionId = Long.parseLong(messageParts[1]);
		    long startAddress = Long.parseLong(messageParts[3]);
		    long endAddress;

		    endAddress = startAddress + Long.parseLong(messageParts[4])
			    - 1;

		    if (id != merger.getId()
			    && sessionId == merger.getSessionId()) {
			System.out.println(">> ID = " + merger.getId() + " "
				+ getClass().getName()
				+ ">>> DP Packet received; data: " + sessionId
				+ " " + id + " " + startAddress + " "
				+ "DATA_HERE");
			// + new String(packet.getData()));

			if (!merger.hasChunk(startAddress)) {
			    System.out.println(">> ID = " + merger.getId() + " "
				    + "New Chunk Found! " + startAddress);

			    // convert to file
			    int endByte = Integer.parseInt(messageParts[4])
				    + 50;
			    byte[] data = Arrays.copyOfRange(packetByteArray,
				    50, endByte);
			    System.out.println(
				    "$$$$$$$$$$$$$$$$$$$$$ data.length = "
					    + data.length);

			    byte[] dataPartsBytes = data;

			    FileUtils.writeByteArrayToFile(
				    new File(merger.getFileName() + "/"
					    + startAddress + "_" + endAddress),
				    dataPartsBytes);

			    // make chunk object for it in the merger
			    Chunk newChunk = new Chunk(startAddress, endAddress,
				    merger);
			    merger.addChunk(newChunk);
			}
		    }
		}
	    }
	} catch (IOException ex) {
	    // Logger.getLogger(StatusBroadcaster.class.getName()).log(
	    // Level.SEVERE, null, ex);
	    System.out.println("ERRRRRRRRRRRRRRRRRRRRORRRRRR in data receiver");
	    ex.printStackTrace();
	}
    }
}