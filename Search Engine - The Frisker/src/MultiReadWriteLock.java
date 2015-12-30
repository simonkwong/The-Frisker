
/**
 * A simple custom lock that allows simultaneously read operations, but
 * disallows simultaneously write and read/write operations.
 *
 * You do not need to implement any form or priority to read or write
 * operations. The first thread that acquires the appropriate lock should be
 * allowed to continue.
 *
 * @author Simon Kwong
 */
public class MultiReadWriteLock {
	
	private int readers;
	private int writers;
	
	/**
	 * Initializes a multi-reader (single-writer) lock.
	 */
	public MultiReadWriteLock() {
		
		this.readers = 0;
		this.writers = 0;
	}

	/**
	 * Will wait until there are no active writers in the system, and then will
	 * increase the number of active readers.
	 */
	public synchronized void lockRead() {
		
		while(writers > 0) {
			
			try {
				
				this.wait();
			}
			catch (InterruptedException ie) {
				
				System.out.println("Interrupted");
			}
		}
		readers++;
	}

	/**
	 * Will decrease the number of active readers, and notify any waiting
	 * threads if necessary.
	 */
	public synchronized void unlockRead() {
		
		readers--;
		
		if(writers == 0) {
			notifyAll();
		}
	}

	/**
	 * Will wait until there are no active readers or writers in the system, and
	 * then will increase the number of active writers.
	 */
	public synchronized void lockWrite() {
		
		while((readers > 0) || (writers > 0)) {
			
			try {
			
				this.wait();
			}
			catch (InterruptedException ie) {
				
				System.out.println("Interrupted");
			}
		}
		writers++;
	}

	/**
	 * Will decrease the number of active writers, and notify any waiting
	 * threads if necessary.
	 */
	public synchronized void unlockWrite() {
		
		writers--;
		notifyAll();
	}
}
