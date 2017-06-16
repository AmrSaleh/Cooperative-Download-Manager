package CooperativeDownloader;
import java.util.HashSet;

public class Participant {

    private long id;
    private HashSet<Long> chunksBeginings; // Contain the start of each chunk
					   // belong to this participant
    private boolean finishedSending;

    public Participant(long id, HashSet<Long> chunksBeginings, boolean finished) {
	this.id = id;
	this.chunksBeginings = chunksBeginings;
	setFinishedSending(finished);
    }

    public long getId() {
	return id;
    }

    public void setId(long id) {
	this.id = id;
    }

    public HashSet<Long> getAvailableChunks() {
	return chunksBeginings;
    }

    public boolean hasChunk(long chunkStartByte) {
	return chunksBeginings.contains(chunkStartByte);
    }

    public boolean isFinishedSending() {
	return finishedSending;
    }

    public void setFinishedSending(boolean finishedSending) {
	this.finishedSending = finishedSending;
    }
}
