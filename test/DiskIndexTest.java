import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Paths;
import java.util.List;

import modules.index.*;
import main.Indexer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import modules.documents.DirectoryCorpus;
import modules.documents.DocumentCorpus;
import modules.query.BooleanQueryParser;

/* All the Data Stream Writers write a byte at a time. So the minimum gap in the ata stream will be 8 bits.
 */

class DiskIndexTest {
    private BooleanQueryParser queryParser = new BooleanQueryParser();
    private static Index index;
    private static DiskIndex reader;
    private static DocumentCorpus corpus;

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        corpus = DirectoryCorpus.loadJSONFileDirectory(Paths.get("testdirectory/nationalparks"), ".json");
        Indexer ind = new Indexer(corpus);
        index = ind.getPosIndex();
        reader = new DiskIndex();
    }


    @Test
    void testDiskMempostings() {
        for (String a : reader.getVocabulary()) {
//            System.out.println("Posting :" + a);

            List<Posting> posPostings = index.getPostings(a);
            List<Posting> diskPostings = reader.getPostings(a);
            int i = 0;
            assertTrue(posPostings.size() == diskPostings.size());

            while (i < posPostings.size()) {
                List<Integer> posPostingpositions = posPostings.get(i).getPositionsInDoc();
                List<Integer> diskPostingPositions = diskPostings.get(i).getPositionsInDoc();
                assertTrue(posPostings.get(i).getDocumentId() == diskPostings.get(i).getDocumentId());
                assertTrue(posPostingpositions.size() == diskPostingPositions.size());

                for (int pos = 0; pos < posPostingpositions.size(); pos++) {
                    int pos1 = posPostingpositions.get(pos);
                    int pos2 = diskPostingPositions.get(pos);
                    assertTrue(pos1 == pos2);
                }
                i++;
            }
        }
    }

    @Test
    void testMemDiskpostings() {
        for (String a : index.getVocabulary()) {
//            System.out.println("Posting :" + a);

            List<Posting> posPostings = index.getPostings(a);
            List<Posting> diskPostings = reader.getPostings(a);
            int i = 0;
            assertTrue(posPostings.size() == diskPostings.size());

            while (i < posPostings.size()) {
                List<Integer> posPostingpositions = posPostings.get(i).getPositionsInDoc();
                List<Integer> diskPostingPositions = diskPostings.get(i).getPositionsInDoc();
                assertTrue(posPostings.get(i).getDocumentId() == diskPostings.get(i).getDocumentId());
                assertTrue(posPostingpositions.size() == diskPostingPositions.size());

                for (int pos = 0; pos < posPostingpositions.size(); pos++) {
                    int pos1 = posPostingpositions.get(pos);
                    int pos2 = diskPostingPositions.get(pos);
                    assertTrue(pos1 == pos2);
                }
                i++;
            }
        }
    }

    @Test
    void testnational() {
        String term = "nation";
        System.out.print(term);
        for (Posting p : index.getPostings(term)) {
            System.out.println("\n" + p.getDocumentId());
            for (Integer pos : p.getPositionsInDoc()) {
                System.out.print(pos + " ");
            }
        }
        System.out.println("\n Reader results");
        System.out.println(reader.getavgDocLength());
        for (Posting p : reader.getPostings(term)) {
            int DocId = p.getDocumentId();
            System.out.println("\n" + DocId);
            for (Integer pos : p.getPositionsInDoc()) {
                System.out.print(pos + " ");
            }
            System.out.println("\n" + DocId + " " + p.getwdt());
            System.out.println(reader.getload(DocId) + " load");
            System.out.println(reader.getdocLength(DocId) + " doclength");
            System.out.println(reader.getbyteLength(DocId) + " bytelength");
            System.out.println(reader.getavgtftd(DocId) + " avgtftd");

        }
    }
}
