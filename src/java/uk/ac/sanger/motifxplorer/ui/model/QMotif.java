package uk.ac.sanger.motifxplorer.ui.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.derkholm.nmica.metamotif.Dirichlet;
import net.derkholm.nmica.metamotif.MetaMotif;
import net.derkholm.nmica.metamotif.MetaMotifIOTools;
import net.derkholm.nmica.metamotif.MetaMotifTools;
import net.derkholm.nmica.motif.Motif;

import org.biojava.bio.Annotation;
import org.biojava.bio.BioException;
import org.biojava.bio.dist.Distribution;
import org.biojava.bio.dp.WeightMatrix;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.IllegalSymbolException;

import uk.ac.sanger.motifxplorer.ui.graphics.MotifBoundingBox;
import uk.ac.sanger.motifxplorer.ui.graphics.MotifRegion;
import uk.ac.sanger.motifxplorer.ui.graphics.ScoredMotifRegion;
import uk.ac.sanger.motifxplorer.ui.graphics.SelectableMotifRegion;
import uk.ac.sanger.motifxplorer.ui.widget.LogoView;
import uk.ac.sanger.motifxplorer.ui.widget.MotifSetView;

import com.trolltech.qt.core.QObject;
import com.trolltech.qt.core.QRect;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QFont;

//TODO: Save offset and selected columns as annotations!

public class QMotif extends QObject {
	public static final int PRECISION_ALPHA_SCALING = 2;
	public static final int PRECISION_ALPHA_SCALING_FOR_SAMPLE = 3;
	public static enum DrawingStyle {BLOCK, TEXT_LOGO};
	public static enum EditState {READ_WRITE, READ_ONLY};
	
	protected Motif nmicaMotif;
	
	protected List<String> notes = new ArrayList<String>();
	
    public Signal0 loaded = new Signal0();

	protected QFont font = null; 
	
	protected int offset;
	
	private List<QDistribution> dists;
	public SortedSet<Integer> selectedColumns;
	public SortedSet<Integer> highlightedColumns;
	
	private boolean selected;
	
	//Currently precisions are used without any alterations as the alpha channel
	
	private boolean isMetaMotif; //FIXME: Change to false ASAP!
	private double[] precisions;
	private double DEFAULT_PRECISION = 255;
	private MetaMotif metaMotif;
	
	private MotifBoundingBox boundingBox;
	
	private QMotif linkedQMotif;
	private boolean highlighted;
	private List<MotifRegion> regions = new ArrayList<MotifRegion>();
	
	private boolean isSample = false;
	private QColor color;
	private boolean offsetRead = false;
	
	public QMotif getLinkedQMotif() {
		assert  (linkedQMotif == null) || 
				(linkedQMotif != null && linkedQMotif.linkedQMotif == null);
		return linkedQMotif;
	}
	
	public boolean isSample() {
		return this.linkedQMotif != null;
	}
	

	public QMotif(LogoView view, Motif m) {
    	this.setParent(view);
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
    
    public static final Pattern TYPE_PATTERN = Pattern.compile("type:(\\w+)");
    public static final Pattern NAME_PATTERN = Pattern.compile("name:(\\w+)");
    public static final Pattern SELECTED_PATTERN = Pattern.compile("selected:(true|false)");
    public static final Pattern BEGIN_PATTERN = Pattern.compile("begin:(\\d+)");
    public static final Pattern LENGTH_PATTERN = Pattern.compile("length:(\\d+)");
    public static final Pattern SET_ID_PATTERN = Pattern.compile("setId:(\\d+)");
    public static final Pattern COLOR_PATTERN = Pattern.compile("color:(\\#\\d{6,8})");
    public static final Pattern SCORE_PATTERN = Pattern.compile("score:(\\d+\\.{0,1}\\d*)");
    
    public QMotif(LogoView view, MetaMotif mm) {
    	this.setParent(view);
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
		readSelectionState();
		readHighlightState();
		readColors();
		readSelectedColumns();
		readHighlightedColumns();
		readMotifRegions();
		readOffset();
	}
    
    private void readSelectionState() {
    	if (nmicaMotif.getAnnotation().containsProperty("isSelected"))
			if (Boolean.parseBoolean((String)nmicaMotif
					.getAnnotation()
						.getProperty("isSelected")))
		{this.selected = true;}
    }
    
    private void readHighlightState() {
    	if (nmicaMotif.getAnnotation().containsProperty("isHighlighted"))
			if (Boolean.parseBoolean((String)nmicaMotif
					.getAnnotation()
						.getProperty("isHighlighted")))
		{this.highlighted = true;}
    }
    
    private void readColors() {
		if (nmicaMotif.getAnnotation().containsProperty("color")) {
			//System.out.println("Reading color!");
			Object o = nmicaMotif.getAnnotation().getProperty("color");
			if (o instanceof String)
				this.color = new QColor((String)o);
			else
				throw new IllegalStateException(
						"The color annotation is not a String as required!");
		}
    }

    private void readSelectedColumns() {
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
    }
    
    private void readHighlightedColumns() {
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

	private void readMotifRegions() {
		if (!nmicaMotif.getAnnotation().containsProperty("motifregions"))
			return;
			
		Object motifRegsO = nmicaMotif.getAnnotation().getProperty("motifregions");
		int num = 0;
		if (motifRegsO instanceof Integer)
			num = ((Integer)motifRegsO).intValue();
		else if (motifRegsO instanceof String)
			num = Integer.parseInt((String)motifRegsO);
		
		for (int i = 0; i < num; i++) {
			if (nmicaMotif.getAnnotation().containsProperty("motifregion" + i)) {
				MotifRegion reg = readMotifRegion(i);
				if (reg != null) addRegion(reg);
				
			} else {
				System.err.println("WARN: missing annotation \"motifregion" + i + "\"");
			}
		}
	}
    
	private void readOffset() {
		if (!nmicaMotif.getAnnotation().containsProperty("offset"))
			return;
		
		int offset;
		Object offsetObj = nmicaMotif.getAnnotation().getProperty("offset");
		if (offsetObj instanceof Integer) {
			offset = ((Integer)offsetObj).intValue();
		} else if (offsetObj instanceof String) {
			offset = new Integer((String)offsetObj).intValue();
		} else {
			throw new IllegalStateException(
					"Offset annotation ("+offsetObj+") " +
					"is of illegal type: " + offsetObj.getClass().getName());
		}
		moveBy(offset);
	}


	private MotifRegion readMotifRegion(int i) {
		Object annValue = nmicaMotif.getAnnotation().getProperty("motifregion" + i);
		if (annValue instanceof String) {
			String ann = (String)annValue;
			
			String name = null;
			String type = null;
			boolean selected = false;
			int setId = 0;
			int begin = 0;
			int length = 0;
			QColor regionColor = null;
			
			System.out.println(ann);
			Matcher nameMatcher = NAME_PATTERN.matcher(ann);
			Matcher typeMatcher = TYPE_PATTERN.matcher(ann);
			Matcher selectedMatcher = SELECTED_PATTERN.matcher(ann);
			Matcher setIdMatcher = SET_ID_PATTERN.matcher(ann);
			Matcher beginMatcher = BEGIN_PATTERN.matcher(ann);
			Matcher lengthMatcher = LENGTH_PATTERN.matcher(ann);
			Matcher colorMatcher = COLOR_PATTERN.matcher(ann);
			
			if (nameMatcher.find())
				name = nameMatcher.group(1);
			else {
				System.err.println("WARN: name was not found for region " + i);
				name = "";
			}
			if (typeMatcher.find())
				type = typeMatcher.group(1);
			else System.err.println("WARN: type was not found for region " + i);
			if (selectedMatcher.find())
				selected = Boolean.parseBoolean(selectedMatcher.group(1));
			else System.err.println("WARN: selection state was not found for region " + i);
			if (setIdMatcher.find())
				setId = Integer.parseInt(setIdMatcher.group(1));
			else System.err.println("WARN: setId was not found for region " + i);
			if (beginMatcher.find())
				begin = Integer.parseInt(beginMatcher.group(1));
			else System.err.println("WARN: beginning position was not found for region " + i);
			if (lengthMatcher.find())
				length = Integer.parseInt(lengthMatcher.group(1));
			else System.err.println("WARN: length was not found for region " + i);
			if (colorMatcher.find())
				regionColor = new QColor(colorMatcher.group(1));
			
			//FIXME: Support region color for other types but SelectableMotifRegion
			MotifRegion reg;
			if (type.equals("SelectableMotifRegion")) {
				SelectableMotifRegion selReg;
				reg = selReg = new SelectableMotifRegion(null, this, begin, length);
				
				addToAnnotationSet(setId, selReg);
				selReg.setSelectedRegion(selected);
				selReg.setBrushes(regionColor);
			} else if (type.equals("ScoredMotifRegion")) {
				ScoredMotifRegion scorReg;
				reg = scorReg = new ScoredMotifRegion(null,this,begin,length,0);
				
				Matcher scoreMatcher = SCORE_PATTERN.matcher(ann);
				if (scoreMatcher.find())
					scorReg.setScore(Double.parseDouble(scoreMatcher.group(1)));
				
				addToAnnotationSet(setId, scorReg);
				scorReg.setSelectedRegion(selected);
				scorReg.setBrushes(regionColor);
			}
			 else if (type.equals("MotifRegion")) {
				reg = new MotifRegion(null, this, begin, length);
			} else throw new IllegalArgumentException("Type \"" + type + "\" is not supported");
			if (reg != null) {
				System.out.println("name:" + name);
				System.out.println("type:" + type);
				System.out.println("sel :" + selected);
				System.out.println("set :" + setId);
				System.out.println("beg :" + begin);
				System.out.println("len :" + length);
				System.out.println("col :" + color);
				reg.setKept(true);
				reg.setName(name);
				
				return reg;
			}
		} else throw new IllegalStateException("Annotation value is not of type String");
		
		return null;
	}

	private void addToAnnotationSet(int setId, SelectableMotifRegion selReg) {
		LogoView logoView = logoView();
		MotifSetView msetView = null;
		if (logoView != null)
			msetView = logoView().motifSetView();
		if (msetView != null)
			selReg.setAnnotationSet(msetView.getMotifRegionSet(setId));
	}
    
	public void updateQMotifAnnotations() {
		Annotation ann = nmicaMotif.getAnnotation();
		ann.setProperty("offset", "" + this.offset);
		ann.setProperty("isSelected", this.selected ? true : false);
		ann.setProperty("isHighlighted", this.highlighted ? true : false);
		System.out.println("Updating properties");
		if (this.color != null) {
			System.out.println("Setting property color");
			ann.setProperty("color",this.color);
		}
		//FIXME: Iterater rather than use the list
		List<Integer>selCols = new ArrayList<Integer>(this.selectedColumns);
		List<Integer>hilitedCols = new ArrayList<Integer>(this.selectedColumns);
		if (this.selectedColumns != null && this.selectedColumns.size() > 0) {
			StringBuffer buf = new StringBuffer("" + selCols.get(0));
			int i = 1;
			while (i < this.selectedColumns.size()) {
				buf.append("," + selCols.get(i++));
			}
			ann.setProperty("selectedColumns", buf.toString());
		}
		if (this.highlightedColumns != null && this.highlightedColumns.size() > 0) {
			StringBuffer buf = new StringBuffer("" + hilitedCols.get(0));
			int i = 1;
			while (i < this.highlightedColumns.size()) {
				buf.append("," + hilitedCols.get(i++));
			}
			ann.setProperty("highlightedColumns", buf.toString());
		}
		
		if (this.regions != null && this.regions.size() > 0) {
			int rI = 0;
			ann.setProperty("motifregions", this.regions.size());
			for (MotifRegion r : regions) {
				String annotationSetStr = "";
				if (r.getAnnotationSet() != null)
					annotationSetStr = "setId:" + r.getAnnotationSet().getId() + ";";
				
				//type:SelectableMotifRegion;name:tata;selected:true;begin:1;length:2;setId=0
				
				StringBuffer strBuf = new StringBuffer(
										"type:" + r.getClass().getSimpleName() + ";" +
										"name:" + r.getName() + ";" +
										"begin:" + r.getBegin() + ";" +
										"length:" + r.getLength() + ";" +
										annotationSetStr + 
										"color:" + r.getColor() + ";");
				
				if (r instanceof SelectableMotifRegion)
					strBuf.append("selected:" + ((SelectableMotifRegion)r).isSelectedRegion() + ";");
				
				if (r instanceof ScoredMotifRegion)
					strBuf.append("score:" + ((ScoredMotifRegion)r).getScore() + ";");
				
				ann.setProperty("motifregion" + rI++,strBuf);
			}
		}
	}

	public QMotif(Motif m) {
    	this(null, m);
    }
    
    public QMotif(LogoView parent, QMotif qmotif, Motif sampleMotifFromMetaMotif) {
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
    	selectedColumns = new TreeSet<Integer>();
    	highlightedColumns = new TreeSet<Integer>();
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
	
	public void moveTo(int i) {
		offset = i;
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

	public static List<QMotif> create(LogoView logoView, List<Motif> motifs) {
		List<QMotif> qmotifs = new ArrayList<QMotif>();
		for (Motif m : motifs)
			qmotifs.add(new QMotif(logoView,m));
		return qmotifs;
	}
	
	public static List<QMotif> create(List<Motif> motifs) {
		return create(null, motifs);
	}

	public static List<QMotif> create(LogoView logoView, Motif[] motifs) {
		List<QMotif> qms = new ArrayList<QMotif>();
		
		for (Motif m : motifs)
			qms.add(new QMotif(logoView,m));
		
		return qms;
	}

	public static List<QMotif> motifsToQMotifs(Motif[] motifs) {
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
	public SortedSet<Integer> getHighlightedColumns() {
		return highlightedColumns;
	}

	/**
	 * @param highlightedColumns the highlightedColumns to set
	 */
	public void setHighlightedColumns(TreeSet<Integer> highlightedColumns) {
		this.highlightedColumns = highlightedColumns;
	}

	public Dirichlet[] getSelectedDirichletColumns() {
		Dirichlet[] selections  = new Dirichlet[selectedColumns.size()];
		
		int selI = 0;
		for (Integer i : selectedColumns)
			selections[selI++] = this.getMetaMotif().getColumn(i);
			
		return selections;
	}
	
	public Distribution[] getSelectedColumns() {
		Distribution[] selections  = new Distribution[selectedColumns.size()];
		System.out.println("sels:" + selections.length);
		System.out.println("wm  :" + this.getNmicaMotif().getWeightMatrix().columns());
		int selI = 0;
		for (Integer i : selectedColumns) {
			System.out.println("i :" + i);
			System.out.println("selI:" + selI);
			selections[selI++] = this.getNmicaMotif().getWeightMatrix().getColumn(i);
		}
			
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
	
	public void toggleSelected() {
		selected = !selected;
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
			
			//careful there, otherwise it'll be treated as int
			if (this.selectedColumns.contains(column))
				this.selectedColumns.remove(new Integer(column)); 
		}
		for (int i : selectedColumns)
			System.out.print(i + " ");
		
		System.out.println();
	}
	
	public void toggleColumnSelected(int column) {
		this.setColumnSelected(column,!columnIsSelected(column));
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

	public MotifBoundingBox getBoundingBox() {
		return this.boundingBox;
	}

	public void setBoundingBox(MotifBoundingBox motifBoundingBox) {
		this.boundingBox = motifBoundingBox;
	}

	public QMotif[] sampleMotifsFromMetaMotif(QMotif motif,
			String string, int sampleNum) {
		if (!motif.isMetaMotif()) throw new IllegalArgumentException("The input QMotif is not a metamotif");
		QMotif[] qms = new QMotif[sampleNum];
		for (int i = 0; i < sampleNum; i++) {
			qms[i] = new QMotif((LogoView)motif.parent(), motif, 
								MetaMotifTools.sampleMotifFromMetaMotif(
								motif.getMetaMotif(), "" + i));}
		return qms;
	}

	public static List<Motif> qmotifsToMotifs(List<QMotif> qmotifs) {
		List<Motif> motifs = new ArrayList<Motif>();
		for (int i = 0; i < qmotifs.size(); i++) {
			qmotifs.get(i).updateQMotifAnnotations(); //need to sync the XMS annotations before writing
			motifs.add(qmotifs.get(i).getNmicaMotif());
		}
		return motifs;
	}

	
	public void addRegion(MotifRegion ar) {
		LogoView view = null;
		if (this.parent() != null)
			view = (LogoView)this.parent();
		
		if (view != null && view.scene() != null) { 
			view.scene().addItem(ar);
		} else {System.out.println("View scene is null when adding " + ar.getName());}
		
		regions.add(ar);
		if (this.boundingBox != null) 
			ar.setParentItem(this.boundingBox);
		else {System.out.println("Bounding box is null when adding " + ar.getName());}
		
		ar.updateLocation();
		if (view != null)
			view.repaint();
	}
	
	public int regions() {
		return this.regions.size();
	}
	
	public MotifRegion getRegion(int i) {
		return this.regions.get(i);
	}
	
	public void removeRegions(MotifRegion... mreg) {
		for (MotifRegion mr : mreg)
			mr.removeFromScene();
		
		this.regions.removeAll(Arrays.asList(mreg));
	}

	public Set<Integer> getSelectedColumnIndices() {
		return new TreeSet<Integer>(selectedColumns);
	}

	public int overlappingRegions(int begin, int length) {
		int maxOverlaps = 0;
		for (int i = 0; i < length; i++) {
			int overlapsAtThisPos = 0;
			for (MotifRegion reg : this.regions) {
				int regBegin = reg.getBegin();
				int regEnd = (reg.getBegin() + reg.getLength() - 1);
				int thisBegin = i+begin;
				int thisEnd = (i+begin+length - 1);
				
				boolean beginsBeforeOrAt = regBegin <= thisBegin;
				boolean endsAtOrAfter = regEnd >= thisEnd;
				
				boolean endsBeforeOrAtBeginOf = thisEnd <= regBegin;
				boolean beginsBeforeEndOf = thisBegin <= regEnd;
				if ((beginsBeforeOrAt && endsAtOrAfter) ||
					(endsBeforeOrAtBeginOf || beginsBeforeEndOf))
					overlapsAtThisPos++;
			}
			if (overlapsAtThisPos > maxOverlaps)
				maxOverlaps = overlapsAtThisPos;
		}
		return maxOverlaps;
	}
	
	public void setColor(QColor color) {
		this.color = color;
	}
	
	public QColor color() {
		return this.color;
	}
	
	public LogoView logoView() {
		QObject parent = this.parent();
		if (parent != null)
			if (parent instanceof LogoView)
				return (LogoView)parent;
			else throw new IllegalStateException("Parent is not of type LogoView");
		else
			return null;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

}