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

import net.derkholm.nmica.model.metamotif.DirichletParamEstimator;
import net.derkholm.nmica.model.metamotif.MetaMotif;
import net.derkholm.nmica.model.metamotif.MetaMotifIOTools;
import net.derkholm.nmica.motif.Motif;
import net.derkholm.nmica.motif.MotifIOTools;

import org.biojava.bio.BioException;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.IllegalSymbolException;

import uk.ac.sanger.motifxplorer.cmd.ReverseComplementCommand;
import uk.ac.sanger.motifxplorer.cmd.ShiftCommand;
import uk.ac.sanger.motifxplorer.nmica.NestedMICATask;
import uk.ac.sanger.motifxplorer.ui.UiMainWindow;
import uk.ac.sanger.motifxplorer.ui.model.QMotif;
import uk.ac.sanger.motifxplorer.ui.widget.LabelledLogoWidget;
import uk.ac.sanger.motifxplorer.ui.widget.LogoView;
import uk.ac.sanger.motifxplorer.ui.widget.MotifSetView;

import com.trolltech.qt.core.QMessageHandler;
import com.trolltech.qt.core.QRect;
import com.trolltech.qt.core.QSize;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QCloseEvent;
import com.trolltech.qt.gui.QErrorMessage;
import com.trolltech.qt.gui.QFileDialog;
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
    public QAction actionBest_hits;
    public QAction actionBest_reciprocal_hits;
    public QAction actionMLEMetaMotif;
    public QWidget centralwidget;
    public QVBoxLayout vboxLayout;
    public QTableView motifTableView;
    public QMenuBar menubar;
    public QMenu menuMotifExplorer;
    public QMenu menuEdit;
    public QMenu menuAnalysis;
    public QStatusBar statusbar;
    private boolean abort = false;


	private boolean nmicaStarted = false;
	
    private NestedMICATask nmicaTask;
    private Signal2<Motif, Integer> 
    	cycleResultsReady = new Signal2<Motif, Integer>();
	
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
	
	//public static final int DEFAULT_MIN_SCROLL_AREA_HEIGHT = 600;
	//public static final int DEFAULT_MAX_SCROLL_AREA_HEIGHT = 600;
	
	public static final int DEFAULT_MIN_WINDOW_WIDTH = LabelledLogoWidget.DEFAULT_TOTAL_WIDGET_WIDTH;
	public static final int DEFAULT_MAX_WINDOW_WIDTH = LabelledLogoWidget.DEFAULT_TOTAL_WIDGET_WIDTH;
	public static final int DEFAULT_MIN_WINDOW_HEIGHT = 400;
	public static final int DEFAULT_MAX_WINDOW_HEIGHT = 600;
	public static final int DEFAULT_MIN_COL_HEIGHT = LogoView.MOTIF_HEIGHT ;

	public MXplor(List<QMotif> motifs) {
		this(motifs, MotifSetView.DEFAULT_MAX_COLS, MotifSetView.DEFAULT_X_OFFSET);
	}
	
	public MXplor(List<QMotif> motifs, int maxCols, int xOffset) {
		this(motifs, maxCols, xOffset, null, false);
	}
	
	public MXplor(List<QMotif> motifs, int maxCols, int xOffset, String[] args, boolean startNMICA) {
		System.out.println("About to set up UI");
		setupUi(motifs, maxCols, xOffset);
		System.out.println("UI set up");
		this.startNMICA = startNMICA;
		this.args = args;
		
    	if (!allMotifSetWindows.contains(this))
    		MXplor.allMotifSetWindows.add(this);
    	
    	System.out.println("About to show new window");
		show();
		resizeWindowAndScrollArea();
	}
	
	
	private void setupUi(List<QMotif> motifs, int maxCols,int xOffset) {
		this.setMinimumWidth(DEFAULT_MIN_WINDOW_WIDTH);
		this.setMaximumWidth(DEFAULT_MAX_WINDOW_WIDTH);
		this.setMaximumHeight(DEFAULT_MAX_WINDOW_HEIGHT);
		
		System.out.println("About to create scroll area");
    	scrollArea = new QScrollArea();
		scrollArea.setMinimumWidth(DEFAULT_MIN_WINDOW_WIDTH);
		scrollArea.setMinimumWidth(DEFAULT_MAX_WINDOW_WIDTH);
		
		scrollArea.setMinimumHeight(DEFAULT_MIN_WINDOW_HEIGHT);
		scrollArea.setMaximumHeight(DEFAULT_MAX_WINDOW_HEIGHT);
		System.out.println("About to set size policies");
		this.setSizePolicy(QSizePolicy.Policy.Preferred,QSizePolicy.Policy.Preferred);
		scrollArea.setSizePolicy(QSizePolicy.Policy.Preferred, QSizePolicy.Policy.Preferred);
		scrollArea.setWidgetResizable(true);
		System.out.println("About to create motif set view");
		motifSetWidget = new MotifSetView(scrollArea, motifs, maxCols, xOffset);
		motifSetWidget.setMinimumWidth(DEFAULT_MIN_WINDOW_WIDTH);
		scrollArea.setWidget(motifSetWidget);
		
		scrollArea.setHorizontalScrollBarPolicy(Qt.ScrollBarPolicy.ScrollBarAlwaysOff);
		setCentralWidget(scrollArea);
		scrollArea.setParent(this);

		System.out.println("About to set actions");
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
        actionBest_hits = new QAction(this);
        actionBest_hits.setObjectName("actionBest_hits");
        actionBest_reciprocal_hits = new QAction(this);
        actionBest_reciprocal_hits.setObjectName("actionBest_reciprocal_hits");
        actionMLEMetaMotif = new QAction(this);
        actionMLEMetaMotif.setObjectName("actionMLEMetaMotif");
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
        
		System.out.println("Status bar added");

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
        menuAnalysis.addAction(actionBest_hits);
        menuAnalysis.addAction(actionBest_reciprocal_hits);
        menuAnalysis.addAction(actionMLEMetaMotif);
        
		System.out.println("About to set icon, title and retranslate");
        setWindowIcon(new QIcon("classpath:icon.png"));
        setWindowTitle("mXplor");
        retranslateUi(this);
        
        System.out.println("About to connect slots...");
        actionSave.triggered.connect(actionSave, "trigger()");

        this.connectSlotsByName();
        
        
    } // setupUi
    
	protected void closeEvent(QCloseEvent e) {
		MXplor.allMotifSetWindows.remove(this);
		
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
        actionBest_hits.setText("Best hits");
        actionBest_reciprocal_hits.setText("Best reciprocal hits");
        actionMLEMetaMotif.setText("MLE metamotif");
        menuMotifExplorer.setTitle("File");
        menuEdit.setTitle("Edit");
        menuAnalysis.setTitle("Analysis");
    } // retranslateUi
    
    public void on_actionOpen_triggered() {
    	openFile();
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
    	List<QMotif> motifs = this.openXMSFile("");
    	if ((motifs == null) || (motifs.size() == 0)) this.close();
    	
    	addMotifs(motifs);
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
    	System.out.println("About to make QMS");
    	List<QMotif> qms = QMotif.create(this.motifSetWidget,new Motif[]{MetaMotifIOTools.metaMotifToAnnotatedMotif(mm)});
    	System.out.println("About to open mxplor window");
    	MXplor mxplorWindow = new MXplor(qms);
    	System.out.println("Metamotif constructed:" + motifs.length);
    	mxplorWindow.show();
    	mxplorWindow.update();
    	mxplorWindow.repaint();
    	mxplorWindow.resizeWindowAndScrollArea();
    }
    
    public void on_actionExtractMotifs_triggered() throws IllegalSymbolException, IllegalAlphabetException {
    	System.out.println("Extracting motifs...");
    	Motif[] motifs = motifSetWidget.allSelectedColumnsAsMotifs();
    	MXplor mxplorWindow = new MXplor(QMotif.create(motifs),
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

    	List<QMotif> motifs = this.openXMSFile("");
    	mxplorWindow = new MXplor(motifs);
    	System.out.println("Motifs read:" + motifs.size());
    	mxplorWindow.addMotifs(motifs);
    	mxplorWindow.update();
    	mxplorWindow.repaint();
    	mxplorWindow.resizeWindowAndScrollArea();
    }

    public List<QMotif> openXMSFile(String fileName) {
    	List<Motif> motifList = null;
    	
        if (fileName.equals(""))
            fileName = QFileDialog
                    .getOpenFileName(this, tr("Open File"), "", 
                    				 new QFileDialog.Filter("Motif set XML files (*.xms)"));

        if (!fileName.equals("")) {
            try {
            	Motif[] motifs = MotifIOTools.loadMotifSetXML(new FileInputStream(fileName));
            	
            	motifList = java.util.Arrays.asList(motifs);
            	
            	//loadMotifs(motifList, true);
    		} catch (FileNotFoundException e) {
    			e.printStackTrace();
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
        }
        if (motifList != null)
        	return QMotif.create(motifList);
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
		
		MXplor window = new MXplor(QMotif.create(Arrays.asList(motifs)), 
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
}
