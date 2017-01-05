package ru.spbu.math.plok.model.storagesystem;

import java.util.ArrayList;

import ru.spbu.math.plok.model.generator.Vector;

public class Block {
	
	private BlockHeader header;
	private int P;
	private int L;
	private ArrayList<Vector> data;
	
	public Block(int P, int L) {
		this.L = L;
		this.P = P;
		data = new ArrayList<Vector>(P);
	}
	
	public boolean tryAdd(Vector vector){
		if (vector.getLength() != L){
			throw new IllegalArgumentException("Incorrect length!");
		}else if (data.size() == P){
			throw new IllegalStateException("Filled Block!");
		}
		data.add(vector);
		return isFull();
	}
	
	public boolean isFull(){
		return data.size() >= P;
	}
	
	
	public void autoFillHeader(int id, int iBeg){
		this.header = new BlockHeader(id, data.get(0).getTimestamp(), data.get(data.size() - 1).getTimestamp(), iBeg, iBeg + L - 1);
	}
	
	public BlockHeader getHeader() {
		return header;
	}
	
	public void setHeader(BlockHeader header) {
		this.header = header;
	}

	@Override
	public String toString() {
		return "Block [header=" + header + ", P=" + P + ", L=" + L +  ", vectorsInside(" + data.size() +")]";
	}

	public ArrayList<Vector> getData() {
		return data;
	}

}
