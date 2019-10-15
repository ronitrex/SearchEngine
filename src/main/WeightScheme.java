package main;
import modules.index.DiskIndex;
import modules.index.Posting;
import java.util.*;

class WeightScheme {
    private HashMap<Integer, Double> docs;
    private Double getWeight(int docID){
        return docs.get(docID);
    }

    private Comparator<Integer> doubleComparator = (s1, s2) -> {
        double diff = getWeight(s2)- getWeight(s1);
        if(diff>0){
            return 1;
        }
        else if (diff==0){
            return 0;
        }
        else return -1;
    };

    PriorityQueue<Integer> getDefaultWeight(DiskIndex diskIndex, List<String> stemmedUserQuery, int corpusSize){
        docs = new HashMap<>();
        PriorityQueue<Integer> sortedDocs = new PriorityQueue<>(doubleComparator);
        try {
            for (String stemmedTerm : stemmedUserQuery) {
                List<Posting> stemmedTermPostings = diskIndex.getPostings(stemmedTerm);
                float dft = stemmedTermPostings.size();
                double wqt = Math.log(1+(double)(corpusSize /dft));
                double accum = 0;

                for (Posting p : stemmedTermPostings) {
                    int documentId = p.getDocumentId();
                    double wdt = p.getwdt();
                    accum = accum + wqt*wdt;
                    if(accum >0){
                        accum = accum / diskIndex.getLoad(documentId);
                    }
                    if (docs.containsKey(documentId)) {
                        docs.replace(documentId, docs.get(documentId)+ accum);
                    }
                    else docs.put(documentId, accum);
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
//        System.out.println("defaultWeight" + docs);
        sortedDocs.addAll(docs.keySet());
//        System.out.println(sortedDocs);
        return sortedDocs;
    }

    PriorityQueue<Integer> getTfIdf(DiskIndex diskIndex, List<String> stemmedUserQuery, int corpusSize){
        docs = new HashMap<>();
        PriorityQueue<Integer> sortedDocs = new PriorityQueue<>(doubleComparator);
        try {
            for (String stemmedTerm : stemmedUserQuery) {
                List<Posting> stemmedTermPostings = diskIndex.getPostings(stemmedTerm);
                float dft = stemmedTermPostings.size();
                double wqt = Math.log(0+(double)(corpusSize /dft));
                double accum = 0;

                for (Posting p : stemmedTermPostings) {
                    int documentId = p.getDocumentId();
                    double wdt = p.getPositionsInDoc().size();    //tf t, d
                    accum = accum + wqt*wdt;
                    if(accum >0){
                        accum = accum / diskIndex.getLoad(documentId);
                    }
                    if (docs.containsKey(documentId)) {
                        docs.replace(documentId, docs.get(documentId)+ accum);
                    }
                    else docs.put(documentId, accum);
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        sortedDocs.addAll(docs.keySet());
        return sortedDocs;
    }

    PriorityQueue<Integer> getOkapibm25(DiskIndex diskIndex, List<String> stemmedUserQuery, int corpusSize){
        docs = new HashMap<>();
        PriorityQueue<Integer> sortedDocs = new PriorityQueue<>(doubleComparator);
        try {
            for (String stemmedTerm : stemmedUserQuery) {
                List<Posting> stemmedTermPostings = diskIndex.getPostings(stemmedTerm);
                float dft = stemmedTermPostings.size();
                double wqt = Math.log((corpusSize-dft+0.5)/(dft+0.5));
                if (wqt < 0.1){
                    wqt = 0.1;
                }
                double accum = 0;

                for (Posting p : diskIndex.getPostings(stemmedTerm)) {
                    int documentId = p.getDocumentId();
                    double tftd = p.getPositionsInDoc().size();
                    double wdt = (2.2*tftd)/(1.2*(0.25+0.75*(diskIndex.getdocLength(documentId)/ diskIndex.getavgDocLength()))+tftd);
                    accum = accum + wqt*wdt;
                    if(accum >0){
                        accum = accum /1;
                    }
                    if (docs.containsKey(documentId)) {
                        docs.replace(documentId, docs.get(documentId)+ accum);
                    }
                    else docs.put(documentId, accum);
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        sortedDocs.addAll(docs.keySet());
        return sortedDocs;
    }

    PriorityQueue<Integer> getWacky(DiskIndex diskIndex, List<String> stemmedUserQuery, int corpusSize){
        docs = new HashMap<>();
        PriorityQueue<Integer> sortedDocs = new PriorityQueue<>(doubleComparator);
        try {
            for (String stemmedTerm : stemmedUserQuery) {
                List<Posting> stemmedTermPostings = diskIndex.getPostings(stemmedTerm);
                float dft = stemmedTermPostings.size();
                double wqt = Math.log((corpusSize-dft)/(dft));
                if (wqt<0){
                    wqt=0;
                }
                double accum = 0;

                for (Posting p : diskIndex.getPostings(stemmedTerm)) {
                    int documentId = p.getDocumentId();
                    double tftd = p.getPositionsInDoc().size();
                    double wdt = (1+Math.log(tftd)/(1 + Math.log(diskIndex.getavgtftd(documentId))));
                    if (wdt<0.1){
                        wdt = 0.1;
                    }
                    accum = accum + wqt*wdt;
                    if(accum >0){
                        accum = accum /Math.sqrt(diskIndex.getbyteLength(documentId));
                    }
                    if (docs.containsKey(documentId)) {
                        docs.replace(documentId, docs.get(documentId)+ accum);
                    }
                    else docs.put(documentId, accum);
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        sortedDocs.addAll(docs.keySet());
        return sortedDocs;
    }
}

