package CooperativeDownloader;


import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.*;
import java.util.*;

public class MultipleSocketServer implements Runnable {

	private Socket connection;
	private String TimeStamp;
	private int ID;
	static String serverDataFolder = "server_data/";
	static String logFile = "server_data/log.dat";

	public static void main(String[] args) {
		//create log file if it doesn't exist
		File log = new File(logFile);
		log.getParentFile().mkdirs();
		try {
			if (log.exists() == false) {
				System.out.println("File doesn't exist.");
				log.createNewFile();
				PrintWriter out = new PrintWriter(log);
				out.append("1");
				out.close();
			}
		} catch (IOException e) {
			System.out.println("COULD NOT LOG!!");
		}
			

		int port = 19999;
		int count = 0;
		try {
			ServerSocket socket1 = new ServerSocket(port);
			System.out.println("MultipleSocketServer Initialized");
			while (true) {
				Socket connection = socket1.accept();
				Runnable runnable = new MultipleSocketServer(connection,
						++count);
				Thread thread = new Thread(runnable);
				thread.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	MultipleSocketServer(Socket s, int i) {
		this.connection = s;
		this.ID = i;
	}

	public void run() {
		//boolean for validation of parameters
		boolean paramIsValid = true;
		
		//receive 2 lines: the process code (start, participate etc) and the parameters
		//for that code (url, session ID etc)
		try {
			BufferedInputStream is = new BufferedInputStream(
					connection.getInputStream());
			InputStreamReader isr = new InputStreamReader(is);
			int character;
			StringBuffer process = new StringBuffer();
			while ((character = isr.read()) != 0) {
				process.append((char) character);
			}
			System.out.println(process);
			StringBuffer parameter = new StringBuffer();
			while ((character = isr.read()) != 0) {
				parameter.append((char) character);
			}
			System.out.println(parameter);
			// need to wait 10 seconds to pretend that we're processing
			// something
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
			}
			// string containing return message to client
			String returnCode = null;
			
			/*
			 * Start process: Read log file, it should contain next available session ID.
			 * Create a new session log file, it should contain client ID, currently assigned
			 * chunks and their status, as well as file size and number of chunks and
			 * probably downloaded chunks etc. The client should receive session ID, client ID,
			 * file size, chunk size, total number of chunks, assigned chunks.
			 * */
			if (process.toString().compareTo("start") == 0) {
				URL downloadURL = new URL(parameter.toString());
				long fileSize = getFileSize(downloadURL);
				if(fileSize<1)
				{
					returnCode = "invalid";
				}
				else
				{
					File log = new File(logFile);
					
					BufferedReader br = null;
					String sCurrentLine = null;
					try {
						br = new BufferedReader(new FileReader(logFile));
						sCurrentLine = br.readLine();
							
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						try {
							if (br != null)
								br.close();
						} catch (IOException ex) {
							ex.printStackTrace();
						}
					}
					//create new session
					if(sCurrentLine != null)
					{
						int clientID = 1;
						//write the returnCode parameters split by " "
						returnCode =  sCurrentLine + " " + clientID + " ";
						//add to returnCode the chunks to download separated by ","
						//chunk size = 128KB
						//the program will always assign the first 5 chunks of the file
						int chunkSize = 8*1024;
						int numberOfChunks = (int) Math.ceil((double)fileSize/(double)chunkSize);
						returnCode+=fileSize+" "+ chunkSize + " " + numberOfChunks + " ";
						int assignedChunks;
						if(numberOfChunks<5)
						{
							assignedChunks = numberOfChunks;
						}
						else
						{
							assignedChunks = 5;
						}
						for(int i = 0; i < assignedChunks; i++)
						{
							
							returnCode += (i+1);
							if(i==assignedChunks-1)
							{
								//returnCode+=" ";
							}
							else
							{
								returnCode+=",";
							}
						}
						
						
						//write to log file all the above details
						File session = new File("server_data/"+sCurrentLine+".dat");
						File sessionURL = new File("server_data/"+sCurrentLine+".url");
						try {
							if (session.exists() == false) {
								System.out.println("session doesn't exist.");
								session.createNewFile();
								PrintWriter out = new PrintWriter(new FileWriter(session, true), true);
								out.println("File size: "+ fileSize);
								out.println("Chunk size: "+ chunkSize);
								out.println("Total number of chunks: "+ numberOfChunks);
								out.println("Remaining number of chunks: "+ numberOfChunks);
								out.append("Chunks to download: ");
								for(int i = 0; i < numberOfChunks; i++)
								{
									out.append((i+1)+"");
									if(i==numberOfChunks-1)
									{
										//returnCode+=" ";
									}
									else
									{
										out.append(",");
									}
								}
								out.println();
								if(numberOfChunks<6)
								{
									out.println("Next chunk to be assigned: 1");
								}
								else
								{
									out.println("Next chunk to be assigned: 6");
								}
								out.println("Total number of participating clients: 1");
								out.append("1: ");
								for(int i = 0; i < 5 && i < numberOfChunks; i++)
								{
									out.append(i+1+"");
									if(i==numberOfChunks-1||i==4)
									{
										//returnCode+=" ";
									}
									else
									{
										out.append(",");
									}
								}
								out.println();
								out.close();
								
								PrintWriter out2 = new PrintWriter(new FileWriter(sessionURL, true), true);
								out2.println(parameter.toString());
								out2.close();
							}
						} catch (IOException e) {
							System.out.println("COULD NOT LOG!!");
						}
						
						try{
							PrintWriter out = new PrintWriter(log);
							int nextId = Integer.parseInt(sCurrentLine)+1;
							out.append(((Integer)nextId).toString());
							out.close();
						}
						catch (IOException e) {
							System.out.println("COULD NOT LOG!!");
						}
					}
				}
			}
			/*
			 * Ack process: Read session log file, make sure this client was assigned this chunk,
			 * then mark this chunk as downloaded. Parameters received by server are session ID,
			 * client ID and chunk ID. Server replies with ack if the request is valid, and error
			 * if the request is invalid (wrong session ID, client ID or chunk ID)
			 * */
			else if (process.toString().compareTo("ack") == 0) {
				String[] ackParams = parameter.toString().split(" ");
				if(ackParams.length != 3)
				{
					paramIsValid = false;
					System.out.println("Wrong number of parameters");
					returnCode = "error";
				}
				else
				{
					File f = new File(serverDataFolder+ackParams[0]+".dat");
					if(!f.exists() || f.isDirectory()) { 
						paramIsValid = false;
						System.out.println("Invalid session ID");
						returnCode = "error";
					}
					else
					{
					
						BufferedReader br = null;
						String sCurrentLine = null;
						try {
							br = new BufferedReader(new FileReader(f));
							for(int i = 0; i < 6+Integer.parseInt(ackParams[1]); i++)
							{
								br.readLine();
							}
							sCurrentLine = br.readLine();
						} catch (IOException e) {
							paramIsValid = false;
							returnCode = "error";
							e.printStackTrace();
						} finally {
							try {
								if (br != null)
									br.close();
							} catch (IOException ex) {
								ex.printStackTrace();
							}
						}
						if(paramIsValid)
						{
							String[] clientInfo = sCurrentLine.split(":");
							if(clientInfo[0].compareTo(ackParams[1])!=0)
							{
								paramIsValid = false;
								System.out.println("Invalid client ID");
								returnCode = "error";
							}
							else if(clientInfo[1].compareTo(" ")==0)
							{
								paramIsValid = false;
								System.out.println("Client has no assigned chunks");
								returnCode = "error";
							}
							else
							{
								paramIsValid = false;
								String[] clientChunks = clientInfo[1].trim().split(",");
								for(int i = 0; i < clientChunks.length; i++)
								{
									if(clientChunks[i].compareTo(ackParams[2])==0)
									{
										paramIsValid = true;
									}
								}
							}
						}
					}
				}
				if(paramIsValid)
				{
					Path path = Paths.get (serverDataFolder+ackParams[0]+".dat");
					List<String> lines = Files.readAllLines(path);
					
					String remainingChunks = lines.get(3);
					long remainingChunksNumber = Long.parseLong(remainingChunks.split(":")[1].trim());
					
					
					String chunksToDownload = lines.get(4);
					
					String[] chunksToDownloadSplit= chunksToDownload.split(":");
					String[] chunks = chunksToDownloadSplit[1].trim().split(",");
					String modifiedChunksToDownload = chunksToDownloadSplit[0]+": ";
					
					String nextAssignedChunk= lines.get(5);
					long nextChunk = Long.parseLong(nextAssignedChunk.split(":")[1].trim());
					
					for(int i = 0; i < chunks.length; i++)
					{
						if(chunks[i].compareTo(ackParams[2])!=0)
						{
							modifiedChunksToDownload+=chunks[i]+",";
							
						}
						else
						{
							if(nextChunk == Long.parseLong(chunks[i]))
							{
								if(i==chunks.length-1)
								{
									nextChunk = Long.parseLong(chunks[0]);
									if(i==0)
									{
										nextChunk = 0;
									}
								}
								else
								{
									nextChunk = Long.parseLong(chunks[i+1]);
								}
							}
							remainingChunksNumber--;
						}
					}
					remainingChunks = remainingChunks.split(":")[0]+": "+remainingChunksNumber;
					if(modifiedChunksToDownload.substring(modifiedChunksToDownload.length()-1).compareTo(",")==0 )
					{
						modifiedChunksToDownload = modifiedChunksToDownload.substring(0,modifiedChunksToDownload.length()-1);
					}
					
					
					nextAssignedChunk = nextAssignedChunk.split(":")[0]+": "+nextChunk;
					
					String clientInfo = lines.get(6+Integer.parseInt(ackParams[1]));
					String[] clientInfoSplit= clientInfo.split(":");
					String[] clientInfoChunks = clientInfoSplit[1].trim().split(",");
					String modifiedClientInfo = clientInfoSplit[0]+": ";
					for(int i = 0; i < clientInfoChunks.length; i++)
					{
						if(clientInfoChunks[i].compareTo(ackParams[2])!=0)
						{
							modifiedClientInfo+=clientInfoChunks[i]+",";
							
						}
					}
					if(modifiedClientInfo.substring(modifiedClientInfo.length()-1).compareTo(",")==0)
					{
						modifiedClientInfo = modifiedClientInfo.substring(0,modifiedClientInfo.length()-1);
					}
					
					
					lines.set(3, remainingChunks);
					lines.set(4, modifiedChunksToDownload);
					lines.set(5, nextAssignedChunk); //modify this
					lines.set(6+Integer.parseInt(ackParams[1]), modifiedClientInfo);
					Files.write(path, lines); // You can add a charset and other options too
					
					returnCode = "ack";
				}
				else
				{
					returnCode = "error";
				}
			}
			/*Participate process: a client requests to participate in a session
			 *that already exists. The server should ensure the validity of the session
			 *and add this client to the session and assign chunks to it if possible.
			 *client sends the session ID. Client receives either an error, or their new ID
			 *and information about the file they requested. Additionally, a client 
			 *will either receive some chunk IDs to download, or a message concluding
			 *that the download is over and no more chunks are required.
			 */
			else if (process.toString().compareTo("participate") == 0) {
				String sessionID = parameter.toString();
				File f = new File(serverDataFolder+sessionID+".dat");
				if(!f.exists() || f.isDirectory()) { 
					paramIsValid = false;
					System.out.println("Invalid session ID");
					returnCode = "error";
				}
				else
				{
					BufferedReader br = null;
					String fileSizeString = null;
					String chunkSizeString = null;
					String totalNumOfChunksString = null;
					String remainingNumOfChunksString = null;
					String chunksToDownloadString = null;
					String nextChunkAssignedString = null;
					String totalNumOfClientsString = null;
					try {
						br = new BufferedReader(new FileReader(f));
						fileSizeString = br.readLine();
						chunkSizeString = br.readLine();
						totalNumOfChunksString = br.readLine();
						remainingNumOfChunksString = br.readLine();
						chunksToDownloadString = br.readLine();
						nextChunkAssignedString = br.readLine();
						totalNumOfClientsString = br.readLine();
					} catch (IOException e) {
						paramIsValid = false;
						returnCode = "error";
						e.printStackTrace();
					} finally {
						try {
							if (br != null)
								br.close();
						} catch (IOException ex) {
							ex.printStackTrace();
						}
					}
					if(paramIsValid)
					{
						File f2 = new File(serverDataFolder+sessionID+".url");
						BufferedReader br2 = new BufferedReader(new FileReader(f2));
						String url = br2.readLine();
						br2.close();
						returnCode = url+" ";
						File session = new File("server_data/"+sessionID+".dat");
						PrintWriter out = new PrintWriter(new FileWriter(session, true), true);
						String fileSize = fileSizeString.split(":")[1].trim();
						String chunkSize = chunkSizeString.split(":")[1].trim();
						String totalNumOfChunks = totalNumOfChunksString.split(":")[1].trim();
						String remainingNumOfChunks = remainingNumOfChunksString.split(":")[1].trim();
						
						String[] totalNumOfClientsSplit = totalNumOfClientsString.split(":");
						String totalNumOfClients = totalNumOfClientsSplit[1].trim();
						
						String[] nextChunkAssignedSplit = nextChunkAssignedString.split(":");
						
						int newClientID = Integer.parseInt(totalNumOfClients)+1;
						returnCode += sessionID + " " + newClientID + " " + fileSize + " " + 
						chunkSize + " " + totalNumOfChunks + " ";
						if(Long.parseLong(remainingNumOfChunks)==0)
						{
							returnCode+="complete";
							out.println(newClientID+": ");
							
							Path path = Paths.get (serverDataFolder+sessionID+".dat");
							List<String> lines = Files.readAllLines(path);
							//String modifiedNextChunk = nextChunkAssignedString.split(":")[0]+": "+nextChunkAssigned;
							String modifiedNumberOfClients = totalNumOfClientsString.split(":")[0]+": "+newClientID;
							//lines.set(5, modifiedNextChunk); //modify this
							lines.set(6, modifiedNumberOfClients); //modify this
							Files.write(path, lines);
						}
						else
						{
							long nextChunkAssigned = Long.parseLong(nextChunkAssignedSplit[1].trim());
							int numOfAssignableChunks;
							long remainingNumOfChunksLong = Long.parseLong(remainingNumOfChunks);
							if(remainingNumOfChunksLong<5)
							{
								numOfAssignableChunks = (int) remainingNumOfChunksLong;
							}
							else
							{
								numOfAssignableChunks = 5;
							}
							String[] chunksToDownload = chunksToDownloadString.split(":")[1].trim().split(",");
							long[] chunksToDownloadNumeric= new long[chunksToDownload.length];
							for(int i = 0; i < chunksToDownloadNumeric.length; i++)
							{
								chunksToDownloadNumeric[i] = Long.parseLong(chunksToDownload[i]);
							}
							out.append(newClientID+": ");
							for(int i = 0; i < chunksToDownload.length&&numOfAssignableChunks>0; i++)
							{
								if(chunksToDownloadNumeric[i]>=nextChunkAssigned)
								{
									//add this chunk to the client assigned chunks and write it in the session log
									returnCode+=chunksToDownloadNumeric[i];
									out.append(chunksToDownloadNumeric[i]+"");
									numOfAssignableChunks--;
									if(numOfAssignableChunks!=0)
									{
										returnCode+=",";
										out.append(",");
									}
									else
									{
										if(i == chunksToDownload.length-1)
										{
											nextChunkAssigned = chunksToDownloadNumeric[0];
										}
										else
										{
											nextChunkAssigned = chunksToDownloadNumeric[i+1];
										}
									}
								}
							}
							for(int i = 0; i < chunksToDownload.length&&numOfAssignableChunks>0; i++)
							{
								//add this chunk to the client assigned chunks and write it in the session log
								returnCode+=chunksToDownloadNumeric[i];
								out.append(chunksToDownloadNumeric[i]+"");
								numOfAssignableChunks--;
								if(numOfAssignableChunks!=0)
								{
									returnCode+=",";
									out.append(",");
								}
								else
								{
									if(i == chunksToDownload.length-1)
									{
										nextChunkAssigned = chunksToDownloadNumeric[0];
									}
									else
									{
										nextChunkAssigned = chunksToDownloadNumeric[i+1];
									}
								}
							}
							out.close();
							Path path = Paths.get (serverDataFolder+sessionID+".dat");
							List<String> lines = Files.readAllLines(path);
							String modifiedNextChunk = nextChunkAssignedString.split(":")[0]+": "+nextChunkAssigned;
							String modifiedNumberOfClients = totalNumOfClientsString.split(":")[0]+": "+newClientID;
							lines.set(5, modifiedNextChunk); //modify this
							lines.set(6, modifiedNumberOfClients); //modify this
							Files.write(path, lines);
						}
					}
					
				}
			}
			/*RequestChunk process: a client sends their session ID and client ID, and
			 * should receive at most 5 new chunks to download. The client may receive complete
			 * if all file chunks were acked.
			 */
			else if (process.toString().compareTo("requestChunk") == 0) {
				String[] paramString = parameter.toString().split(" ");
				int clientID = Integer.parseInt(paramString[1]);
				File f = new File(serverDataFolder+paramString[0]+".dat");
				if(!f.exists() || f.isDirectory()) { 
					paramIsValid = false;
					System.out.println("Invalid session ID");
					returnCode = "error";
				}
				else
				{
					
					BufferedReader br = null;
					String remainingNumOfChunksString = null;
					String chunksToDownloadString = null;
					String nextChunkAssignedString = null;
					String totalNumOfClientsString = null;
					String clientChunks = null;
					try {
						br = new BufferedReader(new FileReader(f));
						
						for(int i = 0; i < 3; i++)
						{
							br.readLine();
						}
						remainingNumOfChunksString = br.readLine();
						chunksToDownloadString = br.readLine();
						nextChunkAssignedString = br.readLine();
						totalNumOfClientsString = br.readLine();
						if(Integer.parseInt(totalNumOfClientsString.split(":")[1].trim())<clientID)
						{
							paramIsValid = false;
							returnCode = "error";
						}
						
					} catch (IOException e) {
						paramIsValid = false;
						returnCode = "error";
						e.printStackTrace();
					} finally {
						try {
							for(int i = 0; i < clientID-1; i++)
							{
								br.readLine();
							}
							clientChunks = br.readLine();
							//System.out.println(clientChunks);
							if (br != null)
								br.close();
						} catch (IOException ex) {
							ex.printStackTrace();
						}
					}
					if(paramIsValid)
					{
						long remainingNumOfChunks = Long.parseLong(remainingNumOfChunksString.split(":")[1].trim());
						if(remainingNumOfChunks==0)
						{
							returnCode = "complete";
						}
						else
						{
							String[] chunksToDownload = chunksToDownloadString.split(":")[1].trim().split(",");
							long[] chunksToDownloadNumeric= new long[chunksToDownload.length];
							for(int i = 0; i < chunksToDownloadNumeric.length; i++)
							{
								chunksToDownloadNumeric[i] = Long.parseLong(chunksToDownload[i]);
							}
							String[] nextChunkAssignedSplit = nextChunkAssignedString.split(":");
							long nextChunkAssigned = Integer.parseInt(nextChunkAssignedSplit[1].trim());
							String[] clientChunksSplit = clientChunks.split(":");
							clientChunks+=",";
							String[] clientChunksArray = null;
							long[] clientChunksArrayNumeric = null;
							int clientOldChunks = 0;
							if(clientChunksSplit[1].compareTo(" ")!=0)
							{
								clientChunksArray = clientChunksSplit[1].trim().split(",");
								clientChunksArrayNumeric = new long[clientChunksArray.length];
								for(int i = 0; i < clientChunksArrayNumeric.length; i++)
								{
									clientChunksArrayNumeric[i] = Long.parseLong(clientChunksArray[i]);
								}
							}
							int numOfAssignableChunks = 0;
							if(chunksToDownload.length<5)
							{
								numOfAssignableChunks = (int) chunksToDownload.length;
							}
							else
							{
								numOfAssignableChunks = 5;
							}
							returnCode = "";
							long oldNextChunkAssigned = nextChunkAssigned;
							for(int i = 0; i < chunksToDownload.length&&numOfAssignableChunks>0; i++)
							{
								boolean isDuplicate = false;
								if(chunksToDownloadNumeric[i]>=nextChunkAssigned)
								{
									for(int j = 0; j < clientChunksArrayNumeric.length; j++)
									{
										if(clientChunksArrayNumeric[j]==chunksToDownloadNumeric[i])
										{
											isDuplicate = true;
											System.out.println("Index: "+ i +" is duplicate");
											break;
										}
									}
									if(!isDuplicate)
									{
										//add this chunk to the client assigned chunks and write it in the session log
										returnCode+=chunksToDownloadNumeric[i];
										clientChunks+=chunksToDownloadNumeric[i];
										numOfAssignableChunks--;
										if(numOfAssignableChunks!=0)
										{
											returnCode+=",";
											clientChunks+=",";
										}
										if(i == chunksToDownload.length-1)
										{
											nextChunkAssigned = chunksToDownloadNumeric[0];
										}
										else
										{
											nextChunkAssigned = chunksToDownloadNumeric[i+1];
										}
									}
								}
							}
							boolean isDuplicate = false;
							for(int i = 0; i < chunksToDownload.length&&numOfAssignableChunks>0; i++)
							{
								if(chunksToDownloadNumeric[i]==oldNextChunkAssigned)
								{
									break;
								}
								isDuplicate = false;
								for(int j = 0; j < clientChunksArrayNumeric.length; j++)
								{
									if(clientChunksArrayNumeric[j]==chunksToDownloadNumeric[i])
									{
										System.out.println("Index: "+ i +" is duplicate");
										isDuplicate = true;
										break;
									}
								}
								if(!isDuplicate)
								{
									//add this chunk to the client assigned chunks and write it in the session log
									returnCode+=chunksToDownloadNumeric[i];
									clientChunks+=chunksToDownloadNumeric[i];
									numOfAssignableChunks--;
									if(numOfAssignableChunks!=0)
									{
										returnCode+=",";
										clientChunks+=",";
									}
									if(i == chunksToDownload.length-1)
									{
										nextChunkAssigned = chunksToDownloadNumeric[0];
									}
									else
									{
										nextChunkAssigned = chunksToDownloadNumeric[i+1];
									}
								}
							}
							if(clientChunks.substring(clientChunks.length()-1).compareTo(",")==0)
							{
								clientChunks = clientChunks.substring(0, clientChunks.length()-1);
							}
							if(returnCode.substring(returnCode.length()-1).compareTo(",")==0)
							{
								returnCode = returnCode.substring(0, returnCode.length()-1);
							}
							String modifiedNextChunk = nextChunkAssignedSplit[0]+": "+nextChunkAssigned;
							
							
							Path path = Paths.get (serverDataFolder+paramString[0]+".dat");
							List<String> lines = Files.readAllLines(path);
							
							lines.set(5, modifiedNextChunk); //modify this
							lines.set(6+clientID, clientChunks); //modify this
							Files.write(path, lines);
							
							if(returnCode.compareTo("")==0)
							{
								returnCode = "duplicate";
							}
						}
					}
				}
				
			}
			//TimeStamp = new java.util.Date().toString();
			//returnCode = "MultipleSocketServer repsonded at "+ TimeStamp + (char) 13;
			returnCode+=(char)0;
			BufferedOutputStream os = new BufferedOutputStream(
					connection.getOutputStream());
			OutputStreamWriter osw = new OutputStreamWriter(os, "US-ASCII");
			osw.write(returnCode);
			osw.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (IOException e) {
			}
		}
	}
	private long getFileSize(URL url) {
	    HttpURLConnection conn = null;
	    try {
	        conn = (HttpURLConnection) url.openConnection();
	        conn.setRequestMethod("HEAD");
	        conn.getInputStream();
	        return conn.getContentLength();
	    } catch (IOException e) {
	        return -1;
	    } finally {
	        conn.disconnect();
	    }
	}
}