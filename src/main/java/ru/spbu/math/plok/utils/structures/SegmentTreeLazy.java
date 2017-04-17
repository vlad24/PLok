package ru.spbu.math.plok.utils.structures;

public class SegmentTreeLazy {
	
	private static final int DEFAULT_BUFFER = 0;

	private static class TreeElement {
		public long buffer;
		public boolean buffered;
		public int leftControl;
		public int rightControl;
		//
		public long value;

		//
		public TreeElement(long newVal, int leftControl, int rightControl) {
			super();
			this.value = newVal;
			this.leftControl = leftControl;
			this.rightControl = rightControl;
			this.buffered = false;
			this.buffer = DEFAULT_BUFFER;
		}

		@Override
		public String toString() {
			return "TreeElement [buffer=" + buffer + ", buffered=" + buffered + ", leftControl=" + leftControl
					+ ", rightControl=" + rightControl + ", value=" + value + "]";
		}
	}
	private int actualElementAmount;
	private int leafLineStart;
	private TreeElement[] treeElements;
	private boolean fixed;
	
	public SegmentTreeLazy(long[] initialArray) {
		fixed = false;
		actualElementAmount = initialArray.length;
		Integer twoPowerInf = Integer.highestOneBit(actualElementAmount);
		Integer leafLineLength = ((actualElementAmount & (actualElementAmount - 1)) == 0) ? twoPowerInf : (twoPowerInf << 1);
		int totalCapacity = (leafLineLength << 1) - 1;
		treeElements = new TreeElement[totalCapacity];
		leafLineStart = totalCapacity - leafLineLength;
		for (int i = totalCapacity - 1; i >= 0; i--) {
			if (i >= totalCapacity - (leafLineLength - actualElementAmount)) {
				treeElements[i] = new TreeElement(0, i - leafLineStart, i - leafLineStart);
			} else if (i >= totalCapacity - leafLineLength) {
				treeElements[i] = new TreeElement(initialArray[i - leafLineStart], i - leafLineStart, i - leafLineStart);
			} else {
				long newVal = sum(treeElements[2 * i + 1], treeElements[2 * i + 2]);
				int newLeft = treeElements[2 * i + 1].leftControl;
				int newRight = treeElements[2 * i + 2].rightControl;
				treeElements[i] = new TreeElement(newVal, newLeft, newRight);
			}
		}
	}

	public void commitUpdates() {
		recursivelyCommitUpdates(0);
		fixed = true;
	}

	public long getSumAt(int left, int right) {
		// System.out.println(" + Sum query for ["+left + " ; "+ right +"]");
		if ((left <= right) && (left >= 0) && (right < actualElementAmount)) {
			return recursivelySumAt(0, left, right);
		} else
			return Long.MIN_VALUE;
	}
	
	public void incrementAt(int leftBound, int rightBound) {
		recursivelyIncrementAt(0, leftBound, rightBound);
	}
	
	private long recursivelyIncrementAt(int vertex, int leftBound, int rightBound) {
		TreeElement current = treeElements[vertex];
		// look if current node is the target segment
		if ((current.leftControl == leftBound) && (current.rightControl == rightBound)) {
			// here we use lazy approach
			if (isLeaf(vertex)) {
				// no need to buffer, update straight
				current.value += 1;
				return current.value;
			} else {
				// update the value, set the buffer that will be pushed later!
				// LAZY!
				current.buffered = true;
				current.buffer  += 1;
				current.value   += rightBound - leftBound + 1;
				return current.value;
			}
			// need to go deeper!
		} else {
			// To save some old changes push them down, updating values.
			pushBufferDeeper(vertex);
			int leftSon   = 2 * vertex + 1;
			int rightSon  = 2 * vertex + 2;
			long newLeft  = treeElements[leftSon].value;
			long newRight = treeElements[rightSon].value;
			// Look if left son needs to be updated if it intersects with target segment.
			if (treeElements[leftSon].rightControl >= leftBound) {
				newLeft = recursivelyIncrementAt(leftSon, leftBound, Math.min(treeElements[leftSon].rightControl, rightBound));
			}
			// Look if right son needs to be updated if it intersects with target segment.
			if (treeElements[rightSon].leftControl <= rightBound) {
				newRight = recursivelyIncrementAt(rightSon, Math.max(treeElements[rightSon].leftControl, leftBound), rightBound);
			}
			current.value = newLeft + newRight;
			return current.value;
		}
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder(">>\n");
		b.append(treeElements[0].value).append("(").append(treeElements[0].buffer).append(")| \n");
		int start = 1;
		int l = 2;
		while (l < treeElements.length) {
			for (int i = start; i < start + l; i++) {
				TreeElement current = treeElements[i];
				b.append(current.value).append("(").append(current.buffer).append(")|\t");
			}
			start += l;
			l = l << 1;
			b.append("\n");
		}
		return b.append("<< \n").toString();
	}
	
	public Long[] getCleanLeafLine(){
		if (fixed){
			Long[] result = new Long[actualElementAmount];
			for (int i = 0; i < actualElementAmount; i++ ){
				result[i] = treeElements[leafLineStart + i].value;
			}
			return result;
		}else{
			throw new IllegalStateException("Updates not commited");
		}
	}


	private boolean isLeaf(int vertexNumber) {
		return treeElements[vertexNumber].leftControl == treeElements[vertexNumber].rightControl;
	}

	private void pushBufferDeeper(int vertex) {
		TreeElement current = treeElements[vertex];
		if (current.buffered) {
			int leftSon  = 2 * vertex + 1;
			int rightSon = 2 * vertex + 2;
			// if children are leaves - no need to buffer them, set the values from parent buffer
			if (isLeaf(leftSon)) {
				treeElements[leftSon].value  += current.buffer;
				treeElements[rightSon].value += current.buffer;
			} else {
				// push changes to left son and set the correct value here
				treeElements[leftSon].buffered = true;
				treeElements[leftSon].buffer   += current.buffer;
				treeElements[leftSon].value    += current.buffer *  (treeElements[leftSon].rightControl - treeElements[leftSon].leftControl + 1);
				// buffer the right son, set the correct value now
				treeElements[rightSon].buffered = true;
				treeElements[rightSon].buffer   += current.buffer;
				treeElements[rightSon].value    += current.buffer * (treeElements[rightSon].rightControl - treeElements[rightSon].leftControl + 1);
			}
			// unbuffer the parent, it's value is already correct
			current.buffered = false;
			current.buffer = DEFAULT_BUFFER;
		}
	}
	//
	
	private void recursivelyCommitUpdates(int vertex) {
		if (isLeaf(vertex)) {
			return;
		} else if (treeElements[vertex].buffered) {
			int segmentStart = treeElements[vertex].leftControl;
			int segmentEnd = treeElements[vertex].rightControl;
			for (int j = segmentStart; j <= segmentEnd; j++) {
				treeElements[leafLineStart + j].value = treeElements[vertex].buffer;
			}
			treeElements[vertex].buffered = false;
			treeElements[vertex].buffer = DEFAULT_BUFFER;
		} else {
			recursivelyCommitUpdates(2 * vertex + 1);
			recursivelyCommitUpdates(2 * vertex + 2);
		}
	}

	private long recursivelySumAt(int vertexNumber, int left, int right) {
		TreeElement current = treeElements[vertexNumber];
		if ((current.leftControl == left) && (current.rightControl == right)) {
			return current.value;
		} else {
			pushBufferDeeper(vertexNumber);
			long leftSum = 0;
			long rightSum = 0;
			if (left <= treeElements[2 * vertexNumber + 1].rightControl) {
				leftSum = recursivelySumAt(2 * vertexNumber + 1, left,
						Math.min(treeElements[2 * vertexNumber + 1].rightControl, right));
			}
			if (right >= treeElements[2 * vertexNumber + 2].leftControl) {
				rightSum = recursivelySumAt(2 * vertexNumber + 2,
						Math.max(treeElements[2 * vertexNumber + 2].leftControl, left), right);
			}
			return leftSum + rightSum;
		}
	}
	private long sum(TreeElement a, TreeElement b) {
		return a.value + b.value;
	}
	
	
}
