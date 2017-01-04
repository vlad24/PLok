package ru.spbu.math.plok.model.storagesystem;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;

/**
 * Persists data to file system.
 */
public class FilePersistentStorage {

	private static final Logger log = LoggerFactory.getLogger(FilePersistentStorage.class);
	private static final String PERSISTER_MAIN_FILE_NAME_FORMAT = "persister_%d";
	private final int blockSize;
	private final String storagePath;
	private int id;
	private int P;
	private int L;
	private long blockID;
	private ByteBuffer writeBuffer;
	private FileHandler mainFile;

	@Inject
	public FilePersistentStorage(@Named("storagePath") String storagePath, @Named("N") int N, @Named("P") int P, @Named("L") int L) throws IOException {
		super();
		this.storagePath = Paths.get(storagePath, "files").toAbsolutePath().toString();
		this.P = P;
		this.L = L;
		int P_S = N % P;
		int L_S = N % L;
		this.blockSize = 1 + Math.max(P * L * Float.BYTES, P_S * L_S *Float.BYTES); //additional byte to store whether block is special
		writeBuffer = ByteBuffer.allocateDirect(P);
		blockID = -1;
		File file = getFile(PERSISTER_MAIN_FILE_NAME_FORMAT);
		if (file.exists()) {
			blockID = (file.length() / P) - 1;
		}
		log.info("Initialized persister. Initial data : {}.", blockID + 1);
		if (mainFile == null) {
				if (mainFile == null) {
					mainFile = initializeStorage(PERSISTER_MAIN_FILE_NAME_FORMAT);
					mainFile.channel.position(mainFile.channel.size());
				}
			}
	}

	private File getFile(String namePattern) {
		return new File(Paths.get(storagePath, String.format(namePattern, this.id)).toString());
	}

	private FileHandler initializeStorage(String namePattern) throws IOException {
		File file = getFile(namePattern);
		file.getParentFile().mkdirs();
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		FileChannel channel = raf.getChannel();
		log.debug("Persistent storage file {} has been created at {}", this.id, file.getAbsolutePath());
		return new FileHandler(raf, channel);
	}

	
	public long add(Block block) throws IOException {
		return add(toBytes(block));
	}

	public Block get(long id) throws IOException {
		return fromBytes(getByteBlock(id));
	}
	
	private byte[] toBytes(Block block) {
		byte[] result = new byte[blockSize];
		result[0] = (byte) (block.getData().size() == L ? 1 : 0);
		return result;
	}
	private Block fromBytes(byte[] bytes) {
		// TODO Auto-generated method stub
		return null;
	}
	

	public long add(byte[] block) throws IOException {
		blockID++;
		writeBuffer.put(block).flip();
		while (writeBuffer.hasRemaining()) {
			mainFile.channel.write(writeBuffer);
		}
		writeBuffer.clear();
		return blockID;
	}

	  public byte[] getByteBlock(long blockID) throws IOException {
		ByteBuffer resultBuffer = ByteBuffer.allocate(blockSize);
	    mainFile.channel.read(resultBuffer, blockID * blockSize);
	    return resultBuffer.array();
	  }
	
	

	public void close() throws IOException {
		if (mainFile != null) {
			mainFile.close();
		}
	}

	private static class FileHandler implements Closeable {

		private final RandomAccessFile raf;
		private final FileChannel channel;

		FileHandler(RandomAccessFile raf, FileChannel channel) {
			this.raf = raf;
			this.channel = channel;
		}

		@Override
		public void close() throws IOException {
			channel.force(false);
			raf.close();
		}
	}
}
