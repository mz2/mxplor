package uk.ac.sanger.motifxplorer.app;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.derkholm.nmica.maths.NativeMath;
import net.derkholm.nmica.model.metamotif.DirichletParamEstimator;
import net.derkholm.nmica.model.metamotif.MetaMotif;
import net.derkholm.nmica.model.metamotif.MetaMotifIOTools;
import net.derkholm.nmica.motif.Motif;
import net.derkholm.nmica.motif.MotifIOTools;
import net.derkholm.nmica.motif.MotifTools;

import org.biojava.bio.BioError;
import org.biojava.bio.BioException;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.IllegalSymbolException;

import uk.ac.sanger.motifxplorer.cmd.ReverseComplementCommand;
import uk.ac.sanger.motifxplorer.cmd.ShiftCommand;
import uk.ac.sanger.motifxplorer.nmica.NestedMICATask;
import uk.ac.sanger.motifxplorer.ui.graphics.MotifRegion;
import uk.ac.sanger.motifxplorer.ui.graphics.MotifRegionSet;
import uk.ac.sanger.motifxplorer.ui.graphics.ScoredMotifRegion;
import uk.ac.sanger.motifxplorer.ui.graphics.SelectableMotifRegion;
import uk.ac.sanger.motifxplorer.ui.model.QMotif;
import uk.ac.sanger.motifxplorer.ui.widget.LabelledLogoView;
import uk.ac.sanger.motifxplorer.ui.widget.LogoView;
import uk.ac.sanger.motifxplorer.ui.widget.MotifSetView;

import com.trolltech.qt.core.QEvent;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.core.QRect;
import com.trolltech.qt.core.QRectF;
import com.trolltech.qt.core.QSize;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QCloseEvent;
import com.trolltech.qt.gui.QFileDialog;
import com.trolltech.qt.gui.QFileOpenEvent;
import com.trolltech.qt.gui.QGraphicsScene;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QInputDialog;
import com.trolltech.qt.gui.QMainWindow;
import com.trolltech.qt.gui.QMenu;
import com.trolltech.qt.gui.QMenuBar;
import com.trolltech.qt.gui.QPainter;
import com.trolltech.qt.gui.QPrinter;
import com.trolltech.qt.gui.QScrollArea;
import com.trolltech.qt.gui.QShowEvent;
import com.trolltech.qt.gui.QSizePolicy;
import com.trolltech.qt.gui.QStatusBar;
import com.trolltech.qt.gui.QTableView;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

public class MXplor extends QMainWindow {
    private static final double DEFAULT_PSEUDOCOUNT = 0.0000001;
	private static final String SET_NAME_PROPERTY = "setId";
	private static final String DEFAULT_SET_NAME = "untitled";
	private static final int NUM_MOTIFS_SHOWN_AT_A_TIME = 6;
    //private MotifTableModel motifTableModel = new MotifTableModel(this);
    //private List<QMotif> motifs;
    //private String fileName;
    
    public QAction actionNew;
    public QAction actionSaveAll;
    public QAction actionExport_PDF;
    public QAction actionOpen;
    public QAction actionClose;
    public QAction actionQuit;
    public QAction actionShift_left;
    public QAction actionShift_right;
    public QAction actionReverse_complement;
    //public QAction actionBest_hits;
    //public QAction actionBest_reciprocal_hits;
    public QAction actionMLEMetaMotif;
    public QWidget centralwidget;
    public QVBoxLayout vboxLayout;
    public QTableView motifTableView;
    public QMenuBar menubar;
    public QMenu menuMotifExplorer;
    public QMenu menuEdit;
    public QMenu menuAnalysis;
    public QMenu menuBestHits;
    public QMenu menuBestReciprocalHits;
    public QMenu menuBestMetaMotifHits;
    
    public QStatusBar statusbar;
    private boolean abort = false;


	private boolean nmicaStarted = false;
	private boolean infoContentScale = true;
    private NestedMICATask nmicaTask;
    private Signal2<Motif, Integer> 
    	cycleResultsReady = new Signal2<Motif, Integer>();
	
    private Signal2<MXplor, MXplor> 
		bestHitsSignal = new Signal2<MXplor, MXplor>();

    private Signal2<MXplor, MXplor> 
		bestReciprocalHitsSignal = new Signal2<MXplor, MXplor>();

    private Signal2<MXplor, MXplor> 
    	bestMetaMotifHits = new Signal2<MXplor, MXplor>();
    
    
    protected static List<MXplor> allMotifSetWindows = new ArrayList<MXplor>();
    
    
	private MotifSetView motifSetView;
	private QScrollArea scrollArea;
	private boolean startNMICA;
	private String[] args;
	private QAction actionAdd;
	private QAction actionExtractMotifs;
	private QAction actionUndo;
	private QAction actionRedo;
	private String savedFileName;
	private QAction actionBestMetaMotifHits;
	
	private QAction actionKeepScoredRegions;
	private QAction actionRemoveScoredRegions;
	private QAction actionAddAnnotations;
	
	
	//public static final int DEFAULT_MIN_SCROLL_AREA_HEIGHT = 600;
	//public static final int DEFAULT_MAX_SCROLL_AREA_HEIGHT = 600;
	
	public static final int DEFAULT_MIN_WINDOW_WIDTH = LabelledLogoView.DEFAULT_TOTAL_WIDGET_WIDTH - 100;
	public static final int DEFAULT_MAX_WINDOW_WIDTH = LabelledLogoView.DEFAULT_TOTAL_WIDGET_WIDTH;
	public static final int DEFAULT_MIN_WINDOW_HEIGHT = 400;
	public static final int DEFAULT_MAX_WINDOW_HEIGHT = 600;
	public static final int DEFAULT_MIN_COL_HEIGHT = LogoView.MOTIF_HEIGHT ;

	private boolean menusNeedUpdating;
	//private String name;
	
	//TODO: Make configurable and sync with the default value from MetaMotifFinder
	private double pseudoCounts = 0.001;
	private QMenu menuAnnotate;
	private QAction actionRenameAnnotations;
	private QAction actionExpandAnnotationSelection;
	private QAction actionExtractMotifsFromAnnotations;
	private QAction actionSelectAllAnnotations;
	private QAction actionSelectNoneAnnotations;
	private QAction actionMLEMetaMotifsFromAnnotations;
	private QAction actionSave;
	
	
	
	public MXplor(String name, List<QMotif> motifs) {
		this(name, motifs, MotifSetView.DEFAULT_MAX_COLS, MotifSetView.DEFAULT_X_OFFSET);
	}
	
	public MXplor(String name, List<QMotif> motifs, int maxCols, int xOffset) {
		this(name, motifs, maxCols, xOffset, null, false);
	}
	
	public MXplor(	String name, 
					List<QMotif> motifs, 
					int maxCols, 
					int xOffset, 
					String[] args, 
					boolean startNMICA) {
		setupUi(name, motifs, maxCols, xOffset);
		this.startNMICA = startNMICA;
		this.args = args;
		
		MXplor.allMotifSetWindows.add(this);
		menusNeedUpdating();
		resizeWindowAndScrollArea();
		show();
	}
	
	private void setupUi(String name, List<QMotif> motifs, int maxCols,int xOffset) {
		//System.out.println("Setting up UI");
		this.setMinimumWidth(DEFAULT_MIN_WINDOW_WIDTH);
		this.setMaximumWidth(DEFAULT_MAX_WINDOW_WIDTH);
		this.setMaximumHeight(DEFAULT_MAX_WINDOW_HEIGHT);
		
		scrollArea = new QScrollArea();
		scrollArea.setMinimumWidth(DEFAULT_MIN_WINDOW_WIDTH);
		scrollArea.setMinimumWidth(DEFAULT_MAX_WINDOW_WIDTH);
		
		scrollArea.setMinimumHeight(DEFAULT_MIN_WINDOW_HEIGHT);
		scrollArea.setMaximumHeight(DEFAULT_MAX_WINDOW_HEIGHT);
		this.setSizePolicy(QSizePolicy.Policy.Preferred,QSizePolicy.Policy.Preferred);
		scrollArea.setSizePolicy(QSizePolicy.Policy.Preferred, QSizePolicy.Policy.Preferred);
		scrollArea.setWidgetResizable(true);
		motifSetView = new MotifSetView(name, scrollArea, motifs, maxCols, xOffset, infoContentScale);
		motifSetView.setMinimumWidth(DEFAULT_MIN_WINDOW_WIDTH);
		scrollArea.setWidget(motifSetView);
		
		scrollArea.setHorizontalScrollBarPolicy(Qt.ScrollBarPolicy.ScrollBarAlwaysOff);
		setCentralWidget(scrollArea);
		scrollArea.setParent(this);
        actionNew = new QAction(this);
        actionNew.setObjectName("actionNew");
        actionSaveAll = new QAction(this);
        actionSaveAll.setObjectName("actionSaveAll");
        actionSave = new QAction(this);
        actionSave.setObjectName("actionSave");        
        actionExport_PDF = new QAction(this);
        actionExport_PDF.setObjectName("actionExport_PDF");
        actionOpen = new QAction(this);
        actionOpen.setObjectName("actionOpen");
        actionAdd = new QAction(this);
        actionAdd.setObjectName("actionAdd");
        actionClose = new QAction(this);
        actionClose.setObjectName("actionClose");
        actionQuit = new QAction(this);
        actionQuit.setObjectName("actionQuit");
        actionUndo = new QAction(this);
        actionUndo.setObjectName("actionUndo");
        actionRedo = new QAction(this);
        actionRedo.setObjectName("actionRedo");
        
        actionRemoveScoredRegions = new QAction(this);
        actionRemoveScoredRegions.setObjectName("actionRemoveScoredRegions");
        actionAddAnnotations = new QAction(this);
        actionAddAnnotations.setObjectName("actionAddAnnotations");
        actionRenameAnnotations = new QAction(this);
        actionRenameAnnotations.setObjectName("actionRenameAnnotations");
        actionSelectAllAnnotations = new QAction(this);
        actionSelectAllAnnotations.setObjectName("actionSelectAllAnnotations");
        actionSelectNoneAnnotations = new QAction(this);
        actionSelectNoneAnnotations.setObjectName("actionSelectNoneAnnotations");
        actionExpandAnnotationSelection = new QAction(this);
        actionExpandAnnotationSelection.setObjectName("actionExpandAnnotationSelection");
        actionExtractMotifsFromAnnotations = new QAction(this);
        actionExtractMotifsFromAnnotations.setObjectName("actionExtractMotifsFromAnnotations");
        actionMLEMetaMotifsFromAnnotations = new QAction(this);
        actionMLEMetaMotifsFromAnnotations.setObjectName("actionMLEMetaMotifsFromAnnotations");
        
        actionShift_left = new QAction(this);
        actionShift_left.setObjectName("actionShift_left");
        actionShift_right = new QAction(this);
        actionShift_right.setObjectName("actionShift_right");
        
        actionReverse_complement = new QAction(this);
        actionReverse_complement.setObjectName("actionReverse_complement");
        actionExtractMotifs = new QAction(this);
        actionExtractMotifs.setObjectName("actionExtractMotifs");
        
        actionMLEMetaMotif = new QAction(this);
        actionMLEMetaMotif.setObjectName("actionMLEMetaMotif");
        actionBestMetaMotifHits = new QAction(this);
        actionBestMetaMotifHits.setObjectName("actionBestHits");
        menubar = new QMenuBar(this);
        menubar.setObjectName("menubar");
        menubar.setGeometry(new QRect(0, 0, 800, 22));
        menuMotifExplorer = new QMenu(menubar);
        menuMotifExplorer.setObjectName("menuMotifExplorer");
        menuEdit = new QMenu(menubar);
        menuEdit.setObjectName("menuEdit");
        
        menuAnnotate = new QMenu(menubar);
        menuAnnotate.setObjectName("menuAnnotate");
        
        menuAnalysis = new QMenu(menubar);
        menuAnalysis.setObjectName("menuAnalysis");
        
        this.setMenuBar(menubar);
        statusbar = new QStatusBar(this);
        statusbar.setObjectName("statusbar");
        this.setStatusBar(statusbar);

        menubar.addAction(menuMotifExplorer.menuAction());
        menubar.addAction(menuEdit.menuAction());
        menubar.addAction(menuAnnotate.menuAction());
        menubar.addAction(menuAnalysis.menuAction());
        
        menuMotifExplorer.addAction(actionNew);
        menuMotifExplorer.addSeparator();
        menuMotifExplorer.addAction(actionOpen);
        menuMotifExplorer.addAction(actionAdd);
        menuMotifExplorer.addSeparator();
        menuMotifExplorer.addAction(actionSave);
        menuMotifExplorer.addAction(actionSaveAll);
        menuMotifExplorer.addAction(actionExport_PDF);
        menuMotifExplorer.addSeparator();
        menuMotifExplorer.addAction(actionClose);
        menuMotifExplorer.addAction(actionQuit);
        
        menuEdit.addAction(actionUndo);
        menuEdit.addAction(actionRedo);
        menuEdit.addSeparator();
        menuEdit.addAction(actionShift_left);
        menuEdit.addAction(actionShift_right);
        menuEdit.addSeparator();
        menuEdit.addAction(actionReverse_complement);
        menuEdit.addSeparator();
        menuEdit.addAction(actionExtractMotifs);
        
        menuAnnotate.addAction(actionAddAnnotations);
        menuAnnotate.addAction(actionRemoveScoredRegions);
        menuAnnotate.addSeparator();
        menuAnnotate.addAction(actionRenameAnnotations);
        menuAnnotate.addAction(actionExpandAnnotationSelection);
        menuAnnotate.addAction(actionSelectAllAnnotations);
        menuAnnotate.addAction(actionSelectNoneAnnotations);
        menuAnnotate.addSeparator();
        menuAnnotate.addAction(actionExtractMotifsFromAnnotations);
        menuAnnotate.addAction(actionMLEMetaMotifsFromAnnotations);
        
        menuBestMetaMotifHits = new QMenu("menuBestMetaMotifHits");
        menuBestHits = new QMenu("menuBestHits");
        menuBestReciprocalHits = new QMenu("menuBestReciprocalHits");
        
        menuAnalysis.addMenu(menuBestHits);
        menuAnalysis.addMenu(menuBestReciprocalHits);
        menuAnalysis.addMenu(menuBestMetaMotifHits);
        menuAnalysis.addSeparator();
        menuAnalysis.addAction(actionMLEMetaMotif);
		setWindowIcon(new QIcon("classpath:icon.png"));
        setWindowTitle("mXplor - " + getName());
        retranslateUi(this);
        
        this.connectSlotsByName();
        
        
    } // setupUi
    
	protected void closeEvent(QCloseEvent e) {
		MXplor.allMotifSetWindows.remove(this);
		
		//FIXME: If you include this there's a crash upon closing the last window...
		if (this.allMotifSetWindows.size() > 0)
			for (MXplor mxplor : this.allMotifSetWindows)
				mxplor.menusNeedUpdating();
		
		/*
		if (nmicaTask != null) {
			synchronized (nmicaTask) {
	            abort = true;
	            nmicaTask.notify();
	        }
		}*/
        super.closeEvent(e);
	}
	
	public void updateWidget() {
		this.update();
	}
	
	//FIXME: Disable this action item in the menu if there are no logos to export
	public void on_actionExport_PDF_triggered() {
		if (motifSetView.logoWidgets() == 0) return;
		
		String filename = QFileDialog
							.getSaveFileName(
									this, 
									"Export to PDF",
									"", //directory
									new QFileDialog.Filter(".pdf"));
		
		if (filename == null) return;
		
		QPrinter printer = new QPrinter();
        printer.setOutputFormat(QPrinter.OutputFormat.PdfFormat);
        printer.setOutputFileName(filename);
        QPainter pdfPainter = new QPainter(printer);
        
        int logoCount = motifSetView.logoWidgets();
        double vPadding = 20;
        LogoView firstLogo = motifSetView
								.getLabelledLogoWidget(0)
								.getLogo();
        
        for (int i = 0; i < logoCount; i++) {
            QRectF logoTargetRect = new QRectF(120,
            		i * firstLogo.scene().sceneRect().height() + (i * vPadding),
					firstLogo.scene().sceneRect().width(),
					firstLogo.scene().sceneRect().height());
            QRectF nameTargetRect = new QRectF(0,
            		i * firstLogo.scene().sceneRect().height() + 
            		firstLogo.scene().sceneRect().height() / 2.0 + 
            		(i * vPadding),
					firstLogo.scene().sceneRect().width(),
					firstLogo.scene().sceneRect().height());
        	LabelledLogoView widg = motifSetView.getLabelledLogoWidget(i);
        	widg.getLogo().getBorderItem().setVisible(false);
        	QGraphicsScene scene = widg.getLogo().scene();
        	scene.render(pdfPainter, logoTargetRect, scene.sceneRect());
        	//TODO: wasteful, don't replace the scene each time
        	//just remove the old text item and make a new one
        	QGraphicsScene labelScene = new QGraphicsScene();
            labelScene.addText(widg.getLogo().getMotif().getNmicaMotif().getName());
            labelScene.render(pdfPainter, nameTargetRect, scene.sceneRect());
        }
        
        for (int i = 0; i < logoCount; i++) {
            LabelledLogoView widg = motifSetView.getLabelledLogoWidget(i);
        	widg.getLogo().getBorderItem().setVisible(true);
        }
        
        //scene.render(pdfPainter);
        pdfPainter.end();
	}
	
    void retranslateUi(QMainWindow UiMainWindow) {
        actionNew.setText("New");
        actionSave.setText("Save..");
        actionSaveAll.setText("Save all..");
        actionExport_PDF.setText("Export PDF..");
        actionOpen.setText("Open..");
        actionAdd.setText("Add..");
        actionClose.setText("Close");
        actionQuit.setText("Quit");
        
        actionUndo.setText("Undo");
        actionRedo.setText("Redo");
        actionShift_left.setText("Shift left");
        actionShift_right.setText("Shift right");
        actionReverse_complement.setText("Reverse complement");
        actionExtractMotifs.setText("Selections to motifs");
        
        actionRemoveScoredRegions.setText("Remove annotation");
        actionAddAnnotations.setText("Add annotation");
        actionExpandAnnotationSelection.setText("Expand selection");
        actionExtractMotifsFromAnnotations.setText("Annotations to motifs");
        actionSelectAllAnnotations.setText("Select all");
        actionSelectNoneAnnotations.setText("Select none");
        actionMLEMetaMotifsFromAnnotations.setText("MLE metamotif");
        actionRenameAnnotations.setText("Rename");
        
        menuBestHits.setTitle("Best hits");
        menuBestReciprocalHits.setTitle("Best reciprocal hits");
        menuBestMetaMotifHits.setTitle("Best metamotif hits");
        actionMLEMetaMotif.setText("MLE metamotif");
        actionBestMetaMotifHits.setText("Best metamotif hits");
        menuMotifExplorer.setTitle("File");
        menuEdit.setTitle("Edit");
        menuAnnotate.setTitle("Annotate");
        menuAnalysis.setTitle("Analysis");
    }

    public void on_actionOpen_triggered() throws Exception {
    	openFile();
    }
    
    public void on_menuEdit_aboutToShow() {
    	actionUndo.setEnabled(motifSetView.getUndoStack().canUndo());
    	actionRedo.setEnabled(motifSetView.getUndoStack().canRedo());
    }
    
    
    public void on_menuAnalysis_aboutToShow() {
    	if (menusNeedUpdating) {
    		for (QAction qa : menuBestHits.actions())
    			menuBestHits.removeAction(qa);
    		
    		for (MXplor w : allMotifSetWindows) {
    			if (w == this) continue;
				menuBestHits.addAction(w.getName(), new MotifComparisonSignalReceiver(this,w), "trigger()");
    		}
    		menusNeedUpdating = false;
    	}
    }
    
    private String getName() {
		return this.motifSetView.getName();
	}
    
    public void on_actionAdd_triggered() {
    	try {
			addMotifsFromFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public void on_actionUndo_triggered() {
    	if (motifSetView.getUndoStack().canUndo())
    		motifSetView.getUndoStack().undo();
    }
    
    public void on_actionRedo_triggered() {
    	if (motifSetView.getUndoStack().canRedo())
    		motifSetView.getUndoStack().redo();    	
    }
    
    public void addMotifsFromFile() throws Exception {
    	HashMap<String, List<QMotif>> motifs = this.openXMSFile("");
    	if ((motifs == null) || (motifs.values().size() == 0)) this.close();
    	
    	for (String key : motifs.keySet())
    		addMotifs(motifs.get(key));
    }
    
    
    public void on_actionMLEMetaMotif_triggered() throws BioException {
    	List<QMotif> qmotifs  = motifSetView.getMotifs();
    	Motif[] motifs = new Motif[qmotifs.size()];
    	MetaMotif mm = null;
    	for (int i = 0; i < qmotifs.size(); i++)
    		motifs[i] = qmotifs.get(i).getNmicaMotif();
    	
    	//try {
    		System.out.println("Calculating maximum likelihood estimate for the motifs...");
    		mm = DirichletParamEstimator.mle(motifs);
    	//} catch(Exception e) {
    	//	QErrorMessage msg = new QErrorMessage();
    	//	msg.setWindowIcon(new QIcon("classpath:tango/dialog-error.png"));
    	//	msg.setWindowTitle("Error!");
    	//	msg.showMessage(e.getMessage());
    	//	return;
    	//}
    	
    	List<QMotif> qms = QMotif.create(Arrays.asList(new Motif[]{MetaMotifIOTools.metaMotifToAnnotatedMotif(mm)}));
    	MXplor mxplorWindow = new MXplor("MLE metamotif", qms);
    	mxplorWindow.show();
    	mxplorWindow.update();
    	mxplorWindow.repaint();
    	mxplorWindow.resizeWindowAndScrollArea();
    }
    
    /*
    public void on_actionBestMetaMotifHits_triggered() {
    	KeyValuePair<String,List<QMotif>> motifs = this.openXMSFile("");
    	for (QMotif mm : motifs.getValue()) {
    		for (QMotif m : this.motifSetWidget.getMotifs()) {
    			m.getMetaMotif().bestHit(m.getNmicaMotif());    			
    		}
    	}
    }
    */
    
    
    public void on_actionExtractMotifs_triggered() throws IllegalSymbolException, IllegalAlphabetException {
    	System.out.println("Extracting motifs...");
    	Motif[] motifs = motifSetView.allSelectedColumnsAsMotifs();
    	MXplor mxplorWindow = new MXplor("Extracted motifs",QMotif.motifsToQMotifs(motifs),
				LogoView.DEFAULT_MAX_COLS, LogoView.DEFAULT_X_OFFSET, args,
				true);
    	System.out.println("Motifs constructed:" + motifs.length);
    	menusNeedUpdating();
    	mxplorWindow.show();
    	mxplorWindow.update();
    	mxplorWindow.repaint();
    	mxplorWindow.resizeWindowAndScrollArea();
    }
    
    public void on_actionSelectNoneAnnotations_triggered() {
    	deselectAllAnnotations();
    }
    
    
    public void on_actionSelectAllAnnotations_triggered() {
    	selectAllAnnotations();
    }
    
    public void on_actionExpandAnnotationSelection_triggered() {
    	selectAllInTheAnnotationSets();
    }
    
    public void on_actionExtractMotifsFromAnnotations_triggered() throws Exception {
    	System.out.println("Extracting motifs from annotations...");

    	Motif[] motifs = motifSetView.selectedAnnotationsAsMotifs();
    	MotifIOTools.writeMotifSetXML(new FileOutputStream(new File("/tmp/foo.xms")),motifs);
    	
    	MXplor mxplorWindow = new MXplor("Extracted motifs",
    			QMotif.motifsToQMotifs(motifs),
				LogoView.DEFAULT_MAX_COLS, LogoView.DEFAULT_X_OFFSET, args,
				true);
    	menusNeedUpdating();
    	mxplorWindow.show();
    	mxplorWindow.update();
    	mxplorWindow.repaint();
    	mxplorWindow.resizeWindowAndScrollArea();
    }
    
    public void on_actionMLEMetaMotifsFromAnnotations_triggered() throws Exception {
    	List<QMotif> qmotifs  = motifSetView.getMotifs();
    	Motif[] motifs = new Motif[qmotifs.size()];
    	MetaMotif mm = DirichletParamEstimator.mle(
    						motifSetView.selectedAnnotationsAsMotifs());
    	
    	List<QMotif> qms = QMotif.create(
    						Arrays.asList(
    							new Motif[]{
									MetaMotifIOTools.metaMotifToAnnotatedMotif(mm)}));
    	MXplor mxplorWindow = new MXplor("MLE metamotif", qms);
    	mxplorWindow.show();
    	mxplorWindow.update();
    	mxplorWindow.repaint();
    	mxplorWindow.resizeWindowAndScrollArea();
    }
    
    public void on_actionRemoveScoredRegions_triggered() {
    	SelectableMotifRegion[] selRegs = motifSetView.selectedMotifRegions();
    	//motifSetWidget.get
    	motifSetView.removeMotifRegions(selRegs);
    	repaint();
    }
    
    public void on_actionAddAnnotations_triggered() {
    	String name = QInputDialog.getText(this, "Annotation", "Name");
    	if (name == null) return;
    	deselectAllAnnotations();
    	
    	MotifRegionSet mregSet = new MotifRegionSet();
    	mregSet.setColor(MotifSetView.annotationColors().colorFor(mregSet));
    	
    	//int maxAnnotId = MotifRegion.maxAnnotationSetId();
    	
    	for (QMotif m : motifSetView.getMotifs()) {
    		int prevI = -2; //so there's a difference of more than 1 to begin with
    		List<Set<Integer>> selectedSets = new ArrayList<Set<Integer>>();
    		Set<Integer> currentSet = null;
    		for (int i : m.getSelectedColumnIndices()) {
    			if ((i - prevI) > 1) {
    				if (currentSet != null && currentSet.size() > 0)  {
    						selectedSets.add(currentSet);
    						currentSet = new TreeSet<Integer>();
    					}
    				else currentSet = new TreeSet<Integer>();
    			}
    			if (i >= 0) currentSet.add(i);
    			prevI = i;
    		}
    		if (!selectedSets.contains(currentSet))
    			selectedSets.add(currentSet);

        	for (Set<Integer> set : selectedSets) {
        		if (set == null || set.size() == 0) continue;
        		Integer[] ints = set.toArray(new Integer[set.size()]);
        		
        		// FIXME: addMotifRegion should handle the construction. This way you're 
        		// making it possible to make the qmotif reference 
        		// in MotifRegion point to a different qmotif
        		SelectableMotifRegion motifReg = new SelectableMotifRegion(
        												mregSet, 
        												m, 
        												ints[0], 
        												ints.length);
        		//motifReg.setAnnotationSetId(maxAnnotId + 1);
        		motifReg.setSelectedRegion(true);
        		motifReg.setColor(mregSet.color());
        		motifReg.setKept(true);
        		
        		if (name != null) motifReg.setName(name);
        		m.addRegion(motifReg);
        	}
    	}
    	
    	//then clear the selection
    	for (QMotif m : motifSetView.getMotifs()) {
    		for (int i : m.getSelectedColumnIndices())
    			m.getDists().get(i).toggleSelected();
    	}
    	
    	this.repaint();
    }
    
    public void on_actionRenameAnnotations_triggered() {
    	String newName = QInputDialog.getText(this, "Rename annotation","Name");
    	if (newName == null) return;
    	
    	MotifRegionSet regSet = null;
    	boolean firstDone = false;
    	for (QMotif m : motifSetView.getMotifs())
    		for (int i = 0; i < m.regions(); i++){
    			MotifRegion reg = m.getRegion(i);
    			if (reg instanceof SelectableMotifRegion) {
    				SelectableMotifRegion sreg = (SelectableMotifRegion) reg;
    				if (!firstDone) {
    					firstDone = true;
    					regSet = sreg.getAnnotationSet();
    				}
    				if (sreg.isSelectedRegion()) {
    					sreg.setName(newName);
    					sreg.setAnnotationSet(regSet);
    				}
    			}
    		}
    	
    	repaint();
    }
    
    //TODO: Indexing according to annotation set would make this more efficient
    public void selectAllInTheAnnotationSets() {
    	List<MotifRegionSet> sets = new ArrayList<MotifRegionSet>();
    	
    	//get all the selected set ids
    	for (QMotif m : motifSetView.getMotifs()) {
    		for (int i = 0; i < m.regions(); i++) {
    			MotifRegion reg = m.getRegion(i);
    			if (reg.isSelected() &! sets.contains(reg.getAnnotationSet()))
    				sets.add(reg.getAnnotationSet());
    		}
    	}
    	
    	//add all the set members to the selection
    	for (MotifRegionSet set : sets)
    		for (MotifRegion reg : set.getRegions())
        		reg.setSelected(true);
    }
    
    public void unselectAllInTheAnnotationSets() {
    	Set<MotifRegionSet> sets = new TreeSet<MotifRegionSet>();
    	
    	for (QMotif m : motifSetView.getMotifs()) {
    		for (int i = 0; i < m.regions(); i++) {
    			MotifRegion reg = m.getRegion(i);
    			
    			if (reg instanceof SelectableMotifRegion && 
    					!((SelectableMotifRegion)reg).isSelectedRegion() 
    					&! sets.contains(reg.getAnnotationSet()))
    				sets.add(reg.getAnnotationSet());
    		}
    	}

    	for (MotifRegionSet set : sets)
    		for (MotifRegion reg : set.getRegions())
	    		if (reg instanceof SelectableMotifRegion &&
    				reg.getAnnotationSet() == set)
    					((SelectableMotifRegion)reg).setSelectedRegion(false);
    }
    
    public void selectAllAnnotations() {
    	for (QMotif m : motifSetView.getMotifs())
    		for (int i = 0; i < m.regions(); i++) {
    			if (m.getRegion(i) instanceof SelectableMotifRegion)
    				((SelectableMotifRegion)m.getRegion(i)).setSelectedRegion(true);
    		}
    }
    
    public void deselectAllAnnotations() {
    	for (QMotif m : motifSetView.getMotifs())
    		for (int i = 0; i < m.regions(); i++)
    			if (m.getRegion(i) instanceof SelectableMotifRegion)
    				((SelectableMotifRegion)m.getRegion(i)).setSelectedRegion(false);
    }
    
    public void on_actionSave_triggered() throws Exception {
    	String filename = QFileDialog
    							.getSaveFileName(
    									this, 
    									"Save motif sets",
    									"", //directory
    									new QFileDialog.Filter(".xms"));
    	
    	if (filename != null)
    		exportMotifSetToXMS(
    				new BufferedOutputStream(
    						new FileOutputStream(
    							new File(filename))));
    }
    
    public void on_actionSaveAll_triggered() throws Exception {
    	if (this.savedFileName == null) {
    		savedFileName = QFileDialog
    							.getSaveFileName(
    									this, 
    									"Save motif sets",
    									"", //directory
    									new QFileDialog.Filter(".xms"));
    	} 
    	
    	if (savedFileName != null)
    		exportSessionToXMS(
				new BufferedOutputStream(
						new FileOutputStream(
							new File(savedFileName))));
    }
    
    public void on_actionClose_triggered() {
    	allMotifSetWindows.remove(this);
    	menusNeedUpdating();
    	
    	//if (allMotifSetWindows.size() == 0)
    	//	System.exit(0);
    }
    
    public void on_actionReverse_complement_triggered() {
    	motifSetView.getUndoStack().push(new ReverseComplementCommand(motifSetView.getSelectedMotifs(), null));

    }
    
    public void on_actionShift_left_triggered() {
    	motifSetView.getUndoStack().push(new ShiftCommand(this.motifSetView.getSelectedMotifs(),-1,null));
    }
    
    public void on_actionShift_right_triggered() {
    	motifSetView.getUndoStack().push(new ShiftCommand(this.motifSetView.getSelectedMotifs(),1,null));    	
    }
    
    public void on_actionBest_hits_triggered() {
    	
    }
    
    public  void on_actionBest_reciprocal_hits_triggered() {
    	
    }
    
    public void openFile() throws Exception {
    	if ((motifSetView.getMotifs() == null) || 
    		(motifSetView.getMotifs().size() == 0))
    		this.close();

    	HashMap<String, List<QMotif>> motifs = this.openXMSFile("");
    	for (String s : motifs.keySet())
    		newMXplorWindow(s, motifs.get(s));
    }

	private void newMXplorWindow(String s, List<QMotif> motifs) {
		MXplor mxplorWindow;
		mxplorWindow = new MXplor(s, motifs);
    	for (QMotif m : mxplorWindow.motifSetView.getMotifs())
    		for (int i = 0; i < m.regions(); i++)
    			m.getRegion(i).updateLocation();
    	
    	menusNeedUpdating();
    	//TODO: get rid of repaint/resize?
    	mxplorWindow.update();
    	mxplorWindow.repaint();
    	mxplorWindow.resizeWindowAndScrollArea();
	}

	private void menusNeedUpdating() {
		for (MXplor w : allMotifSetWindows) w.menusNeedUpdating = true;
	}

    //TODO: Make public and derive ResVal from it
    private class KeyValuePair<K,V> {
    	K k;
    	V v;
    	private KeyValuePair(K k, V v) {
    		this.k = k;
    		this.v = v;
    	}
    	
    	public K getKey() {
    		return k;
    	}
    	
    	public V getValue() {
    		return v;
    	}
    }
    
    public HashMap<String, List<QMotif>> openXMSFile(String fileName) throws Exception {
    	//List<Motif> motifList = null;
    	HashMap<String, List<QMotif>> qms = null;
    	
        if (fileName == null || fileName.equals(""))
            fileName = QFileDialog
                    .getOpenFileName(this, tr("Open File"), "", 
                    				 new QFileDialog.Filter("Motif set XML files (*.xms)"));

        if (!(fileName == null) || fileName.equals("")) {
        	HashMap<String, List<Motif>> motifSets = importXMS(fileName);
        	HashMap<String,List<QMotif>> kps = new HashMap<String,List<QMotif>>();
    		for (String s : motifSets.keySet())
    			kps.put(s, QMotif.create(motifSets.get(s)));
    		
    		return kps;
    		
        } else return null;
        
        /*
        if (!(fileName == null) || fileName.equals("")) {
            try {
            	Motif[] motifs = MotifIOTools.loadMotifSetXML(new FileInputStream(fileName));
            	if (pseudoCounts > 0 && MotifTools.containsWeightsBelowValue(motifs, 0.0000001))
	            	for (Motif m : motifs)
	            		MotifTools.addPseudoCounts(m, pseudoCounts);
            	
            	motifList = java.util.Arrays.asList(motifs);
            	//loadMotifs(motifList, true);
    		} catch (FileNotFoundException e) {
    			e.printStackTrace();
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
        }
        if (motifList != null)
        	return new KeyValuePair<String, List<QMotif>>
        					(fileName,QMotif.create(motifList));
        else 
        	return null;
        	*/
    }
    
    public boolean eventFilter(QObject obj, QEvent event) {
    	if (event instanceof QFileOpenEvent) {
    		QFileOpenEvent foe = (QFileOpenEvent)event;
    		//System.out.println("File opened!" + foe.file());
    		HashMap<String, List<QMotif>> motifs = null;
			try {
				motifs = openXMSFile(foe.file());
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (motifs != null)
				for (String s : motifs.keySet()) newMXplorWindow(s, motifs.get(s));
    		return true;
    	} else return false;
    }
    
    public static void main(String args[]) throws Exception {
		QApplication.initialize(args);
		QApplication app = QApplication.instance();
		
		System.out.println("Starting...");
		Motif[] motifs = null;
		String filename = null;
		
		//FIXME: Make this use CliTools or Apache Commons CLI
		if (args.length == 0) 
			filename = "/Users/mz2/workspace/NestedMICA/metamotifs/sim/34567.xms"; //for testing
		else if (args.length == 1) {
			System.out.println("Opening " + args[0] + " ...");
			filename = args[0];
		}
		else if (args.length > 1) {
			System.err.println("Please supply the name of the XMS file to be opened as an argument");
			QApplication.quit();
			System.exit(1);
		}
		
		/*
		try {
			motifs = MotifIOTools.loadMotifSetXML(
					new FileInputStream(filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		
		//HashMap<String, List<QMotif>> motifs
		
		//for (String s : motifs.keySet())
    	//	newMXplorWindow(s, motifs.get(s));
		
		HashMap<String, List<Motif>> motifSets = importXMS(filename);
		
		for (String s : motifSets.keySet()) {
			//System.out.println("s:" + s);
			MXplor win = new MXplor(filename,
					QMotif.create(motifSets.get(s)), 
					LogoView.DEFAULT_MAX_COLS, 
					LogoView.DEFAULT_X_OFFSET, args, true);
			win.show();
		}

		/*MXplor window = new MXplor(filename,QMotif.create(Arrays.asList(motifs)), 
													LogoView.DEFAULT_MAX_COLS, 
													LogoView.DEFAULT_X_OFFSET, args, true);
		*/
		
		if (MXplor.allMotifSetWindows.size() > 0)
			app.installEventFilter(MXplor.allMotifSetWindows.get(0));
		else {
			System.err.println("No motifs were imported from the input XMS file.");
			QApplication.quit();
			System.exit(1);
		}
		
		QApplication.exec();
	}

    private List<QMotif> currentMotifs;
	private boolean firstTime = true;
	private boolean useNmica = false;
	
	/*public void updateShownMotif(Motif motif, Integer i) {
		this.motifSetWidget.loadMotif(new QMotif(motif), !firstTime, i);
	}*/
    
	public void addMotifs(List<QMotif> motifs) {
		motifSetView.addMotifs(motifs);
		firstTime = false;
    	this.resizeWindowAndScrollArea();
	}

	
	private void resizeWindowAndScrollArea() {
		if (motifSetView.getMotifs() != null) {
			//System.out.println("Will resize (non-null motifs)");
			resize(LabelledLogoView.DEFAULT_TOTAL_WIDGET_WIDTH,
					(int)Math.round(DEFAULT_MIN_COL_HEIGHT * NUM_MOTIFS_SHOWN_AT_A_TIME * 1.0));
			scrollArea.resize(LabelledLogoView.DEFAULT_TOTAL_WIDGET_WIDTH, Math.min(DEFAULT_MIN_COL_HEIGHT * 3,this.width()));
		}
		else {
			//System.out.println("Will resize (null motifs)");
			resize(DEFAULT_MIN_WINDOW_WIDTH,DEFAULT_MIN_COL_HEIGHT * 4);
			scrollArea.resize(DEFAULT_MIN_WINDOW_WIDTH,DEFAULT_MIN_COL_HEIGHT);
			
		}
		updateWidget();
	}
	
	public QSize sizeHint() {
		return new QSize(LabelledLogoView.DEFAULT_TOTAL_WIDGET_WIDTH, DEFAULT_MIN_COL_HEIGHT * NUM_MOTIFS_SHOWN_AT_A_TIME);
	}
	
	protected void showEvent(QShowEvent showEvent) {
		if (useNmica) {
			if (!nmicaStarted) {
				if (startNMICA) nmicaTask = new NestedMICATask(this, args);
				nmicaTask.setDaemon(true);
				nmicaTask.startTraining();
				
			}
		}
	}

	@Deprecated public void updateView(final List<QMotif> list) {
		QApplication.invokeLater(new Runnable() {
			public void run() {
				synchronized (nmicaTask) {
					//System.out.println("Foo!!!");
					if (!abort) {
						update();
						repaint();
					}
				}
			}
		});
	}
	
	public static HashMap<String, List<Motif>> importXMS(String filename) throws Exception  {
		return importXMS(filename, DEFAULT_PSEUDOCOUNT);
	}
	
	public static HashMap<String, List<Motif>> importXMS(String filename, double pseudoCounts) throws Exception {
		HashMap<String,List<Motif>> sessionWindowMotifSets = new HashMap<String,List<Motif>>();
		
		Motif[] motifs = MotifIOTools.loadMotifSetXML(new BufferedInputStream(new FileInputStream(filename)));
		if (pseudoCounts > 0 && MotifTools.containsWeightsBelowValue(motifs, DEFAULT_PSEUDOCOUNT))
        	for (Motif m : motifs)
        		MotifTools.addPseudoCounts(m, pseudoCounts);
    	
		Set<String> setIds = new TreeSet<String>();
		for (Motif m : motifs)
			if (m.getAnnotation().containsProperty(SET_NAME_PROPERTY))
				setIds.add((String)m.getAnnotation().getProperty(SET_NAME_PROPERTY));
		
		for (String s : setIds)
			sessionWindowMotifSets.put(s, new ArrayList<Motif>());
		
		if (setIds.size() == 0) {
			//System.out.println("no set ids defined");
			sessionWindowMotifSets.put(filename, new ArrayList<Motif>());
		}
		
		if (sessionWindowMotifSets.size() == 1) {
			if (setIds.size() == 0)
				for (Motif m : motifs)
					sessionWindowMotifSets.get(filename).add(m);
			else
				for (Motif m : motifs)
					sessionWindowMotifSets.get(m.getAnnotation().getProperty(SET_NAME_PROPERTY)).add(m);
		} else {
			for (Motif m : motifs)
				sessionWindowMotifSets.get(m.getAnnotation().getProperty(SET_NAME_PROPERTY)).add(m);
		}
		
		return sessionWindowMotifSets;
	}
	
	public void exportSetToXMS(OutputStream os) throws Exception {
		List<Motif> motifs = QMotif.qmotifsToMotifs(this.motifSetView.getMotifs());
		MotifIOTools.writeMotifSetXML(os, motifs.toArray(new Motif[motifs.size()]));
	}
	
	public void exportMotifSetToXMS(OutputStream os) throws Exception {
		List<QMotif> qms = motifSetView.getMotifs();
		List<Motif> motifs = QMotif.qmotifsToMotifs(qms);
		
		for (Motif m : motifs)
			m.getAnnotation()
				.setProperty(SET_NAME_PROPERTY, motifSetView.getName());
		
		MotifIOTools.writeMotifSetXML(os, motifs.toArray(new Motif[motifs.size()]));
	}
	
	public void exportSessionToXMS(OutputStream os) throws Exception {
		List<Motif> allMotifs = new ArrayList<Motif>();
		for (int i = 0; i < allMotifSetWindows.size(); i++) {
			List<QMotif> qms = allMotifSetWindows.get(i).motifSetView.getMotifs();
			List<Motif> motifs = QMotif.qmotifsToMotifs(qms);
			for (Motif m : motifs) {
				m.getAnnotation()
					.setProperty(SET_NAME_PROPERTY, 
								allMotifSetWindows.get(i).motifSetView.getName());
			}
			allMotifs.addAll(motifs);
		}
		MotifIOTools.writeMotifSetXML(os, allMotifs.toArray(new Motif[allMotifs.size()]));
	}
	
	public class MotifComparisonSignalReceiver {
		private MXplor mxplor0, mxplor1;
		
		public MotifComparisonSignalReceiver(MXplor xplor0, MXplor xplor1) {
			this.mxplor0 = xplor0;
			this.mxplor1 = xplor1;
		}
		public void trigger() {
			mxplor0.comparisonAgainstMotifset(mxplor1);
		}
	}

	public void comparisonAgainstMotifset(MXplor mxplor1) {
		
		//FIXME: Threshold should have a different value
		double threshold = QInputDialog.getDouble(this,
                "Score threshold",
                "value",
                -50,
               -100000000,
                100000000,
                5,
                new Qt.WindowFlags(Qt.WindowType.Popup));
        
		//FIXME: Give an error message
		if (Double.isNaN(threshold) || Double.isInfinite(threshold)) return;
		deselectAllAnnotations();
		
		for (QMotif qm0 : this.motifSetView.getMotifs()) {
			if (!qm0.isMetaMotif()) continue; //FIXME: Support motif--motif hits
			
			MotifRegionSet mregSet = new MotifRegionSet();
			mregSet.setColor(qm0.color());
			
			for (QMotif qm1 : mxplor1.motifSetView.getMotifs())
				bestHitsWith(mxplor1, threshold, qm0, mregSet, qm1);
			
		}		
	}

	private void bestHitsWith(
			MXplor mxplor1, 
			double threshold, 
			QMotif qm0,
			MotifRegionSet mregSet, 
			QMotif qm1) throws BioError {
		double[] hitLogProbs = null;
		try {
			hitLogProbs = qm0
							.getMetaMotif()
								.logProbDensities(
									qm1.getNmicaMotif()
										.getWeightMatrix());
		} catch (IllegalSymbolException e) {
			throw new BioError(e);
		} catch (IllegalAlphabetException e) {
			throw new BioError(e);
		}
		int maxIndex = 0;
		
		if (hitLogProbs != null) {
			double maxValue = Double.NEGATIVE_INFINITY;
			for (int i = 1; i < hitLogProbs.length; i++)
				if (!Double.isNaN(hitLogProbs[i]) &! Double.isInfinite(hitLogProbs[i]) && maxValue < hitLogProbs[i])
					maxValue = hitLogProbs[maxIndex = i];
			
			if (!Double.isInfinite(maxValue) && maxValue > threshold) {
				System.out.println("Max hit: " + maxValue + " (i:" + maxIndex + ")");
				int metaMotifLength = qm0.getMetaMotif().columns();
				System.out.println("Metamotif length:" + metaMotifLength + " " + qm1.getNmicaMotif().getWeightMatrix().columns());
				ScoredMotifRegion sar = new ScoredMotifRegion(mregSet,
						qm1,
						maxIndex,
						metaMotifLength,
						NativeMath.exp2(maxValue));
				sar.setName(qm0.getNmicaMotif().getName());
				sar.setSelectedRegion(true);
				sar.setColor(mregSet.color());
				//sar.setBrushes(qm0.color());
				qm1.addRegion(sar);
				
				updateWidget();
				mxplor1.updateWidget();
			} else {
				System.out.println("No significant hits found");
			}
		} else {
			
		}
	}
}
