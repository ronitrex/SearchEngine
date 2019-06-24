package main;
import modules.documents.DocumentCorpus;
import modules.index.PositionalDiskIndex;
import modules.index.Posting;
//import modules.text.AdvancedTokenProcessor;

import java.util.*;

public class WeightScheme {
    private List<String> uQuery;
    private PositionalDiskIndex ureader;
    private DocumentCorpus ucorpus;
    private HashMap<Integer, Double> docs;
    private int utotaldocs;

    public WeightScheme(List<String> userquery, PositionalDiskIndex reader, DocumentCorpus corpus, int totalDocs) throws Throwable {
        uQuery = userquery;
        ureader = reader;
        ucorpus = corpus;
        docs = new HashMap<>();
        utotaldocs= totalDocs;
    }

    public Double getweight(int docID){
        return docs.get(docID);
    }

    Comparator<Integer> doubleComparator = new Comparator<Integer>(){
        @Override
        public int compare(Integer s1, Integer s2) {
            double diff = getweight(s2)-getweight(s1);
//            System.out.println(diff+ "s1 "+s1+" s2 "+ s2);
            if(diff>0){

                return 1;
            }
            return -1;
        }
    };


    public PriorityQueue<Integer> defaultWeightDocIds() throws Throwable{
        docs = new HashMap<>();
        PriorityQueue<Integer> SortedDocs = new PriorityQueue<Integer>(doubleComparator);
        try {
            for (String termStemmed : uQuery) {
                int dft = ureader.getPostings(termStemmed).size();
                double wqt = Math.log((double)1+(utotaldocs/dft));
                double Adt = 0;

                for (Posting p : ureader.getPostings(termStemmed)) {
                    Adt = Adt + wqt*p.getwdt();
                    if(Adt>0){
                        Adt = Adt/ureader.getload(p.getDocumentId());
                    }
//                    System.out.println("kya ha " + p.getDocumentId());
//                    System.out.println("Document " + ucorpus.getDocument(p.getDocumentId()).getTitle() + "- " + " Document ID:" + ucorpus.getDocument(p.getDocumentId()).getId());
                    if (docs.containsKey(p.getDocumentId())) {
                        docs.replace(p.getDocumentId(), docs.get(p.getDocumentId())+ Adt);
                    }
                    else docs.put(p.getDocumentId(), Adt);
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        System.out.println(docs);

        for(Integer DocEntries : docs.keySet()){

            SortedDocs.add(DocEntries);

        }
        System.out.println(SortedDocs);
        return SortedDocs;
    }

    public PriorityQueue<Integer> idfSortedDocIds() throws Throwable{
        PriorityQueue<Integer> SortedDocs = new PriorityQueue<Integer>(doubleComparator);
        try {
            for (String termStemmed : uQuery) {
                int dft = ureader.getPostings(termStemmed).size();
                double wqt = Math.log(1400/dft);
                double Adt = 0;

                for (Posting p : ureader.getPostings(termStemmed)) {
                    double wdt = p.getPositionsInDoc().size();
                    Adt = Adt + wqt*wdt;
                    if(Adt>0){
                        Adt = Adt/ureader.getload(p.getDocumentId());
                    }
                    System.out.println("Document " + ucorpus.getDocument(p.getDocumentId()).getTitle() + "- " + " Document ID:" + ucorpus.getDocument(p.getDocumentId()).getId());
                    if (docs.containsKey(p.getDocumentId())) {
                        docs.replace(p.getDocumentId(), docs.get(p.getDocumentId())+ Adt);
                    }
                    else docs.put(p.getDocumentId(), Adt);
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        for(Integer DocEntries : docs.keySet()){
            SortedDocs.add(DocEntries);
        }
        return SortedDocs;
    }

    public PriorityQueue<Integer> okapiweightSortedDocIds() throws Throwable{
        PriorityQueue<Integer> SortedDocs = new PriorityQueue<Integer>(doubleComparator);
        try {
            for (String termStemmed : uQuery) {
                int dft = ureader.getPostings(termStemmed).size();

                double wqt = Math.log((utotaldocs-dft+0.5)/(dft+0.5));
                double Adt = 0;

                for (Posting p : ureader.getPostings(termStemmed)) {
                    double wdt = (2.2*p.getPositionsInDoc().size())/(1.2*(0.25+0.75*(ureader.getdocLength(p.getDocumentId())/ureader.getavgDocLength()))+p.getPositionsInDoc().size());
                    if (wdt<0.1){
                        wdt = 0.1;
                    }
                    Adt = Adt + wqt*p.getwdt();
                    if(Adt>0){
                        Adt = Adt/1;
                    }
                    System.out.println("Document " + ucorpus.getDocument(p.getDocumentId()).getTitle() + "- " + " Document ID:" + ucorpus.getDocument(p.getDocumentId()).getId());
                    if (docs.containsKey(p.getDocumentId())) {
                        docs.replace(p.getDocumentId(), docs.get(p.getDocumentId())+ Adt);
                    }
                    else docs.put(p.getDocumentId(), Adt);
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        for(Integer DocEntries : docs.keySet()){
            SortedDocs.add(DocEntries);
        }
        return SortedDocs;
    }

    public PriorityQueue<Integer> wackySortedDocIds() throws Throwable{
        PriorityQueue<Integer> SortedDocs = new PriorityQueue<Integer>(doubleComparator);
        try {
            for (String termStemmed : uQuery) {
                int dft = ureader.getPostings(termStemmed).size();

                double wqt = Math.log((utotaldocs-dft)/(dft));
                if (wqt<0){
                    wqt=0;
                }
                double Adt = 0;

                for (Posting p : ureader.getPostings(termStemmed)) {
                    double wdt = (1+Math.log(p.getPositionsInDoc().size())/(1 + Math.log(ureader.getavgtftd(p.getDocumentId()))));
                    if (wdt<0.1){
                        wdt = 0.1;
                    }
                    Adt = Adt + wqt*p.getwdt();
                    if(Adt>0){
                        Adt = Adt/Math.sqrt(ureader.getbyteLength(p.getDocumentId()));
                    }
                    System.out.println("Document " + ucorpus.getDocument(p.getDocumentId()).getTitle() + "- " + " Document ID:" + ucorpus.getDocument(p.getDocumentId()).getId());
                    if (docs.containsKey(p.getDocumentId())) {
                        docs.replace(p.getDocumentId(), docs.get(p.getDocumentId())+ Adt);
                    }
                    else docs.put(p.getDocumentId(), Adt);
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        for(Integer DocEntries : docs.keySet()){
            SortedDocs.add(DocEntries);
        }
        return SortedDocs;
    }


}

