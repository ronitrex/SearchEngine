package modules.index;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class DiskIndexReader {
    private String vocabFile = "src/modules/binaryindex/vocab.bin";
    private String postingsFile = "src/modules/binaryindex/postings.bin";
    private String vocabTableFile = "src/modules/binaryindex/vocabTable.bin";
    private String docWeightsFile = "src/modules/binaryindex/docWeights.bin";
    private List<String> vocabulary = new ArrayList<>();
    private List<Long> vocabPositions = new ArrayList<>();

    public List<String> getVocab(){
        return vocabulary;
    }
    public List<Long> getVocabPositions(){
        return vocabPositions;
    }

    public DiskIndexReader(){
        VocabTableCreator();
    }

    private RandomAccessFile makeRandomAccessFile(String filename) {
        RandomAccessFile filename_raf = null;
        try {
            filename_raf = new RandomAccessFile(filename, "r");
        } catch (IOException e) {
            System.out.println("Could not locate file to create a random access file" + e);
        }
        return filename_raf;
    }

    public double getload(int DocID){
        double load = 0.0;
        try {
            long position = 0;
            position = position + (24 * DocID);
            RandomAccessFile docWeights_raf = makeRandomAccessFile(docWeightsFile);
            docWeights_raf.seek(position);
            load = docWeights_raf.readDouble();
            docWeights_raf.close();
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
            RandomAccessFile docWeights_raf = makeRandomAccessFile(docWeightsFile);
            docWeights_raf.seek(position+8);
            docLength =  docWeights_raf.readInt();
            docWeights_raf.close();
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
            RandomAccessFile docWeights_raf = makeRandomAccessFile(docWeightsFile);
            docWeights_raf.seek(position+12);
            bytelength = docWeights_raf.readInt();
            docWeights_raf.close();
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
            RandomAccessFile docWeights_raf = makeRandomAccessFile(docWeightsFile);
            docWeights_raf.seek(position+16);
            avgtftd = docWeights_raf.readDouble();
            docWeights_raf.close();
        } catch (IOException e){
            System.out.println("getavgtftd error");
        }
        return avgtftd;
    }

    public double getavgDocLength(){
        double avgtftd = 0.0;
        try {
            RandomAccessFile docWeights_raf = makeRandomAccessFile(docWeightsFile);
            docWeights_raf.seek(docWeights_raf.length()-8);
            avgtftd = docWeights_raf.readDouble();
            docWeights_raf.close();
        } catch (IOException e){
            System.out.println("getavgDocLengtherror");
        }
        return avgtftd;
    }



    private void VocabTableCreator(){
        try{
            RandomAccessFile vocabTable_raf = makeRandomAccessFile(vocabTableFile);
            long jumper = 0;
            while(jumper<vocabTable_raf.length()){
                long vocabPosition = longRead(vocabTable_raf, jumper);
                long vocpost = longRead(vocabTable_raf,jumper+8);
                String worded = wordRead(vocabPosition);
                vocabulary.add(worded);
                vocabPositions.add(vocpost);
                jumper=jumper+16;
            }
            vocabTable_raf.close();
        }catch (IOException e){
            System.out.println("There was some problem in creating the sorted vocabulary");
        }
    }



    public List<Posting> PostingListReader(long position){
        System.out.println(position);
        List<Posting> Postingread = new ArrayList<Posting>();
        try {
            RandomAccessFile  postings_raf = makeRandomAccessFile(postingsFile);

            postings_raf.seek(position);
            int dft = postings_raf.readInt();
            int DocId = 0;
            for (int i = 0; i < dft; i++) {

                position = position + 4;
                postings_raf.seek(position);
                int currdoc = postings_raf.readInt();
                DocId = DocId+currdoc;

                position = position + 4;
                postings_raf.seek(position);
                double wdt = postings_raf.readDouble();

                position = position + 8;
                postings_raf.seek(position);
                int tftd = postings_raf.readInt();

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
            RandomAccessFile  vocab_raf = makeRandomAccessFile(vocabFile);
            vocab_raf.seek(position);
            word = vocab_raf.readUTF();
            vocab_raf.close();
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
//            file.close();
        } catch (IOException e) {
            System.out.println("long read error at position" + position);
        }
        return longValue;
    }
}
