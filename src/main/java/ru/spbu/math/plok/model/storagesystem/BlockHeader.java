package ru.spbu.math.plok.model.storagesystem;

public class BlockHeader {
	
	public static int BYTES = 2 * Integer.BYTES + 3 * Long.BYTES; 
	
	private long id;
	private long tBeg;
	private long tEnd;
	private int iBeg;
	private int iEnd;
	
	public BlockHeader(int id) {
		super();
		this.id = id;
		this.tBeg = -1L;
		this.tEnd = -1L;
		this.iBeg = -1;
		this.iEnd = -1;
	}

	@Override
	public String toString() {
		return "BlockHeader [id=" + id + ", tBeg=" + tBeg + ", tEnd=" + tEnd + ", iBeg=" + iBeg + ", iEnd=" + iEnd
				+ "]";
	}

	public BlockHeader(long id, long tBeg, long tEnd, int iBeg, int iEnd) {
		super();
		this.id = id;
		this.tBeg = tBeg;
		this.tEnd = tEnd;
		this.iBeg = iBeg;
		this.iEnd = iEnd;
	}


	public long getId() {
		return id;
	}

	public long gettBeg() {
		return tBeg;
	}

	public void settBeg(long tBeg) {
		this.tBeg = tBeg;
	}

	public long gettEnd() {
		return tEnd;
	}

	public void settEnd(long tEnd) {
		this.tEnd = tEnd;
	}

	public int getiBeg() {
		return iBeg;
	}

	public void setiBeg(int iBeg) {
		this.iBeg = iBeg;
	}

	public int getiEnd() {
		return iEnd;
	}

	public void setiEnd(int iEnd) {
		this.iEnd = iEnd;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (iBeg ^ (iBeg >>> 32));
		result = prime * result + (int) (iEnd ^ (iEnd >>> 32));
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + (int) (tBeg ^ (tBeg >>> 32));
		result = prime * result + (int) (tEnd ^ (tEnd >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BlockHeader other = (BlockHeader) obj;
		if (iBeg != other.iBeg)
			return false;
		if (iEnd != other.iEnd)
			return false;
		if (id != other.id)
			return false;
		if (tBeg != other.tBeg)
			return false;
		if (tEnd != other.tEnd)
			return false;
		return true;
	}
	
}
