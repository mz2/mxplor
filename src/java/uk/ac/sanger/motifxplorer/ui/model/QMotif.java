package uk.ac.sanger.motifxplorer.ui.model;

import java.util.ArrayList;
import java.util.List;

import net.derkholm.nmica.model.metamotif.Dirichlet;
import net.derkholm.nmica.model.metamotif.MetaMotif;
import net.derkholm.nmica.model.metamotif.MetaMotifIOTools;
import net.derkholm.nmica.motif.Motif;

import org.biojava.bio.BioException;
import org.biojava.bio.dist.Distribution;
import org.biojava.bio.dp.WeightMatrix;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.IllegalSymbolException;

import com.trolltech.qt.core.QObject;
import com.trolltech.qt.core.QRect;
import com.trolltech.qt.gui.QFont;
import com.trolltech.qt.gui.QWidget;

public class QMotif extends QObject {
	public static final int PRECISION_ALPHA_SCALING = 5;

	public static enum DrawingStyle {BLOCK, TEXT_LOGO};
	public static enum EditState {READ_WRITE, READ_ONLY};
	
	protected Motif nmicaMotif;
    public Signal0 loaded = new Signal0();

	protected QFont font = null; 
	
	protected int horizontalOffset;
	
	private List<QDistribution> dists;
	public List<Integer> selectedColumns;
	public List<Integer> highlightedColumns;
	
	public boolean selected;
	
	//Currently precisions are used without any alterations as the alpha channel
	
	private boolean isMetaMotif = true; //FIXME: Change to false ASAP!
	private double[] precisions;
	private double DEFAULT_PRECISION = 255;
	private MetaMotif metaMotif;
	
    public QMotif(QWidget widget, Motif m) {
    	this.setParent(widget);
    	nmicaMotif = m;
    	setupDists();
    	setSelected(false);

    	precisions = MetaMotifIOTools.alphaSumsFromMotif(m);
    	isMetaMotif = precisions != null;
    }
    
    public QMotif(MetaMotif mm) {
    	this(null, mm);
    }
    
    public QMotif(QWidget widget, MetaMotif mm) {
    	this.setParent(widget);
    	//TODO: Actually do the error handling here as it's supposed to
    	try {
			nmicaMotif = MetaMotifIOTools.metaMotifToAnnotatedMotif(mm);
	    	setupDists();
	    	setSelected(false);
	    	precisions = MetaMotifIOTools.alphaSumsFromMotif(nmicaMotif);
		} catch (IllegalSymbolException e) {
			e.printStackTrace();
		} catch (IllegalAlphabetException e) {
			e.printStackTrace();
		} catch (BioException e) {
			e.printStackTrace();
		}
    	
    }
    
    
    public QMotif(Motif m) {
    	this(null, m);
    }
    
    public void setupDists() {
    	List<QDistribution> dists = new ArrayList<QDistribution>();
    	WeightMatrix wm = nmicaMotif.getWeightMatrix();
    	
    	for (int i = 0; i < wm.columns(); i++) {
			Distribution dist = wm.getColumn(i);
			QDistribution qdist = new QDistribution(this,dist);
			dists.add(qdist);
    	}
    	selectedColumns = new ArrayList<Integer>();
    	highlightedColumns = new ArrayList<Integer>();
    	this.dists = dists;
    }
    
    /*
     * TODO: Remove these bits. This was just part of an experiment with the multithreading framework in QT.
     */
    public void loadMotif() {
    	synchronized(this) {
	        if (nmicaMotif != null) {
	            loaded.emit();
	        }
    	}
    }

	/**
	 * @return the nmicaMotif
	 */
	public synchronized Motif getNmicaMotif() {
		return nmicaMotif;
	}

	/**
	 * @param nmicaMotif the nmicaMotif to set
	 */
	public synchronized void setNmicaMotif(Motif motif) {
		this.nmicaMotif = motif;
		setupDists();
	}
    
	public int getHorizontalOffsetInPixels(QRect rect, int maxCols) {
		return (int)(Math.round(rect.width() / ((double)maxCols) * 0.95));
	}

	public List<QDistribution> dists() {
		return dists;
	}

	public void moveToLeft() {
		horizontalOffset--;
	}
	public void moveBy(int i) {
		horizontalOffset = horizontalOffset + i;
	}
	public void moveToRight() {
		horizontalOffset++;
	}
	
	public void setHorizontalOffset(int offset) {
		this.horizontalOffset = offset;
	}
	
	public int getHorizontalOffset() {
		return this.horizontalOffset;
	}

	public void toggleSelection(int i) {
		setColumnSelected(i, !columnIsSelected(i));
		System.out.println("Toggled selection at " + i);
	
	}

	public static List<QMotif> create(QWidget qobj, List<Motif> motifs) {
		List<QMotif> qmotifs = new ArrayList<QMotif>();
		for (Motif m : motifs)
			qmotifs.add(new QMotif(qobj,m));
		return qmotifs;
	}
	
	public static List<QMotif> create(List<Motif> motifs) {
		return create(null, motifs);
	}

	public static List<QMotif> create(QWidget qobj, Motif[] motifs) {
		List<QMotif> qms = new ArrayList<QMotif>();
		
		for (Motif m : motifs)
			qms.add(new QMotif(qobj,m));
		
		return qms;
	}

	public static List<QMotif> create(Motif[] motifs) {
		return create(null, motifs);
	}
	
	/**
	 * @return the dists
	 */
	public List<QDistribution> getDists() {
		return dists;
	}

	/**
	 * @param dists the dists to set
	 */
	public void setDists(List<QDistribution> dists) {
		this.dists = dists;
	}

	/**
	 * @return the highlightedColumns
	 */
	public List<Integer> getHighlightedColumns() {
		return highlightedColumns;
	}

	/**
	 * @param highlightedColumns the highlightedColumns to set
	 */
	public void setHighlightedColumns(List<Integer> highlightedColumns) {
		this.highlightedColumns = highlightedColumns;
	}

	public Dirichlet[] getSelectedDirichletColumns() {
		Dirichlet[] selections  = new Dirichlet[selectedColumns.size()];
		
		for (int i = 0; i < selectedColumns.size(); i++) {
			selections[i] = this.getMetaMotif().getColumn(
					selectedColumns.get(i).intValue());
		}
			
		return selections;
	}
	
	public Distribution[] getSelectedColumns() {
		Distribution[] selections  = new Distribution[selectedColumns.size()];
		
		for (int i = 0; i < selectedColumns.size(); i++)
			selections[i] = this.getNmicaMotif().getWeightMatrix().getColumn(
					selectedColumns.get(i).intValue());
		
		return selections;
	}

	/**
	 * @return the selected
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * @param selected the selected to set
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	/**
	 * @param precision the precision to set
	 */
	public void setPrecision(int i, double precision) {
		this.precisions[i] = precision;
	}

	public double getPrecision(int i) {
		if (this.precisions != null)
			return this.precisions[i];
		else
			throw new IllegalStateException("Should not be here...");
	}

	public boolean isMetaMotif() {
		return isMetaMotif;
	}

	public MetaMotif getMetaMotif() {
		if (this.metaMotif != null)
			return this.metaMotif;
		else {
			if (this.precisions == null) 
				return null;
			
			Dirichlet[] dists = new Dirichlet[this.getNmicaMotif().getWeightMatrix().columns()];
			for (int i = 0; i < dists.length; i++) {
				dists[i] = new Dirichlet(this.getNmicaMotif().getWeightMatrix().getColumn(i),precisions[i]);
			}
			
			try {
				this.metaMotif = new MetaMotif(dists);
			} catch (IllegalSymbolException e) {
				throw new IllegalStateException(e);
			}
			return this.metaMotif;
		}
	}

	public boolean columnIsSelected(int column) {
		return (this.selectedColumns.contains(column));
	}
	
	public void setColumnSelected(int column, boolean selected) {
		if (selected) {
			System.out.println("Set " + column);
			if (!this.selectedColumns.contains(column))
				this.selectedColumns.add(column);
		}else {
			System.out.println("Unset " + column);
			if (this.selectedColumns.contains(column))
				this.selectedColumns.remove(new Integer(column)); //careful there, otherwise it'll be treated as int
		}
		for (int i : selectedColumns)
			System.out.print(i + " ");
		
		System.out.println();
	}
	
	public void toggleColumnSelected(int column) {
		this.setColumnHighlighted(column,!columnIsHighlighted(column));
	}
	
	public boolean columnIsHighlighted(int column) {
		return (this.highlightedColumns.contains(column));
	}
	
	public void setColumnHighlighted(int column, boolean hilited) {
		if (hilited)
			if (!this.highlightedColumns.contains(column))
				this.highlightedColumns.add(column);
		else
			if (this.highlightedColumns.contains(column))
				this.highlightedColumns.remove(column);
	}
	
	public void toggleColumnHighlighted(int column) {
		this.setColumnHighlighted(column,!columnIsHighlighted(column));
	}

}
