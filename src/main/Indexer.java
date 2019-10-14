package main;

import modules.documents.DocumentCorpus;
import modules.index.*;

public class Indexer {
    private PositionalInvertedIndex positionalInvertedIndex;

    public Indexer(DocumentCorpus documentCorpus){
        positionalInvertedIndex = new PositionalInvertedIndex(documentCorpus);
        DiskIndexWriter writer = new DiskIndexWriter();
        writer.writePositionalInvertedIndex(positionalInvertedIndex);
    }

    public PositionalInvertedIndex getPositionalInvertedIndex(){
        return positionalInvertedIndex;
    }
}
