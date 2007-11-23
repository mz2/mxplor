package uk.ac.sanger.motifxplorer.ui.model;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import net.derkholm.nmica.apps.MetaMotifSimulator;
import net.derkholm.nmica.model.metamotif.Dirichlet;
import net.derkholm.nmica.model.metamotif.MetaMotif;
import net.derkholm.nmica.model.metamotif.MetaMotifIOTools;
import net.derkholm.nmica.motif.Motif;

import org.biojava.bio.Annotation;
import org.biojava.bio.BioException;
import org.biojava.bio.dist.Distribution;
import org.biojava.bio.dp.WeightMatrix;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.IllegalSymbolException;

import uk.ac.sanger.motifxplorer.ui.graphics.QMotifBoundingBox;

import com.trolltech.qt.core.QObject;
import com.trolltech.qt.core.QRect;
import com.trolltech.qt.gui.QFont;
import com.trolltech.qt.gui.QWidget;

//TODO: Save offset and selected columns as annotations!

public class QMotif extends QObject {
	public static final int PRECISION_ALPHA_SCALING = 2;
	public static final int PRECISION_ALPHA_SCALING_FOR_SAMPLE = 3;
	public static enum DrawingStyle {BLOCK, TEXT_LOGO};
	public static enum EditState {READ_WRITE, READ_ONLY};
	
	protected Motif nmicaMotif;
    public Signal0 loaded = new Signal0();

	protected QFont font = null; 
	
	protected int offset;
	
	private List<QDistribution> dists;
	public List<Integer> selectedColumns;
	public List<Integer> highlightedColumns;
	
	public boolean selected;
	
	//Currently precisions are used without any alterations as the alpha channel
	
	private boolean isMetaMotif; //FIXME: Change to false ASAP!
	private double[] precisions;
	private double DEFAULT_PRECISION = 255;
	private MetaMotif metaMotif;
	
	private QMotifBoundingBox boundingBox;
	
	private QMotif linkedQMotif;
	private boolean highlighted;
	
	public QMotif getLinkedQMotif() {
		assert  (linkedQMotif == null) || 
				(linkedQMotif != null && linkedQMotif.linkedQMotif == null);
		return linkedQMotif;
	}

	public QMotif(QWidget widget, Motif m) {
    	this.setParent(widget);
    	nmicaMotif = m;
    	setupDists();
    	setSelected(false);

    	precisions = MetaMotifIOTools.alphaSumsFromMotif(m);
    	isMetaMotif = precisions != null;
    	
    	readQMotifAnnotations();
    }
    
    public QMotif(MetaMotif mm) {
    	this(null, mm);
    }
    
    public QMotif(QWidget widget, MetaMotif mm) {
    	this.setParent(widget);
    	//TODO: Actually do the error handling here as it's supposed to :)
    	try {
			nmicaMotif = MetaMotifIOTools.metaMotifToAnnotatedMotif(mm);
	    	setupDists();
	    	setSelected(false);
	    	precisions = MetaMotifIOTools.alphaSumsFromMotif(nmicaMotif);
	    	isMetaMotif = precisions != null;
		} catch (IllegalSymbolException e) {
			e.printStackTrace();
		} catch (IllegalAlphabetException e) {
			e.printStackTrace();
		} catch (BioException e) {
			e.printStackTrace();
		}
		
		readQMotifAnnotations();
    }
    
    private void readQMotifAnnotations() {
		if (nmicaMotif.getAnnotation().containsProperty("offset")) {
			Object o = nmicaMotif.getAnnotation().getProperty("offset");
			if (o instanceof String)
				this.offset = Integer.parseInt((String) o);
			else if (o instanceof Integer)
				this.offset = (Integer) o;
		}
		if (nmicaMotif.getAnnotation().containsProperty("isSelected")) {
			this.selected = true;
		}
		if (nmicaMotif.getAnnotation().containsProperty("isHighlighted")) {
			this.highlighted = true;
		}

		if (nmicaMotif.getAnnotation().containsProperty("selectedColumns")) {
			Object o = nmicaMotif.getAnnotation()
					.getProperty("selectedColumns");
			if (!(o instanceof String))
				throw new IllegalStateException(
						"The selectedColumns annotation is not a String as required!");
			StringTokenizer tok = new StringTokenizer((String) o, ",");

			do {
				selectedColumns.add(Integer.parseInt(tok.nextToken()));
			} while (tok.hasMoreTokens());
		}
		if (nmicaMotif.getAnnotation().containsProperty("highlightedColumns")) {
			Object o = nmicaMotif.getAnnotation().getProperty(
					"highlightedColumns");
			if (!(o instanceof String))
				throw new IllegalStateException(
						"The highlightedColumns annotation is not a String as required!");
			StringTokenizer tok = new StringTokenizer((String) o, ",");

			do {
				highlightedColumns.add(Integer.parseInt(tok.nextToken()));
			} while (tok.hasMoreTokens());
		}
	}
    
	public void updateQMotifAnnotations() {
		Annotation ann = nmicaMotif.getAnnotation();
		ann.setProperty("offset", "" + this.offset);
		ann.setProperty("isSelected", this.selected ? true : false);
		ann.setProperty("isHighlighted", this.highlighted ? true : false);
		if (this.selectedColumns != null && this.selectedColumns.size() > 0) {
			StringBuffer buf = new StringBuffer("" + this.selectedColumns.get(0));
			int i = 1;
			while (i < this.selectedColumns.size()) {
				buf.append("," + this.selectedColumns.get(i));
			}
			ann.setProperty("selectedColumns", buf.toString());
		}
		if (this.highlightedColumns != null && this.highlightedColumns.size() > 0) {
			StringBuffer buf = new StringBuffer("" + this.highlightedColumns.get(0));
			int i = 1;
			while (i < this.highlightedColumns.size()) {
				buf.append("," + this.highlightedColumns.get(i));
			}
			ann.setProperty("highlightedColumns", buf.toString());
		}
	}

	public QMotif(Motif m) {
    	this(null, m);
    }
    
    public QMotif(QWidget parent, QMotif qmotif, Motif sampleMotifFromMetaMotif) {
		this(parent, sampleMotifFromMetaMotif);
		this.linkedQMotif = qmotif;
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
		offset--;
	}
	public void moveBy(int i) {
		offset = offset + i;
	}
	public void moveToRight() {
		offset++;
	}
	
	public void setHorizontalOffset(int offset) {
		this.offset = offset;
	}
	
	public int getHorizontalOffset() {
		return this.offset;
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

	public QMotifBoundingBox getBoundingBox() {
		return this.boundingBox;
	}

	public void setBoundingBox(QMotifBoundingBox motifBoundingBox) {
		this.boundingBox = motifBoundingBox;
	}

	public QMotif[] sampleMotifsFromMetaMotif(QMotif motif,
			String string, int sampleNum) {
		if (!motif.isMetaMotif()) throw new IllegalArgumentException("");
		QMotif[] qms = new QMotif[sampleNum];
		for (int i = 0; i < sampleNum; i++) {
			qms[i] = new QMotif((QWidget)motif.parent(), motif, MetaMotifSimulator.sampleMotifFromMetaMotif(
								motif.getMetaMotif(), "" + i));}
		
		return qms;
	}


	public static List<Motif> qmotifsToMotifs(List<QMotif> qmotifs) {
		List<Motif> motifs = new ArrayList<Motif>();
		for (int i = 0; i < motifs.size(); i++) {
			qmotifs.get(i).updateQMotifAnnotations(); //need to sync the XMS annotations before writing
			motifs.add(qmotifs.get(i).getNmicaMotif());
		}
		return motifs;
	}
}
