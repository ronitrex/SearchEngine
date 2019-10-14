package modules.index;
import java.io.*;
import java.lang.*;
import java.util.ArrayList;
import java.util.HashMap;

//wdt = 1 + ln (tf td)
public class DiskIndexWriter{
    private DataOutputStream VocabOutputStream;
    private DataOutputStream PostingsOutputStream;
    private DataOutputStream VocabTableOutputStream;
    private DataOutputStream docWeightsOutputStream;

    /**
     *  Takes the positional inverted index stored in the memory and writes it to a file
     * @param index             the index created and currently stored in the memory
     * @param DocWeight         the DocWeight of a document is square root of (sum of squares of all w d,t terms)
     * @param wordFreqList      a list of all different keys and their frequencies for each document
     * @param DocLength         the total number of terms in a document
     * @param avgtftd           the avg tf t, d for each document in the document corpus
     * @param avgDocLength      the avg doc length of the entire corpus
     */

    public void writePositionalInvertedIndex(PositionalInvertedIndex positionalInvertedIndex) {
        try {
            //binary files are created from the memory index, which is a positional inverted index
            File vocab = new File("src/modules/binaryindex/vocab.bin");
            File postings = new File("src/modules/binaryindex/postings.bin");
            File vocabTable = new File("src/modules/binaryindex/vocabTable.bin");
            File docWeights = new File("src/modules/binaryindex/docWeights.bin");
            
            VocabOutputStream = new DataOutputStream(new FileOutputStream(vocab));
            PostingsOutputStream = new DataOutputStream(new FileOutputStream(postings));
            VocabTableOutputStream = new DataOutputStream(new FileOutputStream(vocabTable));
            docWeightsOutputStream = new DataOutputStream(new FileOutputStream(docWeights));

            ArrayList<Double> DocWeight = positionalInvertedIndex.getDocWeight();
            ArrayList<HashMap<String, Integer>> wordFreqList = positionalInvertedIndex.getWordFreqList();
            ArrayList<Integer> DocLength = positionalInvertedIndex.getDocLength();
            ArrayList<Double> avgtftdList = positionalInvertedIndex.getAvgtftdList();
            Double avgDocLength  = positionalInvertedIndex.getAvgDocLength();

                // docWeights, DocLength, byteSize (docLength*8) and avg tf t,d )are all per-document values, created during indexing
            // and each is saved to the docWeights.bin
            int corpussize = DocWeight.size();
            for(int i = 0; i< corpussize; i++){
                docWeightsWriter(DocWeight.get(i), DocLength.get(i), avgtftdList.get(i));
            }
            docWeightsOutputStream.writeDouble(avgDocLength);
            docWeightsOutputStream.close();

            //the vocab file contains a list of all the terms or keys in a given corpus in alphabetical order
            //the posting file contains the postings of all documents in the corpus
            //the vocabTable file points to the position of each term in vocab file as well as
            //the starting position of its postings in posting file
            long VocabPos, PostingPos;
            for (String term : positionalInvertedIndex.getVocabulary()) {
                PostingPos = PostingsOutputStream.size();
                VocabPos = VocabOutputStream.size();
                int dft = positionalInvertedIndex.getPostings(term).size();
                PostingsOutputStream.writeInt(dft);
                int prevDocId = 0;
                for (Posting posting : positionalInvertedIndex.getPostings(term)) {
                    int currDocId = posting.getDocumentId();
                    int gapDoc = currDocId - prevDocId;
                    int tftd = wordFreqList.get(currDocId).get(term);
                    PostingsOutputStream.writeInt(gapDoc);
                    PostingsOutputStream.writeDouble(1 + Math.log(tftd));  //w d,t value
                    PostingsOutputStream.writeInt(tftd);
                    prevDocId = currDocId;
                    PostingWriter(posting); //positions of term in the given document
                }
                VocabOutputStream.writeUTF(term);
                VocabTableOutputStream.writeLong(VocabPos);
                VocabTableOutputStream.writeLong(PostingPos);
            }

            PostingsOutputStream.close();
            VocabTableOutputStream.close();
            VocabOutputStream.close();
        }catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void PostingWriter(Posting posting) {
        try {
            int prePos = 0;
            for (Integer pos : posting.getPositionsInDoc()) {
                int gapPos = pos - prePos;
                prePos = pos;
                PostingsOutputStream.writeInt(gapPos);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void docWeightsWriter(double DocWeight, int DocLength, double avgtftd) {
        try {
            docWeightsOutputStream.writeDouble(DocWeight);
            docWeightsOutputStream.writeInt(DocLength);
            docWeightsOutputStream.writeInt(DocLength*8);
            docWeightsOutputStream.writeDouble(avgtftd);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
}
