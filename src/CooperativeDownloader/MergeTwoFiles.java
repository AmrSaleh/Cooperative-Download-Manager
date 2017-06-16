package CooperativeDownloader;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;

//http://luugiathuy.com/2011/03/download-manager-java/
//http://www.hko.gov.hk/gts/time/calendar/pdf/2016e.pdf
public class MergeTwoFiles {

    public static void main(String[] args) throws IOException {

	String fileName = "2016e.pdf";
	mergeFile(fileName);

	/*
	InputStream in = new FileInputStream(new File("2016e.pdf/0_131071"));
	byte[] str = IOUtils.toByteArray(in);
	
	System.out.println(Arrays.toString(str).replaceAll("\\[|\\]| ", ""));
	String a = str[10]+"";
	System.out.println(a);
	System.out.println(Byte.parseByte(a));
	 
	 * Path path = Paths.get("2016e.pdf/0_131071"); byte[] data =
	 * Files.readAllBytes(path); System.out.println(Arrays.toString(str));
	 */
    }

    private static void mergeFile(String fileName) throws IOException {
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

    private static void appendChunk(RandomAccessFile output, File chunk)
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