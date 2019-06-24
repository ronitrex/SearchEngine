package modules.index;

import org.apache.commons.collections4.functors.FalsePredicate;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;


public class PositionalDiskIndex implements Index {
    private String vocabFile = "src/modules/binaryindex/vocab.bin";
    private String postingsFile = "src/modules/binaryindex/postings.bin";
    private String vocabTableFile = "src/modules/binaryindex/vocabTable.bin";
    private String docWeightsFile = "src/modules/binaryindex/docWeights.bin";
    private List<String> vocabulary = new ArrayList<>();
    private List<Long> vocabPositions = new ArrayList<>();
    private static RandomAccessFile vocab_raf;
    private static RandomAccessFile postings_raf;
    private static RandomAccessFile vocabTable_raf;
    private static RandomAccessFile docWeights_raf;

    public PositionalDiskIndex(){
        SortedVocabCreator();
    }


    public boolean hasPostings(String term){
       return (VocaPos(term)!=-1);
    }

    public List<Posting> getPostings(String term) {
        List<Posting> retrieved = new ArrayList<>();
        try {
            long tryposition = vocabPositions.get(VocaPos(term));
//            System.out.print((tryposition + " tryposition"));
            retrieved = PostingListReader(tryposition);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Posting list can't be found for term ||" + term + "||");
        }
        return retrieved;
    }

    @Override
    public List<String> getVocabulary(){
        return vocabulary;
    }

    public double getload(int DocID){
        double load = 0.0;
        try {
            long position = 0;
            position = position + (24 * DocID);
            docWeights_raf.seek(position);
            load = docWeights_raf.readDouble();
        } catch (IOException e){
            System.out.println("getload error");
        }
        return load;
    }

    public int getdocLength(int DocID){
        int docLength = 0;
        try {
            long position = 0;
            position = position + (24 * DocID);
            docWeights_raf.seek(position+8);
            return docWeights_raf.readInt();
        } catch (IOException e){
            System.out.println("getdocLength error");
        }
        return docLength;
    }

    public int getbyteLength(int DocID){
        int bytelength = 0;
        try {
            long position = 0;
            position = position + (24 * DocID);
            docWeights_raf.seek(position+12);
            bytelength = docWeights_raf.readInt();
        } catch (IOException e){
            System.out.println("getbyteLength error");
        }
        return bytelength;
    }

    public double getavgtftd(int DocID){
        double avgtftd = 0.0;
        try {

            long position = 0;
            position = position + (24 *  DocID);
            docWeights_raf.seek(position+16);
            avgtftd = docWeights_raf.readDouble();
        } catch (IOException e){
            System.out.println("getavgtftd error");
        }
        return avgtftd;
    }

    public double getavgDocLength(){
        double avgtftd = 0.0;
        try {
            docWeights_raf.seek(docWeights_raf.length()-8);
            avgtftd = docWeights_raf.readDouble();
        } catch (IOException e){
            System.out.println("getavgDocLengtherror");
        }
        return avgtftd;
    }

    public void RAFcreator() {
        try {
            vocab_raf = new RandomAccessFile(vocabFile, "r");
            postings_raf = new RandomAccessFile(postingsFile, "r");
            vocabTable_raf = new RandomAccessFile(vocabTableFile, "r");
            docWeights_raf = new RandomAccessFile(docWeightsFile, "r");
        } catch (IOException e) {
            System.out.println("RAFcreator reports error" + e);
        }
    }

    public void SortedVocabCreator(){
        try{
            System.out.println("Entering RAFcreator");
            RAFcreator();
            long jumper = 0;
            while(jumper<vocabTable_raf.length()){
//                System.out.println(jumper);
                long vocabPosition = longRead(vocabTable_raf, jumper);
                long vocpost = longRead(vocabTable_raf,jumper+8);
                String worded = wordRead(vocabPosition);
                vocabulary.add(worded);
                vocabPositions.add(vocpost);
                jumper=jumper+16;
            }
        }catch (IOException e){
            System.out.println("There was some problem in creating the sorted vocabulary");
        }
    }

    public int VocaPos(String searchTerm){
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
        System.out.println(mid + " value of mid for error in resuslt for :" + searchTerm);
        return -1;
    }



    public List<Posting> PostingListReader(long position){

        List<Posting> Postingread = new ArrayList<Posting>();
        try {
            postings_raf.seek(position);
            int dft = postings_raf.readInt();
//            System.out.println(dft+ " dft");
            int DocId = 0;
            for (int i = 0; i < dft; i++) {
                position = position + 4;
                postings_raf.seek(position);
                int currdoc = postings_raf.readInt();
                DocId = DocId+currdoc;
//                System.out.print(Docid+ " Docid");
                position = position + 4;
                postings_raf.seek(position);
                double wdt = postings_raf.readDouble();
//                System.out.print(wdt+ " wdt");
                position = position + 8;
                postings_raf.seek(position);
                int tftd = postings_raf.readInt();
//                System.out.print(tftd+ " tftd");
                List<Integer> positions = new ArrayList<>();
                int currpos = 0;
                for (int j = 0; j < tftd; j++) {
                    position = position + 4;
                    postings_raf.seek(position);
                    int pos = postings_raf.readInt();
                    currpos = currpos + pos;
                    positions.add(currpos);
                }
                Posting p = new Posting(DocId, positions, wdt);
                Postingread.add(p);
            }
        }catch (IOException e){
            System.out.println("Some error in creating the PostingListReader file");
        }
        return Postingread;


    }

    public String wordRead(long position ){
        String word = "";
        try{
            vocab_raf.seek(position);
            word = vocab_raf.readUTF();
        } catch (IOException e){
            System.out.println("word read error at position" + position);
        }
        return word;
    }

    public Long longRead(RandomAccessFile file, long position ) {
        long longValue = 0;
        try {
            file.seek(position);
            longValue = file.readLong();
        } catch (IOException e) {
            System.out.println("long read error at position" + position);
        }
        return longValue;
    }
}