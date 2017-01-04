package ru.spbu.math.plok.model.storagesystem;

public class BlockHeader {
	private long id;
	private long tBeg;
	private long tEnd;
	private long iBeg;
	private long iEnd;
	
	public BlockHeader(long id) {
		super();
		this.id = id;
		this.tBeg = -1L;
		this.tEnd = -1L;
		this.iBeg = -1L;
		this.iEnd = -1L;
	}

	@Override
	public String toString() {
		return "BlockHeader [id=" + id + ", tBeg=" + tBeg + ", tEnd=" + tEnd + ", iBeg=" + iBeg + ", iEnd=" + iEnd
				+ "]";
	}

	public BlockHeader(long id, long tBeg, long tEnd, long iBeg, long iEnd) {
		super();
		this.id = id;
		this.tBeg = tBeg;
		this.tEnd = tEnd;
		this.iBeg = iBeg;
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

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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

	public long getiBeg() {
		return iBeg;
	}

	public void setiBeg(long iBeg) {
		this.iBeg = iBeg;
	}

	public long getiEnd() {
		return iEnd;
	}

	public void setiEnd(long iEnd) {
		this.iEnd = iEnd;
	}

	

	
	
}
