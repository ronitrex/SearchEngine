package modules.index;

import org.apache.commons.collections4.functors.FalsePredicate;

import javax.print.Doc;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;


public class DiskIndex implements Index {
    private List<String> vocabulary = new ArrayList<>();
    private List<Long> vocabTablePositions = new ArrayList<>();
    private DiskIndexReader reader;
    public DiskIndex(){
        reader = new DiskIndexReader();
        vocabulary = reader.getVocab();
        vocabTablePositions = reader.getVocabPositions();
    }


    public boolean hasPostings(String term){
       return (VocaPos(term)!=-1);
    }

    public List<Posting> getPostings(String term) {
        List<Posting> retrieved = new ArrayList<>();
        try {
            long termPosition = vocabTablePositions.get(VocaPos(term));
            retrieved = reader.PostingListReader(termPosition);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Posting list can't be found for term ||" + term + "||");
        }
        return retrieved;
    }

    @Override
    public List<String> getVocabulary(){
        return vocabulary;
    }

    /**
     * Retrieves the position at which the queried term is. This also gives the position it has in
     * vocabTablePositions list
     * @param searchTerm
     * @return
     */
    private int VocaPos(String searchTerm){
        int low = 0;
        int high = vocabulary.size() - 1;
        int mid = -2;
        while (low <= high) {
            mid = (low + high) / 2;
            if (vocabulary.get(mid).compareTo(searchTerm) < 0) {
                low = mid + 1;
            } else if (vocabulary.get(mid).compareTo(searchTerm) > 0) {
                high = mid - 1;
            } else {
                return mid;
            }
        }
        if(mid==0) {
            return mid;
        }
        System.out.println(mid + " error value in result:" + searchTerm);
        return -1;
    }

    public double getload(int DocID){
        return reader.getload(DocID);
    }

    public double getavgDocLength(){
        return reader.getavgDocLength();
    }

    public int getbyteLength(int DocID){
        return reader.getbyteLength(DocID);
    }

    public int getdocLength(int DocID){
        return reader.getdocLength(DocID);
    }

    public double getavgtftd(int DocID){
        return reader.getavgtftd(DocID);
    }



}