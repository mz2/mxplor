package uk.ac.sanger.motifxplorer.app;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import uk.ac.sanger.motifxplorer.ui.UiMainWindow;
import uk.ac.sanger.motifxplorer.ui.graphics.ScoredAnnotatedRegion;
import uk.ac.sanger.motifxplorer.ui.model.QMotif;
import uk.ac.sanger.motifxplorer.ui.widget.LabelledLogoWidget;
import uk.ac.sanger.motifxplorer.ui.widget.LogoView;
import uk.ac.sanger.motifxplorer.ui.widget.MotifSetView;

import com.trolltech.qt.core.QEvent;
import com.trolltech.qt.core.QRect;
import com.trolltech.qt.core.QSize;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QCloseEvent;
import com.trolltech.qt.gui.QFileDialog;
import com.trolltech.qt.gui.QFileOpenEvent;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QMainWindow;
import com.trolltech.qt.gui.QMenu;
import com.trolltech.qt.gui.QMenuBar;
import com.trolltech.qt.gui.QScrollArea;
import com.trolltech.qt.gui.QShowEvent;
import com.trolltech.qt.gui.QSizePolicy;
import com.trolltech.qt.gui.QStatusBar;
import com.trolltech.qt.gui.QTableView;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

public class MXplor extends QMainWindow {
    private static final int NUM_MOTIFS_SHOWN_AT_A_TIME = 6;
	private UiMainWindow ui = new UiMainWindow();
    //private MotifTableModel motifTableModel = new MotifTableModel(this);
    //private List<QMotif> motifs;
    //private String fileName;
    
    public QAction actionNew;
    public QAction actionSave;
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
    
    
	private MotifSetView motifSetWidget;
	private QScrollArea scrollArea;
	private boolean startNMICA;
	private String[] args;
	private QAction actionAdd;
	private QAction actionExtractMotifs;
	private QAction actionUndo;
	private QAction actionRedo;
	private String savedFileName;
	private QAction actionBestMetaMotifHits;
	
	//public static final int DEFAULT_MIN_SCROLL_AREA_HEIGHT = 600;
	//public static final int DEFAULT_MAX_SCROLL_AREA_HEIGHT = 600;
	
	public static final int DEFAULT_MIN_WINDOW_WIDTH = LabelledLogoWidget.DEFAULT_TOTAL_WIDGET_WIDTH - 100;
	public static final int DEFAULT_MAX_WINDOW_WIDTH = LabelledLogoWidget.DEFAULT_TOTAL_WIDGET_WIDTH;
	public static final int DEFAULT_MIN_WINDOW_HEIGHT = 400;
	public static final int DEFAULT_MAX_WINDOW_HEIGHT = 600;
	public static final int DEFAULT_MIN_COL_HEIGHT = LogoView.MOTIF_HEIGHT ;

	private boolean menusNeedUpdating;
	private String name;
	
	//TODO: Make configurable and sync with the default value from MetaMotifFinder
	private double pseudoCounts = 0.001;
	
	public MXplor(String name, List<QMotif> motifs) {
		this(name, motifs, MotifSetView.DEFAULT_MAX_COLS, MotifSetView.DEFAULT_X_OFFSET);
	}
	
	public MXplor(String name, List<QMotif> motifs, int maxCols, int xOffset) {
		this(name, motifs, maxCols, xOffset, null, false);
	}
	
	public MXplor(String name, List<QMotif> motifs, int maxCols, int xOffset, String[] args, boolean startNMICA) {
		this.name = name;
		setupUi(name, motifs, maxCols, xOffset);
		this.startNMICA = startNMICA;
		this.args = args;
		
    	if (!allMotifSetWindows.contains(this))
    		MXplor.allMotifSetWindows.add(this);
    	
    	show();
		resizeWindowAndScrollArea();
	}
	
	public boolean event(QEvent event) {
		if (event instanceof QFileOpenEvent) {
			QFileOpenEvent ev = (QFileOpenEvent) event;
			System.out.print(ev.file());
			return true;
		}
		else return super.event(event);
	}
	
	private void setupUi(String name, List<QMotif> motifs, int maxCols,int xOffset) {
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
		motifSetWidget = new MotifSetView(scrollArea, motifs, maxCols, xOffset, infoContentScale);
		motifSetWidget.setMinimumWidth(DEFAULT_MIN_WINDOW_WIDTH);
		scrollArea.setWidget(motifSetWidget);
		
		scrollArea.setHorizontalScrollBarPolicy(Qt.ScrollBarPolicy.ScrollBarAlwaysOff);
		setCentralWidget(scrollArea);
		scrollArea.setParent(this);
        actionNew = new QAction(this);
        actionNew.setObjectName("actionNew");
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
        
        actionShift_left = new QAction(this);
        actionShift_left.setObjectName("actionShift_left");
        actionShift_right = new QAction(this);
        actionShift_right.setObjectName("actionShift_right");
        actionReverse_complement = new QAction(this);
        actionReverse_complement.setObjectName("actionReverse_complement");
        actionExtractMotifs = new QAction(this);
        actionExtractMotifs.setObjectName("actionExtractMotifs");
        //actionBest_hits = new QAction(this);
        //actionBest_hits.setObjectName("actionBest_hits");
        //actionBest_reciprocal_hits = new QAction(this);
        //actionBest_reciprocal_hits.setObjectName("actionBest_reciprocal_hits");
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
        menuAnalysis = new QMenu(menubar);
        menuAnalysis.setObjectName("menuAnalysis");
        this.setMenuBar(menubar);
        statusbar = new QStatusBar(this);
        statusbar.setObjectName("statusbar");
        this.setStatusBar(statusbar);

        menubar.addAction(menuMotifExplorer.menuAction());
        menubar.addAction(menuEdit.menuAction());
        menubar.addAction(menuAnalysis.menuAction());
        menuMotifExplorer.addAction(actionNew);
        menuMotifExplorer.addAction(actionSave);
        menuMotifExplorer.addAction(actionExport_PDF);
        menuMotifExplorer.addAction(actionOpen);
        menuMotifExplorer.addAction(actionAdd);
        menuMotifExplorer.addAction(actionClose);
        menuMotifExplorer.addAction(actionQuit);
        menuEdit.addAction(actionUndo);
        menuEdit.addAction(actionRedo);
        menuEdit.addAction(actionShift_left);
        menuEdit.addAction(actionShift_right);
        menuEdit.addAction(actionReverse_complement);
        menuEdit.addAction(actionExtractMotifs);
        menuBestMetaMotifHits = new QMenu("menuBestMetaMotifHits");
        menuBestHits = new QMenu("menuBestHits");
        menuBestReciprocalHits = new QMenu("menuBestReciprocalHits");
        menuAnalysis.addMenu(menuBestHits);
        menuAnalysis.addMenu(menuBestReciprocalHits);
        menuAnalysis.addMenu(menuBestMetaMotifHits);
        menuAnalysis.addAction(actionMLEMetaMotif);
		setWindowIcon(new QIcon("classpath:icon.png"));
        setWindowTitle("mXplor - " + name);
        retranslateUi(this);
        
        //QSystemTray sTray = new QSystem
        //actionSave.triggered.connect(actionSave, "trigger()");

        this.connectSlotsByName();
        
        
    } // setupUi
    
	protected void closeEvent(QCloseEvent e) {
		MXplor.allMotifSetWindows.remove(this);
		for (MXplor mxplor : allMotifSetWindows)
			mxplor.menusNeedUpdating = true;
		
		if (nmicaTask != null) {
			synchronized (nmicaTask) {
	            abort = true;
	            nmicaTask.notify();
	        }
		}
        super.closeEvent(e);
		
	}
	
	public void updateWidget() {
		//this.show();
		//this.repaint();
		this.update();
		//motifSetWidget.show();
		//motifSetWidget.repaint();
		//motifSetWidget.update();
		//scrollArea.show();
		//scrollArea.repaint();
		//scrollArea.update();
	}
	
    void retranslateUi(QMainWindow UiMainWindow)
    {
        UiMainWindow.setWindowTitle("mXplor");
        actionNew.setText("New");
        actionSave.setText("Save..");
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
        actionExtractMotifs.setText("Extract motifs");
        menuBestHits.setTitle("Best hits");
        menuBestReciprocalHits.setTitle("Best reciprocal hits");
        menuBestMetaMotifHits.setTitle("Best metamotif hits");
        actionMLEMetaMotif.setText("MLE metamotif");
        actionBestMetaMotifHits.setText("Best metamotif hits");
        menuMotifExplorer.setTitle("File");
        menuEdit.setTitle("Edit");
        menuAnalysis.setTitle("Analysis");
    } // retranslateUi
    
    
    public void on_actionOpen_triggered() {
    	openFile();
    	for (MXplor w : allMotifSetWindows) {
    		w.menusNeedUpdating = true;
    		System.out.println("Menus need updating for " + w.getName());
    	}
    }
    
    public void on_menuEdit_aboutToShow() {
    	actionUndo.setEnabled(motifSetWidget.getUndoStack().canUndo());
    	actionRedo.setEnabled(motifSetWidget.getUndoStack().canRedo());
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
		return this.name;
	}

	public void on_actionBestHits_triggered(Integer i) {
    	
    }
    
    public void on_actionAdd_triggered() {
    	addMotifsFromFile();
    }
    
    public void on_actionUndo_triggered() {
    	if (motifSetWidget.getUndoStack().canUndo())
    		motifSetWidget.getUndoStack().undo();
    }
    
    public void on_actionRedo_triggered() {
    	if (motifSetWidget.getUndoStack().canRedo())
    		motifSetWidget.getUndoStack().redo();    	
    }
    
    public void addMotifsFromFile() {
    	KeyValuePair<String, List<QMotif>> motifs = this.openXMSFile("");
    	if ((motifs == null) || (motifs.getValue().size() == 0)) this.close();
    	
    	addMotifs(motifs.getValue());
    }
    
    public void on_actionMLEMetaMotif_triggered() throws BioException {
    	List<QMotif> qmotifs  = motifSetWidget.getMotifs();
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
    	List<QMotif> qms = QMotif.create(this.motifSetWidget,new Motif[]{MetaMotifIOTools.metaMotifToAnnotatedMotif(mm)});
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
    	Motif[] motifs = motifSetWidget.allSelectedColumnsAsMotifs();
    	MXplor mxplorWindow = new MXplor("Extracted motifs",QMotif.create(motifs),
				LogoView.DEFAULT_MAX_COLS, LogoView.DEFAULT_X_OFFSET, args,
				true);
    	System.out.println("Motifs constructed:" + motifs.length);
    	mxplorWindow.show();
    	mxplorWindow.update();
    	mxplorWindow.repaint();
    	mxplorWindow.resizeWindowAndScrollArea();
    }
    
    public void on_actionSave_triggered() throws Exception {
    	if (this.savedFileName == null) {
    		savedFileName = QFileDialog.getSaveFileName();
    	} exportSessionToXMS(
				new BufferedOutputStream(
						new FileOutputStream(
							new File(savedFileName))));
    }
    
    public void on_actionClose_triggered() {
    	allMotifSetWindows.remove(this);
    	for (MXplor mxplor : allMotifSetWindows)
    		mxplor.menusNeedUpdating = true;
    	
    	if (allMotifSetWindows.size() == 0)
    		System.exit(0);
    }
    
    public void on_actionReverse_complement_triggered() {
    	motifSetWidget.getUndoStack().push(new ReverseComplementCommand(motifSetWidget.getSelectedMotifs(), null));

    }
    
    public void on_actionShift_left_triggered() {
    	motifSetWidget.getUndoStack().push(new ShiftCommand(this.motifSetWidget.getSelectedMotifs(),-1,null));
    }
    
    public void on_actionShift_right_triggered() {
    	motifSetWidget.getUndoStack().push(new ShiftCommand(this.motifSetWidget.getSelectedMotifs(),1,null));    	
    }
    
    public void on_actionBest_hits_triggered() {
    	
    }
    
    public  void on_actionBest_reciprocal_hits_triggered() {
    	
    }
    
    public void openFile() {
    	if ((motifSetWidget.getMotifs() == null) || (motifSetWidget.getMotifs().size() == 0))
    		this.close();

    	MXplor mxplorWindow;

    	KeyValuePair<String, List<QMotif>> motifs = this.openXMSFile("");
    	mxplorWindow = new MXplor(motifs.getKey(), motifs.getValue());
    	System.out.println("Motifs read:" + motifs.getValue().size());
    	//mxplorWindow.addMotifs(motifs.getValue());
    	mxplorWindow.update();
    	mxplorWindow.repaint();
    	mxplorWindow.resizeWindowAndScrollArea();
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
    
    public KeyValuePair<String, List<QMotif>> openXMSFile(String fileName) {
    	List<Motif> motifList = null;
    	
        if (fileName.equals(""))
            fileName = QFileDialog
                    .getOpenFileName(this, tr("Open File"), "", 
                    				 new QFileDialog.Filter("Motif set XML files (*.xms)"));

        if (!fileName.equals("")) {
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
    }
    
    public static void main(String args[]) throws Exception {
		QApplication.initialize(args);
		System.out.println("Starting mXplor...");
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
		
		MXplor window = new MXplor("New",QMotif.create(Arrays.asList(motifs)), 
													LogoView.DEFAULT_MAX_COLS, 
													LogoView.DEFAULT_X_OFFSET, args, true);
		window.show();
		QApplication.exec();
	}

    private List<QMotif> currentMotifs;
	private boolean firstTime = true;
	private boolean useNmica = false;
	
	/*public void updateShownMotif(Motif motif, Integer i) {
		this.motifSetWidget.loadMotif(new QMotif(motif), !firstTime, i);
	}*/
    
	public void addMotifs(List<QMotif> motifs) {
		motifSetWidget.addMotifs(motifs);
		firstTime = false;
    	this.resizeWindowAndScrollArea();
	}

	
	private void resizeWindowAndScrollArea() {
		if (motifSetWidget.getMotifs() != null) {
			System.out.println("Will resize (non-null motifs)");
			resize(LabelledLogoWidget.DEFAULT_TOTAL_WIDGET_WIDTH,
					(int)Math.round(DEFAULT_MIN_COL_HEIGHT * NUM_MOTIFS_SHOWN_AT_A_TIME * 1.0));
			scrollArea.resize(LabelledLogoWidget.DEFAULT_TOTAL_WIDGET_WIDTH, Math.min(DEFAULT_MIN_COL_HEIGHT * 3,this.width()));
		}
		else {
			System.out.println("Will resize (null motifs)");
			resize(DEFAULT_MIN_WINDOW_WIDTH,DEFAULT_MIN_COL_HEIGHT * 4);
			scrollArea.resize(DEFAULT_MIN_WINDOW_WIDTH,DEFAULT_MIN_COL_HEIGHT);
			
		}
		updateWidget();
	}
	
	public QSize sizeHint() {
		return new QSize(LabelledLogoWidget.DEFAULT_TOTAL_WIDGET_WIDTH, DEFAULT_MIN_COL_HEIGHT * NUM_MOTIFS_SHOWN_AT_A_TIME);
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
	
	public void exportSetToXMS(OutputStream os) throws Exception {
		List<Motif> motifs = QMotif.qmotifsToMotifs(this.motifSetWidget.getMotifs());
		MotifIOTools.writeMotifSetXML(os, motifs.toArray(new Motif[motifs.size()]));
	}
	
	public void exportSessionToXMS(OutputStream os) throws Exception {
		for (int i = 0; i < allMotifSetWindows.size(); i++) {
			System.out.println("Saving motifs in window " + 0);
			List<Motif> motifs = QMotif.qmotifsToMotifs(allMotifSetWindows.get(i).motifSetWidget.getMotifs());
			for (Motif m : motifs) {m.getAnnotation().setProperty("setId", "" + i);}
			MotifIOTools.writeMotifSetXML(os, motifs.toArray(new Motif[motifs.size()]));
		}
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
		for (QMotif qm0 : this.motifSetWidget.getMotifs()) {
			if (!qm0.isMetaMotif()) continue;
			for (QMotif qm1 : mxplor1.motifSetWidget.getMotifs()) {
				
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
					double maxValue = hitLogProbs[0];
					for (int i = 1; i < hitLogProbs.length; i++)
						if (maxValue < hitLogProbs[i])
							maxValue = hitLogProbs[maxIndex = i];
					
					System.out.println("Max hit: " + maxValue + " (i:" + maxIndex + ")");
					int metaMotifLength = qm0.getMetaMotif().columns();
					System.out.println("Metamotif length:" + metaMotifLength + " " + qm1.getNmicaMotif().getWeightMatrix().columns());
					ScoredAnnotatedRegion sar = new ScoredAnnotatedRegion(
							qm1,
							maxIndex,
							metaMotifLength,
							NativeMath.exp2(maxValue));
					
					qm1.addAnnotatedRegion(sar);
					
					updateWidget();
					mxplor1.updateWidget();
				} else {
					
				}
			}
		}		
	}
}
