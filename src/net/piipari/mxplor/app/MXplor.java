package net.piipari.mxplor.app;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.derkholm.nmica.build.MXplorApp;
import net.derkholm.nmica.build.VirtualMachine;
import net.derkholm.nmica.model.metamotif.DifferingLengthException;
import net.derkholm.nmica.model.metamotif.DirichletParamEstimator;
import net.derkholm.nmica.model.metamotif.MetaMotif;
import net.derkholm.nmica.model.metamotif.MetaMotifIOTools;
import net.derkholm.nmica.motif.Motif;
import net.derkholm.nmica.motif.MotifIOTools;
import net.derkholm.nmica.motif.MotifTools;
import net.derkholm.nmica.utils.CliTools;
import net.piipari.mxplor.cmd.RemoveMotifCommand;
import net.piipari.mxplor.cmd.ReverseComplementCommand;
import net.piipari.mxplor.cmd.ShiftCommand;
import net.piipari.mxplor.ui.graphics.MotifRegion;
import net.piipari.mxplor.ui.graphics.MotifRegionSet;
import net.piipari.mxplor.ui.graphics.ScoredMotifRegion;
import net.piipari.mxplor.ui.graphics.SelectableMotifRegion;
import net.piipari.mxplor.ui.model.QMotif;
import net.piipari.mxplor.ui.widget.LabelledLogoView;
import net.piipari.mxplor.ui.widget.LogoView;
import net.piipari.mxplor.ui.widget.MotifSetView;
import net.piipari.mxplor.util.MathUtil;
import net.piipari.mxplor.util.QMotifTools;

import org.biojava.bio.BioError;
import org.biojava.bio.BioException;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.bjv2.util.cli.App;
import org.bjv2.util.cli.ConsoleMessages;
import org.bjv2.util.cli.Option;
import org.bjv2.util.cli.UserLevel;

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
import com.trolltech.qt.gui.QKeySequence;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QMainWindow;
import com.trolltech.qt.gui.QMenu;
import com.trolltech.qt.gui.QMenuBar;
import com.trolltech.qt.gui.QPainter;
import com.trolltech.qt.gui.QPrinter;
import com.trolltech.qt.gui.QScrollArea;
import com.trolltech.qt.gui.QShortcut;
import com.trolltech.qt.gui.QSizePolicy;
import com.trolltech.qt.gui.QStatusBar;
import com.trolltech.qt.gui.QTableView;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

@App(overview="A motif / metamotif set viewer and editor", generateStub=false)
public class MXplor extends QMainWindow {
    private static final String LAST_OPEN_LOCATION = "last.open.location";
    private static final String LAST_SAVE_LOCATION = "last.save.location";
    
	private static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private static final double DEFAULT_PSEUDOCOUNT = 0.005;
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
    public QAction actionAlign;
    
    public QAction actionReverse_complement;
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
	
	public static final int DEFAULT_MIN_WINDOW_WIDTH = LabelledLogoView.DEFAULT_TOTAL_WIDGET_WIDTH - 20;
	public static final int DEFAULT_MAX_WINDOW_WIDTH = LabelledLogoView.DEFAULT_TOTAL_WIDGET_WIDTH + 20;
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
	private QShortcut fileOpenShortcut;
	private QAction actionRemove;
	private QAction actionAddMotif;
	private QAction actionAddMetaMotif;
	private QAction action‚learSelection;
	private QAction actionClearSelection;
	private QMenu menuAllMetaMotifHits;
	private int windowHeight = 0;
	private int windowWidth = 0;
	private String setName = null;
	private String[] pdfs;
	
	private static final String VERSION = "0.1";
	
	@Option(help="Motif set name",optional=true)
	public void setName(String name) {
		System.out.println("Motif set name:" + name);
		this.setName = name;
	}
	
	@Option(help="Window height", optional=true)
	public void setHeight(int h) {
		this.windowHeight = h;
	}
	
	@Option(help="Window width", optional=true)
	public void setWidth(int h) {
		this.windowWidth = h;
	}
	
	@Option(help="Print motif set(s) to PDF(s)", optional=true)
	public void setPdf(String[] filenames) {
		this.pdfs = filenames;
	}
	
	public static void main(String[] args) throws Throwable {
    	String mxplorrc = System.getProperty("user.home") + "/.mxplorrc";
    	
    	File propsFile = new File(mxplorrc);
    	if (propsFile.exists())
    		props.load(
				new BufferedInputStream(
					new FileInputStream(propsFile)));
    			
		QApplication.initialize(args);
		QApplication app = QApplication.instance();

		MXplor mapp = new MXplor();
		List<String> argList = Arrays.asList(args);
		if (argList.indexOf("-help") >= 0) {
			System.out.println("Showing help");
		  ConsoleMessages.helpMessage(app, System.err, UserLevel.USER, 80);
		  return;
		}
		try {
			args = CliTools.configureBean(app, args);
			mapp.run(app,args);
			
			Calendar cal = Calendar.getInstance();
		    SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT);
			props.store(new BufferedOutputStream(
						new FileOutputStream(mxplorrc)),
					sdf.format(cal.getTime()));
		} catch (org.bjv2.util.cli.ConfigurationException ex) {
		  ConsoleMessages.errorMessage(app, System.err, 80, ex);
		  System.exit(1);
		}
	}

	
	public void run(QApplication app, String[] args) throws Exception {
		Motif[] motifs = null;
		String fn = null;
		
		if (args.length > 0) {
			for (String filename : args ) {

				System.out.println("Opening " + filename + " ...");
				HashMap<String, List<Motif>> motifSets;
				if (filename != null) {
					motifSets = importXMS(filename);
					for (String s : motifSets.keySet()) {
						System.err.println(s);
			    		newMXplorWindow(s, QMotifTools.motifsToQMotifs(
			    				motifSets
			    					.get(s)
			    						.toArray(
		    								new Motif[motifSets.get(s).size()])));
					}

				}
				filename = args[0];
			}
		} else {
			MXplor win = new MXplor("Untitled",
					null, 
					LogoView.DEFAULT_MAX_COLS, 
					LogoView.DEFAULT_X_OFFSET);
			win.show();
		}

		if (MXplor.allMotifSetWindows.size() > 0)
			app.installEventFilter(MXplor.allMotifSetWindows.get(0));
		else {
			System.err.println("No motifs were imported from the input XMS file.");
			QApplication.quit();
			System.exit(1);
		}
		
		QApplication.exec();

	}
	
	private void init(String name, List<QMotif> motifs, int maxCols, int xOffset) {
		setupUi(name, motifs, maxCols, xOffset);
		MXplor.allMotifSetWindows.add(this);
		menusNeedUpdating();
		resizeWindowAndScrollArea();
		show();
	}
	
	public MXplor() {
	}
	
	public MXplor(String name, List<QMotif> motifs) {
		this(name, 
			 motifs, 
			 MotifSetView.DEFAULT_MAX_COLS, 
			 MotifSetView.DEFAULT_X_OFFSET);
	}
	
	/*public MXplor(String name, 
			List<QMotif> motifs, 
			int maxCols, 
			int xOffset) {
		this(name, 
			 motifs, 
			 maxCols, 
			 xOffset, 
			 null, 
			 false);
	}*/
	
	public MXplor(	String name, 
					List<QMotif> motifs, 
					int maxCols, 
					int xOffset) {
		init(name,motifs,maxCols,xOffset);
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
        actionNew.setShortcut(tr("Ctrl+N"));
        actionSaveAll = new QAction(this);
        actionSaveAll.setShortcut(tr("Ctrl+Shift+S"));
        actionSaveAll.setObjectName("actionSaveAll");
        actionSave = new QAction(this);
        actionSave.setShortcut(tr("Ctrl+S"));
        actionSave.setObjectName("actionSave");        
        actionExport_PDF = new QAction(this);
        actionExport_PDF.setShortcut(tr("Ctrl+P"));
        actionExport_PDF.setObjectName("actionExport_PDF");
        
        actionOpen = new QAction(this);
        actionOpen.setShortcut(new QKeySequence(tr("Ctrl+O")));
        actionOpen.setObjectName("actionOpen");
        actionAdd = new QAction(this);
        actionAdd.setShortcut(tr("Ctrl+Shift+N"));
        actionAdd.setObjectName("actionAdd");
        actionClose = new QAction(this);
        actionClose.setShortcut(tr("Ctrl+W"));
        actionClose.setObjectName("actionClose");
        actionQuit = new QAction(this);
        actionQuit.setShortcut(tr("Ctrl+Q"));
        actionQuit.setObjectName("actionQuit");
        actionUndo = new QAction(this);
        actionUndo.setShortcut(tr("Ctrl+Z"));
        actionUndo.setObjectName("actionUndo");
        actionRedo = new QAction(this);
        actionRedo.setShortcut(tr("Ctrl+Y"));
        actionRedo.setObjectName("actionRedo");
        
        actionAddMotif = new QAction(this);
        actionAddMotif.setShortcut(tr("+"));
        actionAddMotif.setObjectName("actionAddMotif");
        actionAddMetaMotif = new QAction(this);
        actionAddMetaMotif.setShortcut(tr("Ctrl++"));
        actionAddMetaMotif.setObjectName("actionAddMetaMotif");
        
        actionRemove = new QAction(this);
        actionRemove.setShortcut(tr("-"));
        actionRemove.setObjectName("actionRemoveMotif");

        actionClearSelection = new QAction(this);
        actionClearSelection.setShortcut(tr("Ctrl+Shift+D"));
        actionClearSelection.setObjectName("actionClearSelection");
        
        
        actionRemoveScoredRegions = new QAction(this);
        actionRemoveScoredRegions.setShortcut(tr("Ctrl+Backspace"));
        actionRemoveScoredRegions.setObjectName("actionRemoveScoredRegions");
        actionAddAnnotations = new QAction(this);
        actionAddAnnotations.setShortcut(tr("Shift+A"));
        actionAddAnnotations.setObjectName("actionAddAnnotations");
        actionRenameAnnotations = new QAction(this);
        actionRenameAnnotations.setShortcut(tr("Shift+R"));
        actionRenameAnnotations.setObjectName("actionRenameAnnotations");
        actionSelectAllAnnotations = new QAction(this);
        actionSelectAllAnnotations.setShortcut(tr("Ctrl+A"));
        actionSelectAllAnnotations.setObjectName("actionSelectAllAnnotations");
        actionSelectNoneAnnotations = new QAction(this);
        actionSelectNoneAnnotations.setShortcut(tr("Ctrl+D"));
        actionSelectNoneAnnotations.setObjectName("actionSelectNoneAnnotations");
        actionExpandAnnotationSelection = new QAction(this);
        actionExpandAnnotationSelection.setShortcut(tr("Ctrl+E"));
        actionExpandAnnotationSelection.setObjectName("actionExpandAnnotationSelection");
        actionExtractMotifsFromAnnotations = new QAction(this);
        actionExtractMotifsFromAnnotations.setShortcut(tr("Ctrl+M"));
        actionExtractMotifsFromAnnotations.setObjectName("actionExtractMotifsFromAnnotations");

        actionMLEMetaMotifsFromAnnotations = new QAction(this);
        actionMLEMetaMotifsFromAnnotations.setShortcut(tr("Ctrl+K"));
        actionMLEMetaMotifsFromAnnotations.setObjectName("actionMLEMetaMotifsFromAnnotations");
        
        actionAlign = new QAction(this);
        actionAlign.setShortcut("Ctrl+Shift+A");
        actionAlign.setObjectName("actionAlign");
        
        actionShift_left = new QAction(this);
        actionShift_left.setObjectName("actionShift_left");
        actionShift_left.setShortcut(tr("["));
        actionShift_right = new QAction(this);
        actionShift_right.setShortcut(tr("]"));
        actionShift_right.setObjectName("actionShift_right");
        
        actionReverse_complement = new QAction(this);
        actionReverse_complement.setObjectName("actionReverse_complement");
        actionReverse_complement.setShortcut(tr("/"));
        actionExtractMotifs = new QAction(this);
        actionExtractMotifs.setObjectName("actionExtractMotifs");
        actionExtractMotifs.setShortcut(tr("Enter"));
        
        actionMLEMetaMotif = new QAction(this);
        actionMLEMetaMotif.setObjectName("actionMLEMetaMotif");
        actionMLEMetaMotif.setShortcut("Ctrl+Shift+K");
        
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
        menuEdit.addAction(actionAddMotif);
        menuEdit.addAction(actionRemove);
        menuEdit.addSeparator();
        menuEdit.addAction(actionReverse_complement);
        menuEdit.addSeparator();
        menuEdit.addAction(actionExtractMotifs);
        menuEdit.addAction(actionClearSelection);
        
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
        menuAnnotate.addAction(actionAlign);
        
        menuBestMetaMotifHits = new QMenu("menuBestMetaMotifHits");
        menuBestHits = new QMenu("menuBestHits");
        menuAllMetaMotifHits = new QMenu("menuAllMetaMotifHits");
        menuBestReciprocalHits = new QMenu("menuBestReciprocalHits");
        
        menuAnalysis.addMenu(menuBestHits);
        menuAnalysis.addMenu(menuAllMetaMotifHits);
        
        //menuAnalysis.addMenu(menuBestReciprocalHits);
        //menuAnalysis.addMenu(menuBestMetaMotifHits);
        menuAnalysis.addSeparator();
        menuAnalysis.addAction(actionMLEMetaMotif);
        
        
		setWindowIcon(new QIcon("classpath:icon.png"));
        setWindowTitle("mXplor - " + getName());
        retranslateUi(this);
        
        this.connectSlotsByName();
        
        resize(sizeHint());
    } // setupUi
    
	void retranslateUi(QMainWindow UiMainWindow) {
	    actionNew.setText(tr("New"));
	    actionSave.setText(tr("Save.."));
	    actionSaveAll.setText(tr("Save all.."));
	    actionExport_PDF.setText(tr("Export PDF.."));
	    actionOpen.setText(tr("Open.."));
	    actionAdd.setText(tr("Add.."));
	    actionClose.setText(tr("Close"));
	    actionQuit.setText(tr("Quit"));
	    
	    actionUndo.setText(tr("Undo"));
	    actionRedo.setText(tr("Redo"));
	    actionShift_left.setText(tr("Shift left"));
	    actionShift_right.setText(tr("Shift right"));
	    actionAddMotif.setText(tr("Add motif.."));
	    actionRemove.setText(tr("Remove motif"));
	    
	    actionReverse_complement.setText(tr("Reverse complement"));
	    actionExtractMotifs.setText(tr("Selected columns to motifs"));
	    actionClearSelection.setText(tr("Clear selected columns"));
	    
	    actionRemoveScoredRegions.setText(tr("Remove annotation"));
	    actionAddAnnotations.setText(tr("Add annotation"));
	    actionExpandAnnotationSelection.setText(tr("Expand selection"));
	    actionExtractMotifsFromAnnotations.setText(tr("Annotations to motifs"));
	    actionSelectAllAnnotations.setText(tr("Select all"));
	    actionSelectNoneAnnotations.setText(tr("Select none"));
	    actionMLEMetaMotifsFromAnnotations.setText(tr("MLE metamotif"));
	    actionAlign.setText("Align motifs");
	    actionRenameAnnotations.setText(tr("Rename"));
	    
	    menuBestHits.setTitle(tr("Best hits"));
	    menuAllMetaMotifHits.setTitle(tr("All hits"));
	    menuBestReciprocalHits.setTitle(tr("Best reciprocal hits"));
	    menuBestMetaMotifHits.setTitle(tr("Best metamotif hits"));
	    actionMLEMetaMotif.setText(tr("MLE metamotif"));
	    actionBestMetaMotifHits.setText(tr("Best metamotif hits"));
	    menuMotifExplorer.setTitle(tr("File"));
	    menuEdit.setTitle(tr("Edit"));
	    menuAnnotate.setTitle(tr("Annotate"));
	    menuAnalysis.setTitle(tr("Analyse"));
	}

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
		
		printToPDF(filename);
	}

	private void printToPDF(String filename) {
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
	
    //FIXME: Implement
    public void on_actionNew_triggered() throws Exception {
    	
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
				menuBestHits.addAction(w.getName(), new MotifComparisonSignalReceiver(this,w,true), "trigger()");
				menuAllMetaMotifHits.addAction(w.getName(), new MotifComparisonSignalReceiver(this,w,false),"trigger()");
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
    	//if ((motifs == null) || (motifs.values().size() == 0)) this.close();
    	
    	for (String key : motifs.keySet())
    		addMotifs(motifs.get(key));
    }
    
    
    public void on_actionMLEMetaMotif_triggered() throws 
    	BioException, DifferingLengthException {
    	List<QMotif> qmotifs  = motifSetView.getMotifs();
    	Motif[] motifs = new Motif[qmotifs.size()];
    	MetaMotif mm = null;
    	for (int i = 0; i < qmotifs.size(); i++)
    		motifs[i] = qmotifs.get(i).getNmicaMotif();
    	
    	//try {
		System.out.println("Calculating maximum likelihood estimate for the motifs...");
		try {
			mm = DirichletParamEstimator.mle(this.getName() + "_mle",motifs);
		} catch (BioException e) {
			e.printStackTrace();
			//TODO: Add the DifferingLengthException in!
			//System.err.println("The insulting motif: " + e.getInsultingMotif());
		}
    	//} catch(Exception e) {
    	//	QErrorMessage msg = new QErrorMessage();
    	//	msg.setWindowIcon(new QIcon("classpath:tango/dialog-error.png"));
    	//	msg.setWindowTitle("Error!");
    	//	msg.showMessage(e.getMessage());
    	//	return;
    	//}
    	
    	List<QMotif> qms = QMotif.create(Arrays.asList(new Motif[]{MetaMotifIOTools.metaMotifToAnnotatedMotif(mm)}));
    	MXplor mxplorWindow = new MXplor(this.getName() + "_mle", qms);
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
    
    
    /*
    public void on_actionExtractMotifs_triggered() throws IllegalSymbolException, IllegalAlphabetException {
    	System.out.println("Extracting motifs...");
    	Motif[] motifs = motifSetView.allSelectedColumnsAsMotifs();
    	if (motifs.length == 0) return;
    	
    	MXplor mxplorWindow = new MXplor("selected_motifs",QMotif.motifsToQMotifs(motifs),
				LogoView.DEFAULT_MAX_COLS, LogoView.DEFAULT_X_OFFSET);
    	System.out.println("Motifs constructed:" + motifs.length);
    	menusNeedUpdating();
    	mxplorWindow.show();
    	mxplorWindow.update();
    	mxplorWindow.repaint();
    	mxplorWindow.resizeWindowAndScrollArea();
    }*/
    
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
    	if (motifs.length == 0) return;
    	
    	//debug
    	//MotifIOTools.writeMotifSetXML(
		//	new FileOutputStream(new File("/tmp/foo.xms")),motifs);
    	MXplor mxplorWindow = new MXplor(
						makeSelectedMotifRegionSetName(),
		    			QMotif.motifsToQMotifs(motifs),
						LogoView.DEFAULT_MAX_COLS, LogoView.DEFAULT_X_OFFSET);
    	menusNeedUpdating();
    	mxplorWindow.show();
    	mxplorWindow.update();
    	mxplorWindow.repaint();
    	mxplorWindow.resizeWindowAndScrollArea();
    }

	private String makeSelectedMotifRegionSetName() {
		SortedSet<String> nameSet = new TreeSet<String>();
    	for (SelectableMotifRegion reg : motifSetView.selectedMotifRegions())
    		nameSet.add(reg.getName());
    	String nameStr = "";
    	List<String> nameList = new ArrayList<String>(nameSet);
    	
    	if (nameList.size() == 1)
    		nameStr = nameSet.first();
    	else if (nameList.size() > 1) {
    		for (int i = 0; i < nameList.size(); i++)
    			nameStr = nameStr + "_" + nameList.get(i);
    	}
		return nameStr;
	}
    
    public void on_actionMLEMetaMotifsFromAnnotations_triggered() throws Exception {
    	HashMap<String,List<Motif>> ms = motifSetView.selectedAnnotationsAsMotifsPerRegionSet();
    	List<MetaMotif> mms = new ArrayList<MetaMotif>();
    	
    	int totalNum = 0;
    	for (String s : ms.keySet()) {
			Motif[] m = ms.get(s).toArray(new Motif[ms.get(s).size()]);
			if (m.length > 0) mms.add(DirichletParamEstimator.mle(s,m));
			totalNum = totalNum + m.length;
		}
    	
    	if (totalNum == 0) return;
    	
    	List<Motif> motifs = new ArrayList<Motif>(mms.size());
    	for (int i = 0,len=mms.size(); i < len; i++)
    		motifs.add(MetaMotifIOTools.metaMotifToAnnotatedMotif(mms.get(i)));
    	
    	MXplor mxplorWindow = 
    		new MXplor(
				makeSelectedMotifRegionSetName(), 
				QMotif.create(motifs));
    	
    	mxplorWindow.show();
    	mxplorWindow.update();
    	mxplorWindow.repaint();
    	mxplorWindow.resizeWindowAndScrollArea();
    }
    
    public void on_actionAlign_triggered() throws Exception {
    	
    }
    
    public void on_actionRemoveScoredRegions_triggered() {
    	SelectableMotifRegion[] selRegs = motifSetView.selectedMotifRegions();
    	//motifSetWidget.get
    	motifSetView.removeMotifRegions(selRegs);
    	repaint();
    }
    
    public void on_actionAddAnnotations_triggered() {
    	String name = QInputDialog.getText(this, "Annotation", "Name",
    										QLineEdit.EchoMode.Normal,
											"Untitled",
											Qt.WindowType.Dialog);
    	if (name == null) return;
    	deselectAllAnnotations();
    	
    	MotifRegionSet mregSet = motifSetView.getMotifRegionSetWithName(name);
    	
    	if (mregSet == null) {
    		System.out.println("Making new motif region set");
    		mregSet = new MotifRegionSet(this.motifSetView);
    		mregSet.setName(name);
    		mregSet.setColor(MotifSetView.annotationColors().colorFor(mregSet));
    	} else {
    		System.out.println("Adding to motif region set " + mregSet.getName());
    	}
    	
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
    
    public void clearSelectedColumns() {
    	for (QMotif m : motifSetView.getMotifs()) {
    		for (int i : m.getSelectedColumnIndices())
    			m.getDists().get(i).toggleSelected();
    	}
    }
    
    public void on_actionClearSelection_triggered() {
    	clearSelectedColumns();
    }
    
    public void on_actionRenameAnnotations_triggered() {
    	String newName = QInputDialog.getText(this, "Rename annotation","Name",
    										QLineEdit.EchoMode.Normal,
    										"",
    										Qt.WindowType.Dialog);
    	
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
    
    Set<String> getSelectedRegionSetNames() {
    	Set<String> set = new TreeSet<String>();
    	for (QMotif m : motifSetView.getMotifs()) {
    		for (int i = 0; i < m.regions(); i++) {
    			SelectableMotifRegion reg = (SelectableMotifRegion)m.getRegion(i);
    			if (reg.isSelectedRegion())
    				set.add(reg.getName());
    		}
    	}
    	return set;
    }
    //TODO: Indexing according to annotation set would make this more efficient
    public void selectAllInTheAnnotationSets() {
    	
    	Set<String> set = getSelectedRegionSetNames();
    	
    	for (QMotif m : motifSetView.getMotifs()) {
    		for (int i = 0; i < m.regions(); i++) {
    			SelectableMotifRegion reg = (SelectableMotifRegion)m.getRegion(i);
    			if (set.contains(reg.getName())) reg.setSelectedRegion(true);
    		}
    	}
    	repaint();
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
    	String path = "";
    	if (props.containsKey(LAST_SAVE_LOCATION))
    		path = props.getProperty(LAST_SAVE_LOCATION);
    	
    	String filename = QFileDialog
    							.getSaveFileName(
    									this, 
    									"Save motif sets",
    									path, //directory
    									new QFileDialog.Filter(".xms"));
    	
    	motifSetView.setName(filename);
    	
    	if (filename != null)
    		exportMotifSetToXMS(
    				new BufferedOutputStream(
    						new FileOutputStream(
    							new File(filename))));
    }
    
    public void on_actionSaveAll_triggered() throws Exception {
    	String path = "";
    	if (props.containsKey(LAST_SAVE_LOCATION))
    		path = props.getProperty(LAST_SAVE_LOCATION);
    	
    	if (this.savedFileName == null) {
    		savedFileName = QFileDialog
    							.getSaveFileName(
    									this, 
    									"Save motif sets",
    									path, //directory
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

    public void on_actionRemoveMotif_triggered() {
    	System.out.println("Removing motifs...");
    	
    	System.out.println();
    	for (QMotif m : motifSetView.getSelectedMotifs())
    		motifSetView.getUndoStack().push(new RemoveMotifCommand(this.motifSetView,m));
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
    	/*if ((motifSetView.getMotifs() == null) || 
    		(motifSetView.getMotifs().size() == 0))
    		this.close();*/
    	boolean noMotifsToShowCurrently = 
    				motifSetView.getMotifs() != null && 
    				this.motifSetView.getMotifs().size() == 0;
    	
    	HashMap<String, List<QMotif>> motifs = this.openXMSFile("");
    	
    	List<String> names = new ArrayList<String>(motifs.keySet());
    	
    	if (noMotifsToShowCurrently) {
    		if (names.size() == 1) {
    			setupExistingUI(names.get(0), 
    					motifs.get(names.get(0)), 
    					MotifSetView.DEFAULT_MAX_COLS, MotifSetView.DEFAULT_X_OFFSET);
    		
    			setWindowTitle(names.get(0));
    		}
    		else if (names.size() > 1){
    			setupExistingUI(names.get(0), 
    					motifs.get(names.get(0)), 
    					MotifSetView.DEFAULT_MAX_COLS, MotifSetView.DEFAULT_X_OFFSET);

        		setWindowTitle(names.get(0));
        		
        		for (int i = 1, size=names.size(); i < size; i++)
    				newMXplorWindow(names.get(i), motifs.get(names.get(i)));
    		}
    		
    	} else
    		for (String s : motifs.keySet())
	    		newMXplorWindow(s, motifs.get(s));
    }
    
    private void setupExistingUI(String name, List<QMotif> motifs, int maxCols, int xOffset) {
    	motifSetView = new MotifSetView(name, scrollArea, motifs, maxCols, xOffset, infoContentScale);
		motifSetView.setMinimumWidth(DEFAULT_MIN_WINDOW_WIDTH);
		scrollArea.setWidget(motifSetView);
    }

	private static void newMXplorWindow(String s, List<QMotif> motifs) {
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

	private static void menusNeedUpdating() {
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
    	
        if (fileName == null || fileName.equals("")) {
        	String path = "";
        	if (props.containsKey(LAST_OPEN_LOCATION))
        		path = props.getProperty(LAST_OPEN_LOCATION);
        
            fileName = QFileDialog
                    .getOpenFileName(this, tr("Open File"), path, 
                    				 new QFileDialog.Filter("Motif set XML files (*.xms)"));
        }
        if (!(fileName == null) || fileName.equals("")) {
        	HashMap<String, List<Motif>> motifSets = importXMS(fileName);
        	HashMap<String,List<QMotif>> kps = new HashMap<String,List<QMotif>>();
        	
        	if (motifSets.keySet() == null || motifSets.keySet().size() == 0) return null;
        	
    		for (String s : motifSets.keySet())
    			kps.put(s, QMotif.create(motifSets.get(s)));
    		
    		props.setProperty(LAST_OPEN_LOCATION, new File(fileName).getParent());
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
    
    private static Properties props = new Properties();
    
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
		/*if (motifSetView.getMotifs() != null) {
			//System.out.println("Will resize (non-null motifs)");
			resize(LabelledLogoView.DEFAULT_TOTAL_WIDGET_WIDTH,
					(int)Math.round(DEFAULT_MIN_COL_HEIGHT * NUM_MOTIFS_SHOWN_AT_A_TIME * 1.0));
			scrollArea.resize(LabelledLogoView.DEFAULT_TOTAL_WIDGET_WIDTH, Math.min(DEFAULT_MIN_COL_HEIGHT * 3,this.width()));
		}
		else {
			//System.out.println("Will resize (null motifs)");
			resize(DEFAULT_MIN_WINDOW_WIDTH,DEFAULT_MIN_COL_HEIGHT * 4);
			scrollArea.resize(DEFAULT_MIN_WINDOW_WIDTH,DEFAULT_MIN_COL_HEIGHT);
			
		}*/
		updateWidget();
	}
	
	public QSize sizeHint() {
		return new QSize(LabelledLogoView.DEFAULT_TOTAL_WIDGET_WIDTH, 
							DEFAULT_MIN_COL_HEIGHT * NUM_MOTIFS_SHOWN_AT_A_TIME);
	}
	
	/*
	protected void showEvent(QShowEvent showEvent) {
		if (useNmica) {
			if (!nmicaStarted) {
				if (startNMICA) nmicaTask = new NestedMICATask(this, args);
				nmicaTask.setDaemon(true);
				nmicaTask.startTraining();
				
			}
		}
	}*/

	/*
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
	}*/
	
	public static HashMap<String, List<Motif>> importXMS(String filename) throws Exception  {
		return importXMS(filename, DEFAULT_PSEUDOCOUNT);
	}
	
	public static HashMap<String, List<Motif>> importXMS(String filename, double pseudoCounts) throws Exception {
		HashMap<String,List<Motif>> sessionWindowMotifSets = new HashMap<String,List<Motif>>();
		
		if (filename.equals("")) return null;
		
		Motif[] motifs = MotifIOTools.loadMotifSetXML(new BufferedInputStream(new FileInputStream(filename)));
		if (pseudoCounts > 0 && MotifTools.containsWeightsBelowValue(motifs, DEFAULT_PSEUDOCOUNT)) {
        	System.err.println("Too low weights were found. Will add pseudocount of " 
        			+ DEFAULT_PSEUDOCOUNT + " to all columns");
			for (Motif m : motifs)
				MotifTools.addPseudoCounts(m, pseudoCounts);
		}
    	
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
		private boolean allHits;
		
		public MotifComparisonSignalReceiver(MXplor xplor0, 
											 MXplor xplor1, 
											 boolean b) {
			this.mxplor0 = xplor0;
			this.mxplor1 = xplor1;
			this.allHits = b;
		}
		public void trigger() {
			mxplor0.comparisonAgainstMotifset(mxplor1, this.allHits);
		}
	}

	//FIXME: Move comparison logic to somewhere nicer
	public void comparisonAgainstMotifset(MXplor mxplor1, boolean allHits) {
		
		//FIXME: Threshold should have a different value
		double threshold = QInputDialog.getDouble(this,
                "Score threshold",
                "value",
                -50,
               -100000000,
                100000000,
                5,
                new Qt.WindowFlags(Qt.WindowType.Dialog));
        
		//FIXME: Give an error message
		if (Double.isNaN(threshold) || Double.isInfinite(threshold)) return;
		
		deselectAllAnnotations();
		
		for (QMotif qm0 : this.motifSetView.getMotifs()) {
			if (!qm0.isMetaMotif()) continue; //FIXME: Support motif--motif hits
			
			MotifRegionSet mregSet = new MotifRegionSet(this.motifSetView);
			mregSet.setColor(qm0.color());
			
			for (QMotif qm1 : mxplor1.motifSetView.getMotifs()) {
				//System.err.println("Best hits with " + qm1);
				bestHitsWith(mxplor1, threshold, qm0, mregSet, qm1, allHits);
			}
		}
	}

	//FIXME: Move comparison logic across to somewhere else
	private void bestHitsWith(
			MXplor mxplor1, 
			double threshold, 
			QMotif qm0,
			MotifRegionSet mregSet, 
			QMotif qm1, 
			boolean allHits) throws BioError {
		double[] hitLogProbs = null;
		//System.out.println("Best hits!!");
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
			
			if (!allHits) {
				for (int i = 0; i < hitLogProbs.length; i++) {
					if (!Double.isNaN(hitLogProbs[i]) && 
						!Double.isInfinite(hitLogProbs[i]) && 
						maxValue < hitLogProbs[i]) {
						
						maxValue = hitLogProbs[maxIndex = i];
					}
					//System.out.println(maxValue);
					if (!Double.isInfinite(maxValue) && maxValue > threshold) {
						
						//System.out.println("Max hit: " + maxValue + " (i:" + maxIndex + ")");
						addScoredHitRegion(qm0, mregSet, qm1, maxIndex, maxValue);
						
						updateWidget();
						mxplor1.updateWidget();
					} else {
						System.out.println("No significant hits found");
					}
				}
			} else {
				for (int i = 0; i < hitLogProbs.length; i++) {
					if (!Double.isNaN(hitLogProbs[i]) && 
						!Double.isInfinite(hitLogProbs[i]) && 
						hitLogProbs[i] > threshold) {
						
						maxValue = hitLogProbs[maxIndex = i];
						addScoredHitRegion(qm0, mregSet, qm1, maxIndex, maxValue);
						
						updateWidget();
						mxplor1.updateWidget();
					}
				}
			}
		} else {
			
		}
	}


	private void addScoredHitRegion(QMotif qm0, MotifRegionSet mregSet,
			QMotif qm1, int maxIndex, double maxValue) {
		int metaMotifLength = qm0.getMetaMotif().columns();
		System.out.println("Metamotif length:" + 
								metaMotifLength + " " + 
								qm1.getNmicaMotif().getWeightMatrix().columns());
		ScoredMotifRegion sar = new ScoredMotifRegion(mregSet,
				qm1,
				maxIndex,
				metaMotifLength,
				MathUtil.exp2(maxValue));
		sar.setName(qm0.getNmicaMotif().getName());
		sar.setSelectedRegion(true);
		sar.setColor(mregSet.color());
		//sar.setBrushes(qm0.color());
		
		qm1.addRegion(sar);
	}
}