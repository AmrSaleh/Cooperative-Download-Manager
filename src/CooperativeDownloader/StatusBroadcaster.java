package CooperativeDownloader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class StatusBroadcaster implements Runnable{
  private Thread thread;
  private Merger merger;
  private boolean isRunning;
  
  public StatusBroadcaster(Merger merger){
    this.merger = merger;
    isRunning = true;
    thread = new Thread(this);
  }
  
  public void start(){
      thread.start();
  }
  
  public void stopRunning(){
    isRunning = false;
  }
  
  public void run(){
    while(isRunning){
      // Find the server using UDP broadcast
      try {
          // Open a random port to send the package
          DatagramSocket c = new DatagramSocket();
          c.setBroadcast(true);

          // CP: Control Packet
          byte[] sendData = ("CP "+merger.getSessionId()+" "+merger.getId()+" "+merger.isFinishedSending()+" "+merger.getAvailableChunks()).getBytes();

          // Try the 255.255.255.255 first
          try {
          DatagramPacket sendPacket = new DatagramPacket(sendData,
              sendData.length,
              InetAddress.getByName("255.255.255.255"), merger.getControlPort());//8888 default
          c.send(sendPacket);
          System.out.println(">> ID = "+merger.getId()+" >>> Status packet sent to: 255.255.255.255 (DEFAULT)");
          } catch (Exception e) {
          }

          // Broadcast the message over all the network interfaces
          Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
          while (interfaces.hasMoreElements()) {
          NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();

          if (networkInterface.isLoopback() || !networkInterface.isUp()) {
              continue; // Don't want to broadcast to the loopback
                    // interface
          }

          for (InterfaceAddress interfaceAddress : networkInterface
              .getInterfaceAddresses()) {
              InetAddress broadcast = interfaceAddress.getBroadcast();
              if (broadcast == null) {
              continue;
              }

              // Send the broadcast package!
              try {
              DatagramPacket sendPacket = new DatagramPacket(sendData,
                  sendData.length, broadcast, merger.getControlPort());//8888
              c.send(sendPacket);
              } catch (Exception e) {
              }

              System.out.println(">> ID = "+merger.getId()+">>> Status packet sent to: "
                  + broadcast.getHostAddress() + "; Interface: "
                  + networkInterface.getDisplayName());
          }
          }

          System.out.println(">> ID = "+merger.getId()+">>> Done looping over all network interfaces. ");


          System.out.println(">> ID = "+merger.getId()+"status thread sleeping for 15 secs before broadcasting status again");
              // sleep for a while
          try {
              Thread.sleep(1000);
          } catch (InterruptedException e) { 
             e.printStackTrace();
          }



          // Close the port!
          c.close();
      } catch (IOException ex) {
          System.out.println(ex.getStackTrace());
          
      }
  	}
  }
  
  
}
