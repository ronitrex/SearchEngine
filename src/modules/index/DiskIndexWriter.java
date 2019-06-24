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

    public void WriteIndex(Index index, ArrayList<Double> load, ArrayList<HashMap<String, Integer>> docLoads, ArrayList<Integer> docLength, ArrayList<Double> avgtftdList, Double avgDocLength ) {
        try {
            File vocab = new File("src/modules/binaryindex/vocab.bin");
            File postings = new File("src/modules/binaryindex/postings.bin");
            File vocabTable = new File("src/modules/binaryindex/vocabTable.bin");
            File docWeights = new File("src/modules/binaryindex/docWeights.bin");
            VocabOutputStream = new DataOutputStream(new FileOutputStream(vocab));
            PostingsOutputStream = new DataOutputStream(new FileOutputStream(postings));
            VocabTableOutputStream = new DataOutputStream(new FileOutputStream(vocabTable));
            docWeightsOutputStream = new DataOutputStream(new FileOutputStream(docWeights));
            int corpussize = load.size();
            for(int i = 0; i< corpussize; i++){
                docWeightsWriter(load.get(i), docLength.get(i), avgtftdList.get(i));
            }
            docWeightsOutputStream.writeDouble(avgDocLength);

            long VocabPos = 0, PostingPos = 0;
            for (String term : index.getVocabulary()) {
                PostingPos = PostingsOutputStream.size();
                VocabPos = VocabOutputStream.size();
//                System.out.println(term);
                int dft = index.getPostings(term).size();
                PostingsOutputStream.writeInt(dft);
                int prevDocId = 0;
//                System.out.println("whtever "+ prevDocId);
                for (Posting pwrite : index.getPostings(term)) {
                    int currDocId = pwrite.getDocumentId();
                    int gapDoc = currDocId - prevDocId;
                    int tftd = docLoads.get(currDocId).get(term);
//                    System.out.println(tftd+ " tftd");
                    PostingsOutputStream.writeInt(gapDoc);
                    PostingsOutputStream.writeDouble(1 + Math.log(tftd));
                    PostingsOutputStream.writeInt(tftd);
                    prevDocId = currDocId;
//                    System.out.println(gapDoc+" "+ currDocId);
                    PostingWriter(pwrite);
//                    System.out.println((PostingPos));
                }
                VocabWriter(term);
                VocabTableWriter(VocabPos, PostingPos);
            }

        }catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public  void VocabWriter(String termB) {
        try {
            VocabOutputStream.writeUTF(termB);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void VocabTableWriter(long VocabPos, long PostingPos) {
        try {
            VocabTableOutputStream.writeLong(VocabPos);
            VocabTableOutputStream.writeLong(PostingPos);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void PostingWriter(Posting termpostingB) {
        try {
            int prePos = 0;
            for (Integer pos : termpostingB.getPositionsInDoc()) {
                int gapPos = pos - prePos;
                prePos = pos;
                PostingsOutputStream.writeInt(gapPos);
//                System.out.println(gapPos+" "+prePos);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void docWeightsWriter(double load, int docLength, double avgtftdList) {
        try {
//            System.out.println(load);
            docWeightsOutputStream.writeDouble(load);
            docWeightsOutputStream.writeInt(docLength);
            docWeightsOutputStream.writeInt(docLength*8);
            docWeightsOutputStream.writeDouble(avgtftdList);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
}
