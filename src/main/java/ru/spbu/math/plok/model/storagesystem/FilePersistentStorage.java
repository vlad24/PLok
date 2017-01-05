package ru.spbu.math.plok.model.storagesystem;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ru.spbu.math.plok.model.generator.Vector;

/**
 * Persists data to file system.
 */
public class FilePersistentStorage {

	private static final Logger log = LoggerFactory.getLogger(FilePersistentStorage.class);
	private static final String STORAGE_FILE_NAME_FORMAT = "storage_%d";
	private final int blockSize;
	private final String storagePath;
	private int P;
	private int L;
	private int P_S;
	private int L_S;
	private long blockID;
	private ByteBuffer writeBuffer;
	private final RandomAccessFile raf;
	private final FileChannel channel;

	@Inject
	public FilePersistentStorage(@Named("storagePath") String storagePath, @Named("N") int N, @Named("P") int P, @Named("L") int L) throws IOException {
		super();
		this.storagePath = Paths.get(storagePath).toAbsolutePath().toString();
		this.P 		= P;
		this.L 		= L;
		this.L_S 	= N % L;
		this.P_S 	= P * L / L_S;
		this.blockSize = 
				1 + 										//additional byte to store whether block is special or common
				BlockHeader.BYTES +							//header
				P * L * Float.BYTES + L * Long.BYTES; 		//vectors and their timestamps (data)
		File storageFile = Paths.get(this.storagePath,
				String.format(STORAGE_FILE_NAME_FORMAT, System.currentTimeMillis())).toFile();
		storageFile.getParentFile().mkdirs();
		raf = new RandomAccessFile(storageFile, "rw");
		writeBuffer = ByteBuffer.allocateDirect(blockSize);
		blockID = storageFile.length() / blockSize;
		channel = raf.getChannel();
		channel.position(channel.size());
		log.debug("Persistent storage file has been created at {} with initial block id={}", storageFile.getAbsolutePath(), blockID);
	}

	public long add(Block block) throws IOException {
		return writeBytes(toBytes(block));
	}

	public Block get(long id) throws IOException {
		return fromBytes(readBytes(id));
	}
	
	public long writeBytes(ByteBuffer block) throws IOException {
		blockID++;
		writeBuffer.put(block).flip();
		while (writeBuffer.hasRemaining()) {
			channel.write(writeBuffer);
		}
		writeBuffer.clear();
		return blockID;
	}

	  public ByteBuffer readBytes(long blockID) throws IOException {
		ByteBuffer resultBuffer = ByteBuffer.allocate(blockSize);
	    channel.read(resultBuffer, blockID * blockSize);
	    return resultBuffer;
	  }
	  
		private ByteBuffer toBytes(Block block) {
			ByteBuffer result = ByteBuffer.allocate(blockSize);
			result.put((byte) (block.getData().size() == L ? 1 : 0));
			result.putInt(block.getHeader().getId())
					.putInt(block.getHeader().getiBeg())
					.putInt(block.getHeader().getiEnd())
					.putLong(block.getHeader().gettBeg())
					.putLong(block.getHeader().gettEnd());
			for (Vector v: block.getData()){
				for (Float m : v.getVector()){
					result.putFloat(m);
				}
				result.putLong(v.getTimestamp());
			}
			return result;
		}
		
		private Block fromBytes(ByteBuffer bytes) {
			byte isCommon = bytes.get();
			BlockHeader header = new BlockHeader(bytes.getInt());
			header.setiBeg(bytes.getInt());
			header.setiEnd(bytes.getInt());
			header.settBeg(bytes.getLong());
			header.settEnd(bytes.getLong());
			Block result = (isCommon == 1) ? new Block(P, L) : new Block(P_S, L_S);   
			result.setHeader(header);
			while (bytes.hasRemaining()){
				int vectorLength = (isCommon == 1) ? L : L_S;
				float[] values = new float[vectorLength];
				for (int i = 0; i < vectorLength; i++){
						values[i] = bytes.getFloat();
				}
				result.tryAdd(new Vector(bytes.getLong(), values));
			}
			return result;
		}
	
	

	public void close() throws IOException {
		channel.force(false);
		raf.close();
	}

}
