package CooperativeDownloader;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ControlPacketsReceiver implements Runnable {

    private Merger merger;
    private DatagramSocket socket;
    private Thread thread;

    public ControlPacketsReceiver(Merger merger) {
	this.merger = merger;
	thread = new Thread(this);
    }

    public void start() {
	thread.start();
    }

    
    public void run() {
      try {
        //Keep a socket open to listen to all the UDP traffic that is destined for this port
        //Default port 8888
        socket = new DatagramSocket(merger.getControlPort(), InetAddress.getByName("0.0.0.0"));
        socket.setBroadcast(true);

        while (true) {
          //System.out.println(getClass().getName() + ">>>Ready to receive broadcast packets!");

          //Receive a packet
          byte[] recvBuf = new byte[15000];
          DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
          socket.receive(packet);

          //Packet received
          System.out.println(">> ID = "+merger.getId()+" "+getClass().getName() + " >>>Control packet received from: " + packet.getAddress().getHostAddress());
          //System.out.println(getClass().getName() + ">>>Packet received; data: " + new String(packet.getData()));

          //See if the packet holds the right command (message)
          String message = new String(packet.getData()).trim();
//           if (message.equals("DISCOVER_FUIFSERVER_REQUEST")) {
           if (message.length()>=2 && message.charAt(0) == 'C' && message.charAt(1)=='P') {
             String[] messageParts= message.split(" "); // CP sid id isFinished a,b,c,d,e,f
             long id = Long.parseLong(messageParts[2]);
             long sessionId = Long.parseLong(messageParts[1]);
             if(id!= merger.getId() && sessionId == merger.getSessionId()) {
             	System.out.println(">> ID = "+merger.getId()+" "+getClass().getName() + ">>> CP Packet received; data: " + new String(packet.getData()));
                HashMap<Long,Participant> participants = merger.getParticipants();
               
                String[] chunksAvailable= messageParts[4].split(",");
               	boolean isParticipantFinished = (messageParts[3].equals("true"))?true:false;
                  
               if(participants.containsKey(id)){ // exist before, then get it and update chunks hashset
                 	 HashSet<Long> chunksBeginings = participants.get(id).getAvailableChunks();
                 	participants.get(id).setFinishedSending(isParticipantFinished);
                  // add chunks to the hash set <<<
                 for(int i = 0 ; i < chunksAvailable.length ; i++){
                 	chunksBeginings.add(Long.parseLong(chunksAvailable[i]));
                 }
                 
               } else { // first time, create a new participant object
                 HashSet<Long> chunksBeginings = new HashSet<Long>();
                 
                 // add chunks to the hash set <<<
                 for(int i = 0 ; i < chunksAvailable.length ; i++){
                 	chunksBeginings.add(Long.parseLong(chunksAvailable[i]));
                 }
                 
                 Participant participant = new Participant(id,chunksBeginings,isParticipantFinished);
                 participants.put(id,participant);
               }
             }
          }
        }
      } catch (IOException ex) {
        Logger.getLogger(StatusBroadcaster.class.getName()).log(Level.SEVERE, null, ex);
      }
    }

}
