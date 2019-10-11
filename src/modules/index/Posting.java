package modules.index;

import java.util.ArrayList;
import java.util.List;

/**
 * A Posting encapsulates a document ID associated with a search query component.
 * wdt 	- w  d, t : the weight associated with the document contained in the posting..
 */
public class Posting {
	private int mDocumentId;
	private List<Integer> mPositionIds = new ArrayList<Integer>();
	private double mwdt;

	public Posting(int documentId, List<Integer> positions, double wdt) {
		mDocumentId = documentId;
		mPositionIds = positions;
		mwdt = wdt;
	}

	public Posting(int documentId, List<Integer> positions) {
		mDocumentId = documentId;
		mPositionIds = positions;
	}

	public Posting(int documentId) {
		mDocumentId = documentId;
	}

	public int getDocumentId() {
		return mDocumentId;
	}

	public List<Integer> getPositionsInDoc() {
		return mPositionIds;
	}

	public double getwdt(){return  mwdt;}
}
