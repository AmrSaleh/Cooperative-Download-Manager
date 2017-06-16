package CooperativeDownloader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;


public class Chunk{

    private long start,end;
    private Merger merger;
    
    public Chunk(long start,long end,Merger merger){
      this.start = start;
      this.end = end;
      this.merger = merger;
    }
    
    public long getStart(){
      return start;
    }
    
    public long getEnd(){
	      return end;
	    }
    public byte[] getChunkData(){
      InputStream in;
      byte[] data = null;
    try {
	in = new FileInputStream(new File(merger.getFileName()+"/"+start+"_"+end));
	 data = IOUtils.toByteArray(in);
    } catch (FileNotFoundException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
    } catch (IOException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
    }
     
//      return Arrays.toString(data).replaceAll("\\[|\\]| ", "");
    return data;
    }

}
