
package uk.ac.sanger.motifxplorer.ui.widget;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import net.derkholm.nmica.model.metamotif.Dirichlet;
import net.derkholm.nmica.model.metamotif.MetaMotif;
import net.derkholm.nmica.model.motif.NMWeightMatrix;
import net.derkholm.nmica.motif.Motif;
import net.derkholm.nmica.motif.MotifIOTools;

import org.biojava.bio.dist.Distribution;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.IllegalSymbolException;

import uk.ac.sanger.motifxplorer.ui.model.QMotif;

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
	
	private List<LabelledLogoWidget> widgets = new ArrayList<LabelledLogoWidget>();
	private QVBoxLayout layout;
	
	private QUndoStack undoStack;
	
	public MotifSetView(QWidget parent, List<QMotif> motifs, int maxCols, int xOffset) {
		super(parent);
		
		this.motifs = new ArrayList<QMotif>();

		layout = new QVBoxLayout();
		layout.setMargin(0);
		layout.setContentsMargins(0, 0, 0, 0);
		layout.setSpacing(0);
		layout.setWidgetSpacing(0);
		setLayout(layout);
		
		System.out.println("About to add motifs");
		addMotifs(motifs);
		
		System.out.println("About to set mouse tracking on");
		this.setMouseTracking(true);
		
		if (maxCols < 0)
			this.maxCols = DEFAULT_MAX_COLS;
		else
			this.maxCols = maxCols;
		
		/*
		System.out.println("About to set motifs");
		if (motifs == null)
			this.motifs = new ArrayList<QMotif>();
		else
			this.motifs = motifs;
		*/
		
		undoStack = new QUndoStack();
		
		resize(sizeHint());
		setSizePolicy(QSizePolicy.Policy.Fixed, QSizePolicy.Policy.Fixed);
	}

	public MotifSetView(List<QMotif> m) {
		this(null, m, DEFAULT_MAX_COLS, DEFAULT_X_OFFSET);
	}

	public MotifSetView(List<QMotif> m, int maxCols) {
		this(null, m, maxCols,DEFAULT_X_OFFSET);
	}

	public QSize sizeHint() {
		int height;
		if (motifs != null)
			height = (int)Math.min(LabelledLogoWidget.DEFAULT_TOTAL_WIDGET_HEIGHT, 
								LogoView.MOTIF_HEIGHT * motifs.size());
		else
			height = LabelledLogoWidget.DEFAULT_TOTAL_WIDGET_HEIGHT;
		
		return new QSize(LabelledLogoWidget.DEFAULT_TOTAL_WIDGET_WIDTH, height);
	}
	
	public void addMotifs(List<QMotif> motifs) {
		if (motifs == null) {
			this.motifs = new ArrayList<QMotif>();
		}
		for (QMotif m : motifs) addMotif(m);
		System.out.println("About to determine max cols");
		maxCols = maxCols(this.motifs);
		resize(sizeHint());
		update();
		repaint();
	}
	

	public LabelledLogoWidget addMotif(QMotif qm) {
		if (!this.motifs.contains(qm)) this.motifs.add(qm);
		else throw new IllegalArgumentException("The motif " + qm + " is already present on this motif set widget!");
		
		System.out.println("QMOTIF:" + qm);
		LabelledLogoWidget motifLogoWidget = new LabelledLogoWidget(layout,qm,this.maxCols);
		System.out.println("About to add to layout");
		layout().addWidget(motifLogoWidget);
		System.out.println("About to add to widgets");
		widgets.add(motifLogoWidget);
		//repaint();
		//update();
		System.out.println("About to return");
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
	
	public void keyPressEvent(QKeyEvent e) {
		//super.keyPressEvent(e);
		if (Qt.Key.resolve(e.key()) == Qt.Key.Key_Left)
			for (QMotif m : motifs)
				if (m.isSelected() && (m.parent() instanceof LogoView))
					((LogoView)m.parent()).moveToLeft();

		
		if (Qt.Key.resolve(e.key()) == Qt.Key.Key_Right)
			for (QMotif m : motifs)
				if (m.isSelected() && (m.parent() instanceof LogoView))
					((LogoView)m.parent()).moveToRight();
		
		this.update();
	}

	public List<Distribution[]> allSelectedColumns() {
		List<Distribution[]> dists = new ArrayList<Distribution[]>();
		for (int i = 0; i < motifs.size(); i++) {
			Distribution[] selDists = motifs.get(i).getSelectedColumns();
			dists.add(motifs.get(i).getSelectedColumns());
		}
		return dists;
	}
	
	public List<Dirichlet[]> allSelectedDirichletColumns() {
		List<Dirichlet[]> dists = new ArrayList<Dirichlet[]>();
		for (int i = 0; i < motifs.size(); i++) {
			QMotif m = motifs.get(i);
			MetaMotif mm = m.getMetaMotif();
			dists.add(m.getSelectedDirichletColumns());
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
		List<Motif> motifs = new ArrayList<Motif>();
		for (int i = 0; i < distributions.size(); i++) {
			if (distributions.get(i) != null && distributions.get(i).length > 0) {
				Motif m = new Motif();
				m.setWeightMatrix(
					new NMWeightMatrix(distributions.get(i), distributions.get(i).length, 0));
				motifs.add(m);
			} else {
				System.out.println("" + i + " has no selected elements");
				
			}
		}
		return motifs.toArray(new Motif[motifs.size()]);
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
		MotifSetView widget = new MotifSetView(null);
		widget.addMotifs(QMotif.create(java.util.Arrays.asList(motifs)));
		widget.show();
		QApplication.exec();
	}
	
	public static void writeMotifSet(OutputStream outputStream, MotifSetView widget) throws Exception {
		List<Motif> motifs = QMotif.qmotifsToMotifs(widget.motifs);
		MotifIOTools.writeMotifSetXML(outputStream, motifs.toArray(new Motif[motifs.size()]));
	}
}
