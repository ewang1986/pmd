/*
 * User: tom
 * Date: Sep 5, 2002
 * Time: 11:26:28 AM
 */
package net.sourceforge.pmd.dcpd;

import net.jini.core.entry.UnusableEntryException;
import net.jini.core.entry.Entry;
import net.jini.core.transaction.TransactionException;
import net.jini.core.lease.Lease;
import net.jini.space.JavaSpace;
import net.sourceforge.pmd.cpd.*;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

public class TileExpander {

    private JavaSpace space;
    private TokenSetsWrapper tsw;

    public TileExpander(JavaSpace space, TokenSetsWrapper tsw) {
        this.space = space;
        this.tsw = tsw;
    }

    public void expandAvailableTiles() throws RemoteException, UnusableEntryException, TransactionException, InterruptedException{
        Entry twQuery = space.snapshot(new Chunk(tsw.jobID, null, Chunk.NOT_DONE, null));

        Chunk chunk = null;
        int total = 0;
        while ((chunk = (Chunk)space.take(twQuery, null, 10)) != null) {
            total++;
            List wrappers = new ArrayList();
            for (int j=0; j<chunk.tileWrappers.size(); j++) {
                TileWrapper tileWrapperToExpand = (TileWrapper)chunk.tileWrappers.get(j);
                //System.out.println("Expanding " + tileWrapperToExpand.tile.getImage());
                Occurrences results = expand(tileWrapperToExpand, j);
                int expansionIndex = 0;
                for (Iterator i = results.getTiles();i.hasNext();) {
                    Tile tile = (Tile)i.next();
                    TileWrapper tileWrapperToWrite = new TileWrapper(tile, results.getOccurrencesList(tile), new Integer(expansionIndex), new Integer(results.size()));
                    wrappers.add(tileWrapperToWrite);
                    //System.out.println("Wrote " + tileWrapperToWrite + "; occurrences = " + tileWrapperToWrite.occurrences.size());
                    expansionIndex++;
                }
            }
            Chunk chunkToWrite = new Chunk(tsw.jobID, wrappers, Chunk.DONE, chunk.sequenceID);
            space.write(chunkToWrite, null, Lease.FOREVER);
        }
        if (total>0) System.out.println("Expanded " + total + " tiles");
    }

    private Occurrences expand(TileWrapper tileWrapper, int sequenceID)  throws RemoteException, UnusableEntryException, TransactionException, InterruptedException{
        Occurrences newOcc = new Occurrences(new CPDNullListener());
        for (Iterator i = tileWrapper.occurrences.iterator(); i.hasNext();) {
            TokenEntry tok = (TokenEntry)i.next();
            TokenList tokenSet = tsw.tokenSets.getTokenList(tok);
            if (tokenSet.hasTokenAfter(tileWrapper.tile, tok)) {
                TokenEntry token = (TokenEntry)tokenSet.get(tok.getIndex() + tileWrapper.tile.getTokenCount());
                if (!newOcc.contains(token)) {
                    Tile newTile = tileWrapper.tile.copy();
                    newTile.add(token);
                    newOcc.addTile(newTile, tok);
                }
            }
        }
        return newOcc;
    }
}
