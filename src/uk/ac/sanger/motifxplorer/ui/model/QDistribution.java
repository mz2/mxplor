package uk.ac.sanger.motifxplorer.ui.model;

import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.biojava.bio.BioError;
import org.biojava.bio.dist.Distribution;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.Symbol;

import uk.ac.sanger.motifxplorer.ui.graphics.BoundaryItem;
import uk.ac.sanger.motifxplorer.ui.graphics.SymbolGraphicsItem;

import com.trolltech.qt.core.QObject;
import com.trolltech.qt.gui.QFont;

public class QDistribution extends QObject {
	private static final Comparator COMP = new ResValComparator();
	private static double bits = Math.log(2.0);
	private Distribution dist;

	private QFont font;
	
	private double distScale;
	
	private SortedSet<ResVal> info;
	private SortedSet<SymbolGraphicsItem> symbolItems = new TreeSet<SymbolGraphicsItem>();
	private BoundaryItem boundItem;
	private int column = -1; //lazily received from the parent, then stored here for later requests
	
	public QDistribution(QMotif parent, Distribution dist) {
		super(parent);
		
		//this.moveToThread(parent.thread());
		//this.setParent(parent);
		
		this.dist = dist;
		this.info = setupInfo();
		
	}
	
	private SortedSet<ResVal> setupInfo() {
		SortedSet<ResVal> info = new TreeSet<ResVal>(COMP);
		try {
			for(Iterator i = ((FiniteAlphabet) dist.getAlphabet()).iterator();
				i.hasNext();) {
				Symbol s = (Symbol) i.next();
				info.add(new ResVal(s, dist.getWeight(s)));
			}
		} catch (IllegalSymbolException ire) {
			throw new BioError("Symbol dissapeared from dist alphabet", ire);
		}
		return info;
	}
	
	/**
	 * @return the dist
	 */
	public Distribution getDist() {
		return dist;
	}

	/**
	 * @param dist the dist to set
	 */
	public void setDist(Distribution dist) {
		this.dist = dist;
	}
    
	/**
	 * Retrieve the maximal number of bits possible for this type of dist.
	 *
	 * @return maximum bits as a double
	 */
	public static double totalBits(Distribution dist) {
		return Math.log(((FiniteAlphabet) dist.getAlphabet()).size()) / bits;
	}

	/**
	 * <p>
	 * Calculates the total information of the dist in bits.
	 * </p>
	 *
	 * <p>
	 * This calculates <code>totalBits - sum_r(entropy(r))</code>
	 * </p>
	 *
	 * @return  the total information in the dist
	 */
	public static double totalInformation(Distribution dist) {
		double inf = totalBits(dist);

		for(
				Iterator i = ((FiniteAlphabet) dist.getAlphabet()).iterator();
				i.hasNext();
		) {
			Symbol s = (Symbol) i.next();
			try {
				inf -= entropy(dist, s);
			} catch (IllegalSymbolException ire) {
				throw new BioError(
						"Symbol evaporated while calculating information", ire);
			}
		}

		return inf;
	}


	/**
	 * Calculate the information content of a symbol in bits.
	 *
	 * @param r the symbol to calculate for
	 * @throws IllegalSymbolException if r is not within the dist.
	 */
	public static double entropy(Distribution dist, Symbol s) throws IllegalSymbolException {
		double p = dist.getWeight(s);
		if (p == 0.0) {
			return 0;
		}
		double lp = Math.log(p);
		
		return -p * lp / bits;
	}

	/**
	 * @return the info
	 */
	public SortedSet<ResVal> getInfo() {
		return info;
	}

	/**
	 * @param info the info to set
	 */
	public void setInfo(SortedSet<ResVal> info) {
		this.info = info;
	}

	/**
	 * @return the distScale
	 */
	public double getDistScale() {
		return  1 * (
				QDistribution.totalInformation(dist) /
				QDistribution.totalBits(dist));
	}

	/**
	 * @return the highlighted
	 */
	public boolean isHighlighted() {
		return ((QMotif)this.parent()).columnIsHighlighted(this.getColumn());
	}

	/**
	 * @param highlighted the highlighted to set
	 */
	public void setHighlighted(boolean highlighted) {
		((QMotif)this.parent()).setColumnHighlighted(this.getColumn(), highlighted);
		highlightStateUpdated();
	}

	/**
	 * @return the selected
	 */
	public boolean isSelected() {
		return ((QMotif)this.parent()).columnIsSelected(this.getColumn());
	}

	public int getColumn() {
		if (column < 0)
			column = ((QMotif)parent()).dists().indexOf(this);
		return column;
	}
	/**
	 * @param selected the selected to set
	 */
	public void setSelected(boolean selected) {
		((QMotif)this.parent()).setColumnSelected(this.getColumn(), selected);
		selectionStateUpdated();
	}
	
	public void toggleSelected() {
		setSelected(!isSelected());
	}

	public void toggleHighlighted() {
		setHighlighted(!isHighlighted());
	}
	
	public void selectionStateUpdated() {
		for (SymbolGraphicsItem symItem : symbolItems)
			symItem.selectionStateUpdated();
		
	}
	
	public void highlightStateUpdated() {
		for (SymbolGraphicsItem symItem : symbolItems)
			symItem.highlightStateUpdated();
	}
	
	/**
	 * @return the boundItem
	 */
	public BoundaryItem getBoundItem() {
		return boundItem;
	}

	/**
	 * @param boundItem the boundItem to set
	 */
	public void setBoundItem(BoundaryItem boundItem) {
		this.boundItem = boundItem;
	}

	/**
	 * @return the symbolItems
	 */
	public SortedSet<SymbolGraphicsItem> getSymbolItems() {
		return symbolItems;
	}

	/**
	 * @param symbolItems the symbolItems to set
	 */
	public void setSymbolItems(SortedSet<SymbolGraphicsItem> symbolItems) {
		this.symbolItems = symbolItems;
	}
	
	public QMotif motif() {
		return (QMotif)this.parent();
	}
}
