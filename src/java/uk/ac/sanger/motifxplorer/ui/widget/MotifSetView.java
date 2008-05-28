
package uk.ac.sanger.motifxplorer.ui.widget;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.derkholm.nmica.metamotif.Dirichlet;
import net.derkholm.nmica.metamotif.MetaMotif;
import net.derkholm.nmica.motif.Motif;
import net.derkholm.nmica.motif.MotifIOTools;
import net.derkholm.nmica.motif.NMWeightMatrix;

import org.biojava.bio.dist.Distribution;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.IllegalSymbolException;

import uk.ac.sanger.motifxplorer.ui.graphics.MotifRegion;
import uk.ac.sanger.motifxplorer.ui.graphics.MotifRegionSet;
import uk.ac.sanger.motifxplorer.ui.graphics.SelectableMotifRegion;
import uk.ac.sanger.motifxplorer.ui.model.QMotif;
import uk.ac.sanger.motifxplorer.util.ColorBox;

import com.trolltech.qt.core.QSize;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QFrame;
import com.trolltech.qt.gui.QKeyEvent;
import com.trolltech.qt.gui.QSizePolicy;
import com.trolltech.qt.gui.QUndoStack;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

public class MotifSetView extends QFrame {
	private static final int NUM_SPACER_COLS = 2;
	private static final int MAX_NUM_COLS = 20;
	private List<QMotif> motifs;
	
	private int maxCols = LogoView.DEFAULT_MAX_COLS;
	public static final int DEFAULT_MAX_COLS = 16;
	public static final int DEFAULT_X_OFFSET = 0;
	
	private List<LabelledLogoView> widgets = new ArrayList<LabelledLogoView>();
	private QVBoxLayout layout;
	
	private QUndoStack undoStack;
	private boolean infoContentScale;
	private String name;
	
	private static final ColorBox<QMotif> colorBox = new ColorBox<QMotif>();
	private static final ColorBox<MotifRegionSet> ANNOTATION_COLORS = new ColorBox<MotifRegionSet>();
	public static ColorBox<MotifRegionSet> annotationColors() {return ANNOTATION_COLORS;}
	
	private final HashMap<Integer,MotifRegionSet> motifRegionSets = new HashMap<Integer,MotifRegionSet>();
	
	public MotifRegionSet newRegionSet() {
		MotifRegionSet set = new MotifRegionSet(this);
		motifRegionSets.put(set.getId(),set);
		return set;
	}

	public boolean containsMotifRegionSet(int setId) {
		return motifRegionSets.containsKey(setId);
	}
	
	public boolean containsMotifRegionSet(MotifRegionSet mset) {
		return motifRegionSets.containsKey(mset.getId());
	}
	
	public void addRegionSet(MotifRegionSet set) {
		if (!motifRegionSets.containsKey(set.getId()))
			motifRegionSets.put(set.getId(),set);
		else throw new IllegalArgumentException(
				"Region set is already contained");
	}
	
	public void removeRegionSet(MotifRegionSet set) {
		if (motifRegionSets.containsKey(set.getId()))
			motifRegionSets.remove(set);
		else throw new IllegalArgumentException(
				"Region set is not contained by this motif set view");
	}

	public HashMap<Integer, MotifRegionSet> getMotifRegionSets() {
		return this.motifRegionSets;
	}
	
	public MotifRegionSet getMotifRegionSetWithName(String name) {
		for (Integer i : this.motifRegionSets.keySet())
			if (motifRegionSets.get(i).getName().equals(name)) {
				System.out.println("Region set with name " + name + " was found.");
				return motifRegionSets.get(i);
			}
		
		//FIXME: Correct the motif region set naming and get rid of this workaround
		for (Integer i : this.motifRegionSets.keySet()) {
			for (MotifRegion mreg : motifRegionSets.get(i).getRegions())
				if (mreg.getName().equals(name)) {
					System.out.println("Region set with name " + name + " was found (name != set name).");
					return motifRegionSets.get(i);
				}
		}
	
		return null;
	}
	
	
	public MotifRegionSet getMotifRegionSet(int setId) {
		if (containsMotifRegionSet(setId)) return motifRegionSets.get(setId);
		else {
			MotifRegionSet regionSet = new MotifRegionSet(this,setId);
			motifRegionSets.put(setId,regionSet);
			return regionSet;
		}
	}
	
	public int logoWidgets() {
		return widgets.size();
	}
	
	public LabelledLogoView getLabelledLogoWidget(int i) {
		return widgets.get(i);
	}
	
	public MotifSetView(String name, QWidget parent, List<QMotif> motifs, int maxCols, int xOffset, boolean infoContentScale) {
		super(parent);
		this.name = name;
		this.infoContentScale = true;
		this.motifs = new ArrayList<QMotif>();

		layout = new QVBoxLayout();
		layout.setMargin(0);
		layout.setContentsMargins(0, 0, 0, 0);
		layout.setSpacing(0);
		layout.setWidgetSpacing(0);
		setLayout(layout);
		
		addMotifs(motifs);
		
		this.setMouseTracking(true);
		
		if (maxCols < 0)
			this.maxCols = DEFAULT_MAX_COLS;
		else
			this.maxCols = maxCols;
		
		undoStack = new QUndoStack();
		
		resize(sizeHint());
		setSizePolicy(QSizePolicy.Policy.Fixed, QSizePolicy.Policy.Fixed);
	}

	public MotifSetView(String name, List<QMotif> m, boolean infoContentScale) {
		this(name, null, m, DEFAULT_MAX_COLS, DEFAULT_X_OFFSET, infoContentScale);
	}

	public MotifSetView(String name, List<QMotif> m, int maxCols, boolean infoContentScale) {
		this(name, null, m, maxCols,DEFAULT_X_OFFSET, infoContentScale);
	}

	public QSize sizeHint() {
		int height;
		if (motifs != null)
			height = (int)Math.min(LabelledLogoView.DEFAULT_TOTAL_WIDGET_HEIGHT, 
								LogoView.MOTIF_HEIGHT * motifs.size());
		else
			height = LabelledLogoView.DEFAULT_TOTAL_WIDGET_HEIGHT;
		
		return new QSize(LabelledLogoView.DEFAULT_TOTAL_WIDGET_WIDTH, height);
	}
	
	//FIXME: Change the selected
	public void removeMotif(QMotif motif) {
		layout.removeWidget(motif.logoView());
		layout.update();
		motifs.remove(motif);
		update();
	}
	
	public void addMotifs(List<QMotif> motifs) {
		if (motifs == null) {
			this.motifs = new ArrayList<QMotif>();
			maxCols = MAX_NUM_COLS;
		} else {
			for (QMotif m : motifs) addMotif(m);
			maxCols = maxCols(this.motifs);
		}
		resize(sizeHint());
		repaint();
	}

	public LabelledLogoView addMotif(QMotif qm) {
		if (!this.motifs.contains(qm)) this.motifs.add(qm);
		//else throw new IllegalArgumentException("The motif " + qm + " is already present on this motif set widget!");
		
		qm.setColor(colorBox.colorFor(qm));
		LabelledLogoView motifLogoWidget = new LabelledLogoView(this,qm,this.maxCols, infoContentScale);
		layout().addWidget(motifLogoWidget);
		
		widgets.add(motifLogoWidget);
		return motifLogoWidget;
	}
	
	private int maxCols(List<QMotif> motifs) {
		int maxCols = MAX_NUM_COLS;
		if (motifs != null) {
			for (int i = 0; i < motifs.size(); i++) {
				QMotif m = motifs.get(i);
				if (m.getNmicaMotif().getWeightMatrix().columns() > (maxCols - NUM_SPACER_COLS))
					maxCols = m.getNmicaMotif().getWeightMatrix().columns() + NUM_SPACER_COLS;
			}
		}
		return Math.max(maxCols, MAX_NUM_COLS);
	}

	
	/**
	 * @return the maxCols
	 */
	public int getMaxCols() {
		return maxCols;
	}

	/**
	 * @param maxCols the maxCols to set
	 */
	public void setMaxCols(int maxCols) {
		this.maxCols = maxCols;
	}

	/**
	 * @return the motifs
	 */
	public List<QMotif> getMotifs() {
		return motifs;
	}

	public void initWithEmptyLogoWidget() {
		addMotifs(null);
		addMotif(null);
	}

	/**
	 * @return the undoStack
	 */
	public QUndoStack getUndoStack() {
		return undoStack;
	}

	/**
	 * @param undoStack the undoStack to set
	 */
	public void setUndoStack(QUndoStack undoStack) {
		this.undoStack = undoStack;
	}
	
	/*
	public void mouseDoubleClickEvent(QMouseEvent e) {
		System.out.println(e.pos().x() + "," + e.pos().y());
		int column = wmColumn(e.pos(),rect(),maxCols);
		System.out.println(column);
		toggleSelection(column);
	}

	public void toggleSelection(int i) {
		nmicaMotif.toggleSelection(i);
		repaint();
	}
*/
	
	public void moveSelectedMotifsBy(int i) {
		moveMotifs(getSelectedMotifs(),i);
	}
	
	public void moveMotifs(List<QMotif> motifs, int i) {
		for (QMotif m : motifs) {
			
		}
	}
	
	//TODO: Keep a list of selected motifs rather than generating one on every request
	public List<QMotif> getSelectedMotifs() {
		List<QMotif> selectedMotifs = new ArrayList<QMotif>();
		for (QMotif m : motifs)
			if (m.isSelected())
				selectedMotifs.add(m);
		return selectedMotifs;
	}
	
	
	// FIXME: figure out how you can keep both this 
	// and the keyboard shortcuts in the possibly containing MXplor
	public void keyPressEvent(QKeyEvent e) {
		//super.keyPressEvent(e);
		/*if (Qt.Key.resolve(e.key()) == Qt.Key.Key_Left)
			for (QMotif m : motifs)
				if (m.isSelected() && (m.parent() instanceof LogoView))
					((LogoView)m.parent()).moveToLeft();

		
		if (Qt.Key.resolve(e.key()) == Qt.Key.Key_Right)
			for (QMotif m : motifs)
				if (m.isSelected() && (m.parent() instanceof LogoView))
					((LogoView)m.parent()).moveToRight();
		
		this.update();
		*/
	}

	public List<Distribution[]> allSelectedColumns() {
		List<Distribution[]> dists = new ArrayList<Distribution[]>();
		for (int i = 0; i < motifs.size(); i++) {
			Distribution[] selDists;
			if (!motifs.get(i).isSelected())
				selDists = motifs.get(i).getSelectedColumns();
			else {
				selDists = new Distribution[motifs.get(i).getNmicaMotif().getWeightMatrix().columns()];
				for (int j = 0; j < selDists.length; j++) {
					selDists[j] = motifs.get(i).getNmicaMotif().getWeightMatrix().getColumn(j);
				}
			}
			dists.add(selDists);
		}
		return dists;
	}
	
	public List<Double[]> allSelectedColumnPrecisions() {
		List<Double[]> alphaSums = new ArrayList<Double[]>();
		for (int i = 0; i < motifs.size(); i++) {
			Dirichlet[] selDists = null;
			Double[] precs;
			if (!motifs.get(i).isSelected())
				precs = new Double[motifs.get(i).getSelectedColumns().length];
			else
				precs = new Double[motifs.get(i).getNmicaMotif().getWeightMatrix().columns()];
			
			if (motifs.get(i).isMetaMotif()) {
				if (!motifs.get(i).isSelected())
					selDists = motifs.get(i).getSelectedDirichletColumns();
				else {
					selDists = new Dirichlet[motifs.get(i).getMetaMotif().columns()];
					for (int j = 0; j < selDists.length; j++)
						selDists[j] = motifs.get(i).getMetaMotif().getColumn(j);
				}
				//precs = new Double[selDists.length];
				for (int j = 0; j < selDists.length; j++)
					precs[j] = selDists[j].alphaSum();
				alphaSums.add(precs);
			} else {
				for (int j = 0; j < precs.length; j++)
					precs[j] = 0.0;
				
				alphaSums.add(precs);
			}
			
		}
		return alphaSums;
	}
	
	public List<Dirichlet[]> allSelectedDirichletColumns() {
		List<Dirichlet[]> dists = new ArrayList<Dirichlet[]>();
		for (int i = 0; i < motifs.size(); i++) {
			QMotif m = motifs.get(i);
			MetaMotif mm = m.getMetaMotif();
			if (!m.isSelected())
				dists.add(m.getSelectedDirichletColumns());
			else {
				Dirichlet[] dirs = new Dirichlet[mm.columns()];
				for (int j = 0; j < mm.columns(); j++)
					dirs[j] = mm.getColumn(j);
				
				dists.add(dirs);
			}
		}
		return dists;
	}
	
	//FIXME: Motif and Metamotif should really have some common interface... 
	//or you could just return weight matrices
	//just fail if you don't have metamotifs (if the precisions are null)
	public MetaMotif[] allSelectedColumnsAsMetaMotifs() {
		List<Dirichlet[]> dirichlets = allSelectedDirichletColumns();
		List<MetaMotif> metamotifs = new ArrayList<MetaMotif>();
		for (int i = 0; i < dirichlets.size(); i++)
			try {
				if (dirichlets.get(i) != null && dirichlets.get(i).length > 0) {
					MetaMotif mm = new MetaMotif(dirichlets.get(i));
					metamotifs.add(mm);
				}
			} catch (IllegalSymbolException e) {
				e.printStackTrace();
			}
		return metamotifs.toArray(new MetaMotif[metamotifs.size()]);
	}
	
	public Motif[] allSelectedColumnsAsMotifs() 
		throws IllegalSymbolException,IllegalAlphabetException {
		List<Distribution[]> distributions = allSelectedColumns();
		List<Double[]> precisions = allSelectedColumnPrecisions();
		List<Motif> motifs = new ArrayList<Motif>();
		for (int i = 0; i < distributions.size(); i++) {
			if (distributions.get(i) != null && distributions.get(i).length > 0) {
				Motif m = new Motif();
				m.setWeightMatrix(
					new NMWeightMatrix(distributions.get(i), distributions.get(i).length, 0));
				
				for (int j = 0; j < m.getWeightMatrix().columns(); j++)
					m.getAnnotation().setProperty("alphasum;column:"+j, precisions.get(i)[j]);
				motifs.add(m);
			} else {
				//System.out.println("" + i + " has no selected elements");
				
			}
		}
		return motifs.toArray(new Motif[motifs.size()]);
	}
	
	public HashMap<String,List<SelectableMotifRegion>> selectedMotifRegionsPerSet() {
    	SelectableMotifRegion[] motRegs = selectedMotifRegions();
    	HashMap<String, List<SelectableMotifRegion>> 
    		regMap = new HashMap<String, List<SelectableMotifRegion>>();
    	
    	for (SelectableMotifRegion r : motRegs) {
    		if (!regMap.containsKey(r.getName())) 
    			regMap.put(r.getName(),new ArrayList<SelectableMotifRegion>());
    	}
    	
		for (QMotif m : motifs)
    		for (int i = 0,length = m.regions(); i < length; i++) {
    			if (m.getRegion(i) instanceof SelectableMotifRegion) {
    				SelectableMotifRegion mreg = (SelectableMotifRegion)m.getRegion(i);
    				if (mreg.isSelectedRegion()) 
    					regMap.get(mreg.getName()).add(mreg);
    			}
    		}
		
		return regMap;
	}
	
	public SelectableMotifRegion[] selectedMotifRegions() {
    	List<SelectableMotifRegion> motRegs = new ArrayList<SelectableMotifRegion>();
		for (QMotif m : motifs)
    		for (int i = 0,length = m.regions(); i < length; i++)
    			if (m.getRegion(i) instanceof SelectableMotifRegion) {
    				SelectableMotifRegion mreg = (SelectableMotifRegion)m.getRegion(i);
    				if (mreg.isSelectedRegion()) motRegs.add(mreg);
    			}
		
		return motRegs.toArray(new SelectableMotifRegion[motRegs.size()]);
	}

	public HashMap<String,List<Motif>> selectedAnnotationsAsMotifsPerRegionSet() {
		HashMap<String, List<SelectableMotifRegion>> 
				selregs = selectedMotifRegionsPerSet();
		
		HashMap<String, List<Motif>> annotationMotifsPerRegionSet = 
				new HashMap<String, List<Motif>>();
		
		int i = 0;
		for (String k : selregs.keySet()) {
			List<Motif> motifs = new ArrayList<Motif>();
			annotationMotifsPerRegionSet.put(k, motifs);
			for (SelectableMotifRegion sr : selregs.get(k)) {
				Motif m = sr.toMotif();
				m.setName(k);
				motifs.add(m);
			}
		}
		
		return annotationMotifsPerRegionSet;
	}
	
	public Motif[] selectedAnnotationsAsMotifs() {
		SelectableMotifRegion[] motRegs = selectedMotifRegions();
		Motif[] ms = new Motif[motRegs.length];
		for (int i = 0; i < motRegs.length; i++)
			ms[i] = motRegs[i].toMotif();
		return ms;
	}
	
	public static void main(String args[]) {
		Motif[] motifs = null;
		try {
			motifs = MotifIOTools.loadMotifSetXML(
					new FileInputStream("/Users/mz2/workspace/NestedMICA/metamotifs/sim/34567.xms"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (motifs == null)
			System.exit(1);
		
		
		QApplication.initialize(args);
		MotifSetView widget = new MotifSetView("untitled",null, true);
		widget.addMotifs(QMotif.create(java.util.Arrays.asList(motifs)));
		widget.show();
		QApplication.exec();
	}
	
	public static void writeMotifSet(OutputStream outputStream, MotifSetView widget) throws Exception {
		List<Motif> motifs = QMotif.qmotifsToMotifs(widget.motifs);
		MotifIOTools.writeMotifSetXML(outputStream, motifs.toArray(new Motif[motifs.size()]));
	}

	public void setName(String s) {
		this.name = s;
	}
	public String getName() {
		return this.name;
	}

	public void removeMotifRegions(SelectableMotifRegion[] selRegs) {
		for (QMotif m : motifs)
			m.removeRegions(selRegs);
	}


}
