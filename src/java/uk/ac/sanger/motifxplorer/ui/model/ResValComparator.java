package uk.ac.sanger.motifxplorer.ui.model;
import java.util.Comparator;


/*
 * TODO: Grabbed this as is from MotifExplorer. Add the copyright, author, GPL licence info etc here and elsewhere
 */
public class ResValComparator implements Comparator<ResVal> {
    public final int compare(ResVal o1, ResVal o2) {
        ResVal rv1 = (ResVal) o1;
        ResVal rv2 = (ResVal) o2;

        double diff = rv1.getValue() - rv2.getValue();
        if(diff < 0) return -1;
        if(diff > 0) return +1;
        return rv1.getToken().getName().compareTo(rv2.getToken().getName());
    }
}
