package com.luugiathuy.apps.downloadmanager;




/**
Copyright (c) 2011-present - Luu Gia Thuy

Permission is hereby granted, free of charge, to any person
obtaining a copy of this software and associated documentation
files (the "Software"), to deal in the Software without
restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following
conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.
*/

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;

public class HttpDownloader extends Downloader {

    public HttpDownloader(URL url, String outputFolder, int numConnections) {
	super(url, outputFolder, numConnections);
	mOutputFolder += mFileName+"/";
	System.out.println(mOutputFolder);
	new File(mOutputFolder).mkdir();
	download();
    }

    public HttpDownloader(URL url, String outputFolder, int numConnections
	    , long startAddress, long endAddress,int sessionID,int clientID,long chunkID,long fileSize ) {
	super(url, outputFolder, numConnections,startAddress,endAddress,sessionID,clientID,chunkID,fileSize );
	mOutputFolder += mFileName+"/";
	System.out.println(mOutputFolder);
	new File(mOutputFolder).mkdir();
	download();
    }

    private void error() {
	System.out.println("ERROR");
	setState(ERROR);
    }

    
    public void run() {
	HttpURLConnection conn = null;
	try {
	    // Open connection to URL
	    conn = (HttpURLConnection) mURL.openConnection();
	    conn.setConnectTimeout(10000);

	    // Connect to server
	    conn.connect();

	    // Make sure the response code is in the 200 range.
	    if (conn.getResponseCode() / 100 != 2) {
		error();
	    }

	    // Check for valid content length.
	    int contentLength = conn.getContentLength();
	    if (contentLength < 1) {
		error();
	    }

	    if (mFileSize == -1) {
		mFileSize = (int) (endAddress - startAddress + 1);
		stateChanged();
		System.out.println("Chunk size: " + mFileSize);
	    }

	    // if the state is DOWNLOADING (no error) -> start downloading
	    if (mState == DOWNLOADING) {
		// check whether we have list of download threads or not, if not
		// -> init download
		if (mListDownloadThread.size() == 0) {
		    if (mFileSize > MIN_DOWNLOAD_SIZE && false) {
			// downloading size for each thread
			int partSize = Math
				.round(((float) mFileSize / mNumConnections)
					/ BLOCK_SIZE)
				* BLOCK_SIZE;
			System.out.println("Part size: " + partSize);

			// start/end Byte for each thread
			int startByte = 0;
			int endByte = partSize - 1;
			int end = endByte;
			if (mFileSize < endByte) {
			    end = mFileSize;
			}
			HttpDownloadThread aThread = new HttpDownloadThread(1,
				mURL,  mOutputFolder + startByte + "_" + end,
				startByte, endByte);
			mListDownloadThread.add(aThread);
			int i = 2;
			while (endByte < mFileSize) {
			    startByte = endByte + 1;
			    endByte += partSize;
			    end = endByte;
			    if (mFileSize < endByte) {
				end = mFileSize;
			    }
			    aThread = new HttpDownloadThread(i,
				    mURL, mOutputFolder + startByte + "_" + end,
				    startByte, endByte);
			    mListDownloadThread.add(aThread);
			    ++i;
			}
		    } else {
			HttpDownloadThread aThread = new HttpDownloadThread(1,
				mURL, mOutputFolder +  startAddress + "_" + endAddress,
				(int)startAddress,(int) endAddress);
			mListDownloadThread.add(aThread);
		    }
		} else { // resume all downloading threads
		    for (int i = 0; i < mListDownloadThread.size(); ++i) {
			if (!mListDownloadThread.get(i).isFinished())
			    mListDownloadThread.get(i).download();
		    }
		}

		// waiting for all threads to complete
		for (int i = 0; i < mListDownloadThread.size(); ++i) {
		    mListDownloadThread.get(i).waitFinish();
		}

		// check the current state again
		if (mState == DOWNLOADING) {
		    setState(COMPLETED);
//                    String host = "localhost";
		    String host = "192.168.0.117";
                  /** Define a port */
                  int port = 19999;

                  StringBuffer instr = new StringBuffer();
                  String TimeStamp;
                  System.out.println("SocketClient initialized");
                  try {
                          /** Obtain an address object of the server */
                          InetAddress address = InetAddress.getByName(host);
                          /** Establish a socket connetion */
                          Socket connection = new Socket(address, port);
                          /** Instantiate a BufferedOutputStream object */
                          BufferedOutputStream bos = new BufferedOutputStream(
                                          connection.getOutputStream());

                          /**
                           * Instantiate an OutputStreamWriter object with the optional
                           * character encoding.
                           */
                          OutputStreamWriter osw = new OutputStreamWriter(bos, "US-ASCII");
                          TimeStamp = new java.util.Date().toString();
                          String process;
                  /*Start process*/
                          //process = "start"+(char)0+"http://www.hko.gov.hk/gts/time/calendar/pdf/2016e.pdf"+(char)0;
                  /*Acknowledge chunk process process*/
                          process = "ack"+(char)0+sessionID + " " + clientID + " " + chunkID+(char)0;
                  /*Participate in session process*/
                          //process = "participate"+(char)0+"1"+(char)0;
                  /*RequestChunk process*/
                          //process = "requestChunk"+(char)0+"1 1"+(char)0;
                          /** Write across the socket connection and flush the buffer */
                          osw.write(process);
                          osw.flush();
                          /**
                           * Instantiate a BufferedInputStream object for reading /**
                           * Instantiate a BufferedInputStream object for reading incoming
                           * socket streams.
                           */

                          BufferedInputStream bis = new BufferedInputStream(
                                          connection.getInputStream());
                          /**
                           * Instantiate an InputStreamReader with the optional character
                           * encoding.
                           */

                          InputStreamReader isr = new InputStreamReader(bis, "US-ASCII");

                          /** Read the socket's InputStream and append to a StringBuffer */
                          int c;
                          while ((c = isr.read()) != 0)
                                  instr.append((char) c);

                          /** Close the socket connection. */
                          connection.close();
                          System.out.println(instr);
                  } catch (IOException f) {
                          System.out.println("IOException: " + f);
                  } catch (Exception g) {
                          System.out.println("Exception: " + g);
                  }
                  
                  
		}
	    }
	} catch (Exception e) {
	    error();
	} finally {
	    if (conn != null)
		conn.disconnect();
	}
    }

    /**
     * Thread using Http protocol to download a part of file
     */
    private class HttpDownloadThread extends DownloadThread {

	/**
	 * Constructor
	 * 
	 * @param threadID
	 * @param url
	 * @param outputFile
	 * @param startByte
	 * @param endByte
	 */
	public HttpDownloadThread(int threadID, URL url, String outputFile,
		int startByte, int endByte) {
	    super(threadID, url, outputFile, startByte, endByte);
	}

	
	public void run() {
	    BufferedInputStream in = null;
	    RandomAccessFile raf = null;

	    try {
		// open Http connection to URL
		HttpURLConnection conn = (HttpURLConnection) mURL
			.openConnection();

		// set the range of byte to download
		String byteRange = mStartByte + "-" + mEndByte;
		conn.setRequestProperty("Range", "bytes=" + byteRange);
		System.out.println("bytes=" + byteRange);

		// connect to server
		conn.connect();

		// Make sure the response code is in the 200 range.
		if (conn.getResponseCode() / 100 != 2) {
		    error();
		}
		// get the input stream
		in = new BufferedInputStream(conn.getInputStream());

		// open the output file and seek to the start location
		raf = new RandomAccessFile(mOutputFile, "rw");
		raf.seek(0);

		byte data[] = new byte[BUFFER_SIZE];
		int numRead;
		while ((mState == DOWNLOADING)
			&& ((numRead = in.read(data, 0, BUFFER_SIZE)) != -1)) {
		    // write to buffer
		    raf.write(data, 0, numRead);
		    // increase the startByte for resume later
		    mStartByte += numRead;
		    // increase the downloaded size
		    downloaded(numRead);
		}

		if (mState == DOWNLOADING) {
		    mIsFinished = true;
		}
	    } catch (IOException e) {
		error();
	    } finally {
		if (raf != null) {
		    try {
			raf.close();
		    } catch (IOException e) {
		    }
		}

		if (in != null) {
		    try {
			in.close();
		    } catch (IOException e) {
		    }
		}
	    }

	    System.out.println("End thread " + mThreadID);
	}
    }
}
