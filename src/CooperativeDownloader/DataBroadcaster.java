package CooperativeDownloader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;

public class DataBroadcaster implements Runnable {

    private Thread thread;
    private Merger merger;
    private boolean isRunning;

    public DataBroadcaster(Merger merger) {
	this.merger = merger;
	isRunning = true;
	thread = new Thread(this);
    }

    public void start() {
	thread.start();
    }

    public void stopRunning() {
	isRunning = false;
    }

    public void run() {
	while (isRunning) {

	    HashMap<Long, Participant> participants = merger.getParticipants();

	    System.out.println(">> ID = " + merger.getId()
		    + " Data thread sleeping for 15 secs before broadcasting any needed Data again");
	    // sleep for a while
	    try {
		Thread.sleep(2000);
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }

	    if (!isMyTurnToSend(participants)) {
		continue;
	    }

	    // Find the server using UDP broadcast
	    try {
		// Open a random port to send the package
		DatagramSocket c = new DatagramSocket();
		c.setBroadcast(true);

		for (int i = 0; i < merger.getMyChunks().size(); i++) {

		    if (!isChunkNeededToBeSent(merger.getMyChunks().get(i))) {
			continue;
		    }
		    // DP: Data packet
		    // TODO need to get the chunk and put it in message here
		    // byte[] sendData = ("DP " + merger.getSessionId() + " "
		    // + merger.getId() + " "
		    // + merger.getMyChunks().get(i).getStart() + " "
		    // + merger.getMyChunks().get(i).getChunkData()).getBytes();

                    byte[] chunkData = merger.getMyChunks().get(i)
			    .getChunkData();
		    byte[] headerData = ("DP " + merger.getSessionId() + " "
			    + merger.getId() + " "
			    + merger.getMyChunks().get(i).getStart()+ " " + chunkData.length )
				    .getBytes();
		    int offsetBytesCount = 50 - headerData.length;
		    
		    int packetLength = headerData.length + offsetBytesCount
			    + chunkData.length;
		    byte[] sendData = new byte[packetLength];
		    System.out.println(
			    "##################packetLength = " + packetLength);
		    System.out.println("##################headerData.length = "
			    + headerData.length);
		    System.out.println("##################offsetBytesCount = "
			    + offsetBytesCount);
		    System.out.println("##################chunkData.length = "
			    + chunkData.length);

		    for (int i1 = 0; i1 < packetLength; i1++) {
			if (i1 < headerData.length) {
			    sendData[i1] = headerData[i1];
			} else if (i1 < headerData.length + offsetBytesCount) {
			    System.out.println(
				    "########before##########i1 = " + i1);
			    i1 = headerData.length + offsetBytesCount - 1;
			    System.out.println(
				    "########after##########i1 = " + i1);
			} else {
			    sendData[i1] = chunkData[i1 - headerData.length
				    - offsetBytesCount];
			}

		    }
		    System.out.println("sendData.length = " + sendData.length);

		    // Try the 255.255.255.255 first
		    try {
			DatagramPacket sendPacket = new DatagramPacket(sendData,
				sendData.length,
				InetAddress.getByName("255.255.255.255"),
				merger.getDataPort());
			// c.setReceiveBufferSize(60 * 1024);
			System.out.println(
				"########sendData#########" + sendData.length);
			System.out.println("#########getSendBufferSize########"
				+ c.getSendBufferSize());
			c.send(sendPacket);
			System.out.println(">> ID = " + merger.getId()
				+ " >>> Data packet sent to: 255.255.255.255 (DEFAULT)");
		    } catch (Exception e) {
		    }

		    // Broadcast the message over all the network interfaces
		    @SuppressWarnings("rawtypes")
		    Enumeration interfaces = NetworkInterface
			    .getNetworkInterfaces();
		    while (interfaces.hasMoreElements()) {
			NetworkInterface networkInterface = (NetworkInterface) interfaces
				.nextElement();

			if (networkInterface.isLoopback()
				|| !networkInterface.isUp()) {
			    continue; // Don't want to broadcast to the loopback
				      // interface
			}

			for (InterfaceAddress interfaceAddress : networkInterface
				.getInterfaceAddresses()) {
			    InetAddress broadcast = interfaceAddress
				    .getBroadcast();
			    if (broadcast == null) {
				continue;
			    }

			    // Send the broadcast package!
			    try {
				DatagramPacket sendPacket = new DatagramPacket(
					sendData, sendData.length, broadcast,
					merger.getDataPort());
				c.send(sendPacket);
			    } catch (Exception e) {
			    }

			    System.out.println(">> ID = " + merger.getId()
				    + " >>> Data packet sent to: "
				    + broadcast.getHostAddress()
				    + "; Interface: "
				    + networkInterface.getDisplayName());
			}
		    }

		    System.out.println(">> ID = " + merger.getId()
			    + " >>>Data: Done looping over all network interfaces. ");

		}

		if (isAllDataDelivered()) {
		    merger.setFinishedSending(true);
		    this.stopRunning();
		}

		// Close the port!
		c.close();
	    } catch (IOException ex) {
		System.out.println(ex.getStackTrace());

	    }
	}
    }

    private boolean isAllDataDelivered() {
	ArrayList<Chunk> chunks = merger.getMyChunks();
	HashMap<Long, Participant> participants = merger.getParticipants();
	Long[] keys = participants.keySet()
		.toArray(new Long[participants.keySet().size()]);

	// Naive implementation can be optimized
	for (int i = 0; i < chunks.size(); i++) {
	    for (int j = 0; j < keys.length; j++) {
		if (!participants.get(keys[j])
			.hasChunk(chunks.get(i).getStart())) {
		    return false;
		}
	    }
	}

	return true;
    }

    private boolean isChunkNeededToBeSent(Chunk chunk) {

	HashMap<Long, Participant> participants = merger.getParticipants();
	Long[] keys = participants.keySet()
		.toArray(new Long[participants.keySet().size()]);

	// Naive implementation can be optimized

	for (int j = 0; j < keys.length; j++) {
	    if (!participants.get(keys[j]).hasChunk(chunk.getStart())) {
		return true;
	    }
	}

	return false;
    }

    private boolean isMyTurnToSend(HashMap<Long, Participant> participants) {

	Long[] keys = participants.keySet()
		.toArray(new Long[participants.keySet().size()]);

	if (keys.length < 1)
	    return false;

	Arrays.sort(keys);
	for (int i = 0; i < keys.length; i++) {
	    if (merger.getId() < keys[i]) {
		return true;
	    } else if (merger.getId() > keys[i]
		    && participants.get(keys[i]).isFinishedSending() == false) {
		return false;
	    }
	}
	return true;
    }

}
