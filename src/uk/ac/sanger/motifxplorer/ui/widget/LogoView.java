package uk.ac.sanger.motifxplorer.ui.widget;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.derkholm.nmica.model.metamotif.MetaMotif;
import net.derkholm.nmica.model.metamotif.MetaMotifIOTools;
import net.derkholm.nmica.motif.Motif;
import net.derkholm.nmica.motif.MotifIOTools;

import org.biojava.bio.BioException;
import org.biojava.bio.BioRuntimeException;
import org.biojava.bio.dist.Distribution;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.IllegalSymbolException;

import uk.ac.sanger.motifxplorer.ui.graphics.BoundaryItem;
import uk.ac.sanger.motifxplorer.ui.graphics.MotifBoundingBox;
import uk.ac.sanger.motifxplorer.ui.graphics.MotifRegion;
import uk.ac.sanger.motifxplorer.ui.graphics.SymbolGraphicsItem;
import uk.ac.sanger.motifxplorer.ui.model.QDistribution;
import uk.ac.sanger.motifxplorer.ui.model.QMotif;
import uk.ac.sanger.motifxplorer.ui.model.ResVal;

import com.trolltech.qt.core.QByteArray;
import com.trolltech.qt.core.QEvent;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.core.QPoint;
import com.trolltech.qt.core.QPointF;
import com.trolltech.qt.core.QRectF;
import com.trolltech.qt.core.QSize;
import com.trolltech.qt.core.QSizeF;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QAbstractGraphicsShapeItem;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QBrush;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QDrag;
import com.trolltech.qt.gui.QDragEnterEvent;
import com.trolltech.qt.gui.QDragLeaveEvent;
import com.trolltech.qt.gui.QDragMoveEvent;
import com.trolltech.qt.gui.QDropEvent;
import com.trolltech.qt.gui.QFont;
import com.trolltech.qt.gui.QFrame;
import com.trolltech.qt.gui.QGraphicsItemInterface;
import com.trolltech.qt.gui.QGraphicsPathItem;
import com.trolltech.qt.gui.QGraphicsRectItem;
import com.trolltech.qt.gui.QGraphicsScene;
import com.trolltech.qt.gui.QGraphicsView;
import com.trolltech.qt.gui.QKeyEvent;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QLinearGradient;
import com.trolltech.qt.gui.QMimeData;
import com.trolltech.qt.gui.QMouseEvent;
import com.trolltech.qt.gui.QPainter;
import com.trolltech.qt.gui.QPainterPath;
import com.trolltech.qt.gui.QPen;
import com.trolltech.qt.gui.QPixmap;
import com.trolltech.qt.gui.QResizeEvent;
import com.trolltech.qt.gui.QSizePolicy;
import com.trolltech.qt.gui.QWidget;

public class LogoView extends QGraphicsView implements ContainedByMotifSetWidget {
	private static final int EXTRA_PADDING = 0;
	private static final int PADDING = 1;
	private static final int SAMPLE_NUM = 50;
	private static final int HIGHLIGHT_PEN_WIDTH = 2;
	private static final double NORMAL_PEN_WIDTH = 1.0;
	private static final double PEN_OPACITY_SCALE = 3.0;
	private QMotif motif;
	protected int maxCols = DEFAULT_MAX_COLS;
	public static final int DEFAULT_MAX_COLS = 16;
	public static final int DEFAULT_X_OFFSET = 0;
	
	private int lastSelectedColumn = -1;
	private boolean isInFocus;
	private QPoint dragStartPosition;

	private boolean isSelected;
	
	private int xOffset;
	
	private QFont font;
	
	private QGraphicsPathItem pathItemA,pathItemC,pathItemG,pathItemT;
	private QPainterPath pathA,pathC,pathG,pathT;
	private QRectF rectA,rectC,rectG,rectT;
	
	private List<MotifRegion> regions = new ArrayList<MotifRegion>();
	private MotifLabelLineEdit lineEdit;
	
	
	//private double maxHeight = DEFAULT_HEIGHT;
	//private double maxWidth = DEFAULT_WIDTH;
	
	public static final int MOTIF_HEIGHT = 70;
	public static final int MOTIF_WIDTH = 550;
	
	private static final QPen borderNormalPen = new QPen(
													new QColor(100,100,255),
													1.0,
													Qt.PenStyle.SolidLine,
													Qt.PenCapStyle.RoundCap,
													Qt.PenJoinStyle.RoundJoin);
	private static final QPen borderHighlightPen = new QPen(
													new QColor(200,200,255),
													2.5,
													Qt.PenStyle.SolidLine,
													Qt.PenCapStyle.RoundCap,
													Qt.PenJoinStyle.RoundJoin);
	
	//TODO: Set these up in a bit more generic way... No DNA/protein colour specificity (i.e. you should make a QSymbol)
	private static HashMap<String,QColor> normalColours = new HashMap<String, QColor>();
	private static HashMap<String,QColor> highlightColours = new HashMap<String, QColor>();
	private static HashMap<String,QColor> normalPenColours = new HashMap<String, QColor>();
	private static HashMap<String,QColor> highlightPenColours = new HashMap<String, QColor>();
	private static HashMap<String,QBrush> normalBrushes = new HashMap<String, QBrush>();
	private static HashMap<String,QBrush> highlightBrushes = new HashMap<String, QBrush>();
	private static HashMap<String,QPen> normalPens = new HashMap<String, QPen>();
	private static HashMap<String,QPen> highlightPens = new HashMap<String, QPen>();
	
	private static final QLinearGradient normalBrushGrad;
	private static final QLinearGradient hiliteBrushGrad;
	private static final QBrush normalBrush;
	private static final QBrush selectedBrush;
	
	//private static final QBrush normalBrush = new QBrush(new QColor(255,255,255,0));
	//private static final QBrush selectedBrush = new QBrush(new QColor(255,255,100,100));
	

	//private static final QPen normalPenBrush = new QPen(new QColor(255,255,50));
	//private static final QPen selectedPenBrush = new QPen(new QColor(100,100,255));
	
	static {
		normalBrushGrad = new QLinearGradient(0,0,0,MOTIF_WIDTH);
		hiliteBrushGrad = new QLinearGradient(0,0,0,MOTIF_HEIGHT);
		normalBrushGrad.setColorAt(0, new QColor(255,255,255,0));
		normalBrushGrad.setColorAt(1, new QColor(255,255,255,0));
		
		hiliteBrushGrad.setColorAt(0, new QColor(71,119,255,50));
		hiliteBrushGrad.setColorAt(1, new QColor(159,219,242,50));
		
		normalBrush = new QBrush(normalBrushGrad);
		selectedBrush = new QBrush(hiliteBrushGrad);
	}
	
	//FIXME: Use DNATools.a(), c(), g(), t(), if not something even more general
	static {
		int opacity = 200;
		normalColours.put("A", new QColor(0,255,0,opacity));
		normalColours.put("C", new QColor(10,10,255,opacity));
		normalColours.put("G", new QColor(255,255,10,opacity));
		normalColours.put("T", new QColor(255,0,0,opacity));
		
		normalPenColours.put("A", new QColor(0,255,0,Math.min(255,(int)Math.round(opacity * PEN_OPACITY_SCALE))));
		normalPenColours.put("C", new QColor(10,10,255,Math.min(255,(int)Math.round(opacity  * PEN_OPACITY_SCALE))));
		normalPenColours.put("G", new QColor(255,255,10,Math.min(255,(int)Math.round(opacity * PEN_OPACITY_SCALE))));
		normalPenColours.put("T", new QColor(255,0,0,Math.min(255,(int)Math.round(opacity  * PEN_OPACITY_SCALE))));
		
		highlightColours.put("A", new QColor(0,200,0,opacity));
		highlightColours.put("C", new QColor(10,10,200,opacity));
		highlightColours.put("G", new QColor(200,200,10,opacity));
		highlightColours.put("T", new QColor(200,0,0,opacity));
		
		highlightPenColours.put("A", new QColor(0,200,0,Math.min(255,(int)Math.round(opacity * PEN_OPACITY_SCALE))));
		highlightPenColours.put("C", new QColor(10,10,200,(int)Math.min(255,Math.round(opacity  * PEN_OPACITY_SCALE))));
		highlightPenColours.put("G", new QColor(200,200,10,(int)Math.min(255,Math.round(opacity * PEN_OPACITY_SCALE))));
		highlightPenColours.put("T", new QColor(200,0,0,(int)Math.min(255,Math.round(opacity  * PEN_OPACITY_SCALE))));
		
		normalBrushes.put("A", new QBrush(normalColours.get("A")));
		normalBrushes.put("C", new QBrush(normalColours.get("C")));
		normalBrushes.put("G", new QBrush(normalColours.get("G")));
		normalBrushes.put("T", new QBrush(normalColours.get("T")));
		
		highlightBrushes.put("A", new QBrush(highlightColours.get("A")));
		highlightBrushes.put("C", new QBrush(highlightColours.get("C")));
		highlightBrushes.put("G", new QBrush(highlightColours.get("G")));
		highlightBrushes.put("T", new QBrush(highlightColours.get("T")));
		
		normalPens.put("A", new QPen(normalPenColours.get("A"),NORMAL_PEN_WIDTH,
				Qt.PenStyle.SolidLine,
				Qt.PenCapStyle.RoundCap,
				Qt.PenJoinStyle.RoundJoin));
		normalPens.put("C", new QPen(normalPenColours.get("C"),NORMAL_PEN_WIDTH,
				Qt.PenStyle.SolidLine,
				Qt.PenCapStyle.RoundCap,
				Qt.PenJoinStyle.RoundJoin));
		normalPens.put("G", new QPen(normalPenColours.get("G"),NORMAL_PEN_WIDTH,
				Qt.PenStyle.SolidLine,
				Qt.PenCapStyle.RoundCap,
				Qt.PenJoinStyle.RoundJoin));
		normalPens.put("T", new QPen(normalPenColours.get("T"),NORMAL_PEN_WIDTH,
				Qt.PenStyle.SolidLine,
				Qt.PenCapStyle.RoundCap,
				Qt.PenJoinStyle.RoundJoin));
		highlightPens.put("A", new QPen(highlightPenColours.get("A"),HIGHLIGHT_PEN_WIDTH,
				Qt.PenStyle.SolidLine,
				Qt.PenCapStyle.RoundCap,
				Qt.PenJoinStyle.RoundJoin));
		highlightPens.put("C", new QPen(highlightPenColours.get("C"),HIGHLIGHT_PEN_WIDTH,
				Qt.PenStyle.SolidLine,
				Qt.PenCapStyle.RoundCap,
				Qt.PenJoinStyle.RoundJoin));
		highlightPens.put("G", new QPen(highlightPenColours.get("G"),HIGHLIGHT_PEN_WIDTH,
				Qt.PenStyle.SolidLine,
				Qt.PenCapStyle.RoundCap,
				Qt.PenJoinStyle.RoundJoin));
		highlightPens.put("T", new QPen(highlightPenColours.get("T"),HIGHLIGHT_PEN_WIDTH,
				Qt.PenStyle.SolidLine,
				Qt.PenCapStyle.RoundCap,
				Qt.PenJoinStyle.RoundJoin));
	}
	
	private double maxSymWidth;
	private QSize widgetSize;
	
	private QGraphicsRectItem borderItem;
	/*
	private QPen borderFocusItem = new QPen();
	private QGraphicsPathItem dragInfoTextItem;
	private QPainterPath dragInfoTextPath;
	private QFont infoFont;
	*/
	private DistributionItemIface lastSelectedItem;
	
	private int METAMOTIF_OPACITY = 20;
	private int NONMETAMOTIF_OPACITY = 150;
	private boolean infoContentScale;
	private boolean firstTime = true;
	private boolean offsetApplied;
	
	public LogoView(
			QWidget parent, 
			QSize widgetSize, 
			QMotif m, 
			int maxCols, 
			int xOffset, 
			boolean infoContentScale) {
		super(parent);
		setParent(parent);
		this.infoContentScale = infoContentScale;
		this.maxCols = maxCols;
		this.widgetSize = widgetSize;
		resize(widgetSize);
		
		this.setScene(
			new QGraphicsScene(
				new QRectF(0,0,widgetSize.width(),widgetSize.height()), this));
		
		setResizeAnchor(QGraphicsView.ViewportAnchor.NoAnchor);
		setTransformationAnchor(QGraphicsView.ViewportAnchor.NoAnchor);
		setRenderHint(QPainter.RenderHint.Antialiasing);
		setFrameStyle(QFrame.Shape.VLine.value());
		setFocusPolicy(Qt.FocusPolicy.ClickFocus);
		setAutoFillBackground(false);
		
		setSizePolicy(QSizePolicy.Policy.Preferred, QSizePolicy.Policy.Fixed);
		setUpFont();
		this.xOffset = xOffset;
		
		setUpSymbolPainterPaths();
		setUpBorderAndBackground();
		
		//always use the setter for motifs!
		setMotif(m);
		this.setMouseTracking(true);
		setMouseTracking(true);
		setAcceptDrops(true);
		
	}


	private void setUpFont() {
		this.font = new QFont("Arial", 1);
		//this.infoFont = new QFont("Arial", 30);
		font.setBold(true);
	}
	
	
	public void addLineEditAndMotifWidgets(QMotif qm, int i) {
		if (qm != null) {
			QLineEdit lineEdit = new QLineEdit();
			this.setSizePolicy(QSizePolicy.Policy.Preferred, QSizePolicy.Policy.Preferred);
			lineEdit.setSizePolicy(QSizePolicy.Policy.Preferred, QSizePolicy.Policy.Preferred);
			lineEdit.setText(qm.getNmicaMotif().getName());
			lineEdit.setParent(this);
		}
		
	}
	
	//TODO: Generalise to any symbol tokenizations, not just letters A,C,G,T
	private void setUpSymbolPainterPaths() {
		pathA = new QPainterPath();
		pathG = new QPainterPath();
		pathC = new QPainterPath();
		pathT = new QPainterPath();
		
		pathA.addText(new QPointF(0,0), font, "A");
		pathC.addText(new QPointF(0,0), font, "C");
		pathG.addText(new QPointF(0,0), font, "G");
		pathT.addText(new QPointF(0,0), font, "T");
		
		pathItemA = new QGraphicsPathItem(pathA);
		pathItemG = new QGraphicsPathItem(pathC);
		pathItemC = new QGraphicsPathItem(pathG);
		pathItemT = new QGraphicsPathItem(pathT);
		
		rectA = pathItemA.boundingRect();
		rectC = pathItemC.boundingRect();
		rectG = pathItemG.boundingRect();
		rectT = pathItemT.boundingRect();
		
		//hah... generalise again
		maxSymWidth = rectA.width();
		maxSymWidth = Math.max(maxSymWidth, rectC.width());
		maxSymWidth = Math.max(maxSymWidth, rectG.width());
		maxSymWidth = Math.max(maxSymWidth, rectT.width());
	}

	//TODO: Should give an interface as an argument instead
	
	private void setUpLogo(QMotif qm) {
		double blockWidth = this.blockWidth();
		
		boolean isSample = qm.isSample();
		boolean includeBounds = !isSample;
		MotifBoundingBox boundingBox;
		
		if (isSample)
			boundingBox = qm.getLinkedQMotif().getBoundingBox();
		else {
			qm.setBoundingBox(boundingBox = new MotifBoundingBox(qm));
		}
		
		if (!isSample) {
			qm.getBoundingBox().setVisible(true);
			qm.getBoundingBox().setEnabled(true);
		}
		
		if (!scene()
				.items().contains(boundingBox)) 
					scene().addItem(boundingBox);
		
		
        for (int i = 0; i < qm.getDists().size(); i++) {
    		double base = MOTIF_HEIGHT - PADDING;
    		
			QDistribution qdist = qm.getDists().get(i);
			Distribution dist = qdist.getDist();
			SymbolTokenization toke = null;
	        
			try {
				toke = dist.getAlphabet().getTokenization("token");
			} catch (BioException ex) {
				throw new BioRuntimeException(ex);
			}
			
			double distScale;
			if (infoContentScale) distScale = qdist.getDistScale();
			else distScale = 1;
			
			double totDistHeight = (size().height() - 2*PADDING - 2*EXTRA_PADDING)  * distScale;
			
			double accumHeightOffsetFromBottom = PADDING + EXTRA_PADDING;
			double accumHeight = PADDING;
			double accumHeightAlt = 0.0;
			
			double x = PADDING + EXTRA_PADDING + i * blockWidth * 1;
			
			BoundaryItem distBoundItem = null;
			if (includeBounds) {
				distBoundItem = new BoundaryItem(qdist, 
						new QRectF(new QPointF(x,base - MOTIF_HEIGHT), 
						new QSizeF(blockWidth,
						MOTIF_HEIGHT)));
				qdist.setBoundItem(distBoundItem);
				distBoundItem.setParentItem(boundingBox);
				
				//if (isSample) distBoundItem.setEnabled(false);
			}
			
			for(Iterator it = qdist.getInfo().iterator(); it.hasNext();) {
				ResVal rv = (ResVal) it.next();
				double relFreq = rv.getValue();
				double thisSymbolHeight = totDistHeight * relFreq;
				accumHeight = accumHeight + thisSymbolHeight;
				//absolutely nothing to draw if rel. freq 0
				if (relFreq <= 0) {
					continue; 
				}
				String s = null;
				try {
					s = toke.tokenizeSymbol(rv.getToken()).toUpperCase();
				} catch (IllegalSymbolException ex) {
					throw new BioRuntimeException(ex);
				}
				
				SymbolGraphicsItem symItem = this.pathItemForString(qdist, s, !isSample);

				if (qm.isMetaMotif())
					this.setPenAndBrush(s, symItem, Math.min(255, (int) Math
						.round(qm.getPrecision(i)
								* QMotif.PRECISION_ALPHA_SCALING)));
				else
					if (isSample)
						this.setPenAndBrush(s, symItem, Math.min(255,QMotif.PRECISION_ALPHA_SCALING_FOR_SAMPLE));
					else if (qm.isMetaMotif())
						this.setPenAndBrush(s, symItem, (int)Math.round(METAMOTIF_OPACITY * 0.1));
					else
						this.setPenAndBrush(s, symItem, NONMETAMOTIF_OPACITY);
						
				double y = base - accumHeight + thisSymbolHeight;
				
				double blockWidthScale = blockWidth / symItem.boundingRect().width();
				double blockHeightScale = thisSymbolHeight / symItem.boundingRect().height();
				
				symItem.scale(blockWidthScale, blockHeightScale);
				symItem.setPos(x, y);
				symItem.setZValue(1);
				accumHeightAlt = accumHeightAlt + symItem.sceneBoundingRect().height();
				
				//scaling doesn't do too precise job with large scalings, positions need to be finetuned
				//TODO: Look into this effect more... You sure you're scaling correctly?
				symItem.setPos(
							symItem.x() - (symItem.sceneBoundingRect().left() - x), 
							symItem.y() - (symItem.sceneBoundingRect().bottom() - y));
				
				
				qdist.getSymbolItems().add(symItem);
				if (distBoundItem != null)
					symItem.setParentItem(distBoundItem);
				else
					if (boundingBox != null)
						symItem.setParentItem(boundingBox);
					else
						scene().addItem(symItem);
				
				symItem.setZValue(2);
				accumHeightOffsetFromBottom = accumHeightOffsetFromBottom + symItem.sceneBoundingRect().height();
			}
		}
        
        boundingBox.updateLocation();
	}
	
	protected void resizeEvent(QResizeEvent e) {
		//setUpFont();
		//System.out.println(e.size().width());
		//System.out.println(this.rect().width());
		//System.out.println();
		//borderItem.rect().setWidth(e.size().width());
		borderItem.setRect(new QRectF(PADDING, 
									  PADDING,
									  e.size().width() - PADDING - EXTRA_PADDING,
									  e.size().height() - PADDING - EXTRA_PADDING));
		//borderItem.setPos(0,0);
		//super.resizeEvent(e);
	}
	
	private void setUpBorderAndBackground() {
		borderItem = new QGraphicsRectItem(
							new QRectF(PADDING, 
							  PADDING,
							  size().width() - PADDING,
							  size().height() - PADDING));
		
		borderItem.setPen(borderNormalPen);
		borderItem.setBrush(normalBrush);
		scene().addItem(borderItem);
		borderItem.setZValue(0);
	}
	
	public void setPenAndBrush(String s, QAbstractGraphicsShapeItem qg, int opacity) {
		QColor brushColor = new QColor(normalBrushes.get(s).color());
		QColor penColor = normalPens.get(s).color();
		
		brushColor.setAlpha(opacity);
		int scaledOpacity = (int)Math.round(opacity);
		penColor.setAlpha(Math.min(255,scaledOpacity));
		
		QBrush brush  = new QBrush(brushColor);
		QPen pen =  new QPen(penColor);
		qg.setBrush(brush);
		qg.setPen(pen);
		if (qg instanceof SymbolGraphicsItem) {
			((SymbolGraphicsItem)qg).setNormalBrush(brush);
			((SymbolGraphicsItem)qg).setNormalPen(pen);
		}
	}
	
	
	//FIXME: Don't use strings but Symbols here!
	public SymbolGraphicsItem pathItemForString(QDistribution dist, String s, boolean acceptsHover) {
		if (s.equals("A"))
			return new SymbolGraphicsItem(dist,new QPainterPath(pathA));
		else if (s.equals("C"))
			return new SymbolGraphicsItem(dist,new QPainterPath(pathC));
		else if (s.equals("G"))
			return new SymbolGraphicsItem(dist,new QPainterPath(pathG));
		else if (s.equals("T"))
			return new SymbolGraphicsItem(dist,new QPainterPath(pathT));
		else return null;
	}
    
	public double blockWidth() {
		return (double)sizeHint().width() / maxCols;
	}
	
	
	public QSize sizeHint() {
		/*if (parent() == null || !(parent() instanceof QWidget))
			return widgetSize;
		else {
			QWidget parentWidget = (QWidget)parent();
			QSize size = new QSize(widgetSize.width(),
									parentWidget.size().height());
			
			return size;
		}*/
		return new QSize((int)MOTIF_WIDTH,(int)MOTIF_HEIGHT);
	}
	
	
	/**
	 * FIXME: Move the event handling across to the graphics item views
	 * 
	 * @see com.trolltech.qt.gui.QGraphicsView#mousePressEvent(com.trolltech.qt.gui.QMouseEvent)
	 */
	protected void mousePressEvent(QMouseEvent e) {
		dragStartPosition = e.pos();
		setFocus();
		int column = wmColumn(e.pos());
		DistributionItemIface item = itemAtPosition(e.pos());
		
		if (e.modifiers().isSet(Qt.KeyboardModifier.ShiftModifier)) {
			//this.toggleIsSelected();
			if (lastSelectedColumn <= column) {
				for (int i = (lastSelectedColumn + 1); i <= (column);i++) {
					toggleSelection(i);
				}
			} else {
				for (int i = column; i <= (lastSelectedColumn);i++) {
					toggleSelection(i);
				}
			}
			for (QDistribution qd : motif.getDists())
				qd.selectionStateUpdated();
			
		} else {
			//toggleSelectionAtPosition(e.pos());
		}
		lastSelectedColumn = column;
		lastSelectedItem = item;
		
		super.mousePressEvent(e);
	}


	public void mouseDoubleClickEvent(QMouseEvent e) {
		if (e.modifiers().isSet(Qt.KeyboardModifier.ControlModifier)) {
			if (!e.modifiers().isSet(Qt.KeyboardModifier.AltModifier)) {
				for (int j = 0; j < motifSetView().logoWidgets(); j++) {
					LogoView logo = motifSetView().getLabelledLogoWidget(j).getLogo();
					if (logo != this && logo.getMotif().isSelected()) {
						logo.getMotif().toggleSelected();
						logo.borderItem.setBrush(normalBrush);
					}
				}
			}
			
			motif.toggleSelected();
			if (motif.isSelected()) {
				borderItem.setBrush(selectedBrush);
			} else {
				borderItem.setBrush(normalBrush);
			}
	
		}
		super.mouseDoubleClickEvent(e);
	}


	protected void mouseMoveEvent(QMouseEvent e) {
		if (motif == null) {
			super.mouseMoveEvent(e);
			return;
		}
		
		if (dragStartPosition == null) {
			super.mouseMoveEvent(e);
			return;
		}

		System.out.println("Subst     :" + e.pos().subtract(dragStartPosition).manhattanLength());
		System.out.println("Start drag:" + QApplication.startDragDistance());
		
		if (e.pos().subtract(dragStartPosition).manhattanLength() < QApplication.startDragDistance())
			return;
		
		dragStartPosition = null;
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		try {
			System.out.println("Will update");
			motif.updateQMotifAnnotations();
			MotifIOTools.writeMotifSetXML(outputStream, new Motif[] {motif.getNmicaMotif()});
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		if (outputStream != null) {
			QDrag drag = new QDrag(this);
			QMimeData mimeData = new QMimeData();
			//System.out.println("Byte array size:" + outputStream.size());
			if (outputStream.size() > 0) {
				byte[] bytes = outputStream.toByteArray();
				QByteArray qbytes = new QByteArray(bytes);
				//System.out.println(qbytes.toString());
				mimeData.setData("xml/xms", qbytes);
				drag.setMimeData(mimeData);
	
				QPixmap pixmap = new QPixmap(this.size());
				this.render(pixmap);
				drag.setPixmap(pixmap);
				drag.setHotSpot(new QPoint(0,0));
				drag.exec();
			} else {
				//TODO: throw exception here
				System.err.println("Nothing was written to the stream!");
			}
		}
		
		super.mouseMoveEvent(e);
	}


	protected void dragLeaveEvent(QDragLeaveEvent e) {
		//System.out.println("Drag leave.");
		super.dragLeaveEvent(e);
	}
	
	protected void dragEnterEvent(QDragEnterEvent e) {
		if (e.mimeData().hasFormat("xml/xms")) {
			e.setDropAction(Qt.DropAction.MoveAction);
			e.acceptProposedAction();
			
		}
		//MotifIOTools.writeMotifSetXML(, new Motif[] {nmicaMotif.motif});
		//System.out.println("Drag enter.");
		
		super.dragEnterEvent(e);
	}
	
	protected void dragMoveEvent(QDragMoveEvent e) {
		if (e.mimeData().hasFormat("xml/xms")) {
			e.setDropAction(Qt.DropAction.MoveAction);
			e.acceptProposedAction();
		} else{
			e.ignore();
		}
		
		super.dragMoveEvent(e);
	}
	
	//FIXME: Correct the removeItem() issue here
	protected void dropEvent(QDropEvent e) {
		if (e.source() == this) {
			e.ignore();
			super.dropEvent(e);
			return;
		}
		
		
		
		if (e.mimeData().hasFormat("xml/xms")) {
			QMimeData mime = e.mimeData();
			QByteArray data = mime.data("xml/xms");
			if (e.source() instanceof LogoView) {
				LogoView sourceWidget = ((LogoView)e.source());
				QMotif oldSourceMotif = sourceWidget.getMotif();
				sourceWidget.removeDistributionGraphicsItems();
				sourceWidget.setMotif(this.motif);
				if (this.parent() instanceof LabelledLogoView && 
						e.source().parent() instanceof LabelledLogoView) {	
						LabelledLogoView srcLLV = (LabelledLogoView) e.source().parent();
						srcLLV.getMotifLabelLineEdit().setMotif(this.motif);
						
				}
			}
			Motif[] motifs = null;
			try {
				motifs = MotifIOTools
							.loadMotifSetXML(
								new ByteArrayInputStream(data.toByteArray()));
				
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			if ((motifs != null) && (motifs.length > 0)) {
				removeDistributionGraphicsItems();
				QMotif newQM = new QMotif(this,motifs[0]);
				
				this.setMotif(newQM);
				LabelledLogoView thisLLV = (LabelledLogoView) this.parent();
				thisLLV.getMotifLabelLineEdit().setMotif(newQM);
				e.accept();
				update();
			} else {
				System.err.println("No motifs could be read from the stream.");
				return;
			}
		}
		super.dropEvent(e);
	}
	
	protected void mouseReleaseEvent(QMouseEvent e) {
		//repaint();
		super.mouseReleaseEvent(e);
	}
	
	protected void toggleSelectionAtPosition(QPoint p) {
		List<QGraphicsItemInterface>items = items(p);
		
		/*
		for(QGraphicsItemInterface item : items) {
			if (item != null && item.isEnabled())
				if (item instanceof DistributionItemIface) {
					DistributionItemIface gi = (DistributionItemIface)item;
					gi.getDist().toggleSelected(); break;}
		}
		
		repaint();*/
		
	}
	
	protected void toggleHighlightingAtPosition(QPoint p) {
		List<QGraphicsItemInterface>items = items(p);
		
		for(QGraphicsItemInterface item : items) {
			if (item != null && item.isEnabled())
				if (item instanceof DistributionItemIface) {
					DistributionItemIface gi = (DistributionItemIface)item;
					gi.getDist().toggleHighlighted();
					break;}
		}
		
		update();
	}
	
	protected void toggleSelectionAndHighlightingAtPosition(QPoint p) {
		List<QGraphicsItemInterface>items = items(p);
		
		for(QGraphicsItemInterface item : items) {
			if (item != null)
				if (item instanceof DistributionItemIface) {
					DistributionItemIface gi = (DistributionItemIface)item;
					gi.getDist().toggleSelected();
					gi.getDist().toggleHighlighted();
					break;}
		}
		
		update();
	}
	
	private DistributionItemIface itemAtPosition(QPoint p) {
		List<QGraphicsItemInterface>items = items(p);
		
		for(QGraphicsItemInterface item : items)
			if (item != null)
				if (item instanceof DistributionItemIface)
					return (DistributionItemIface)item;
					
		return null;
	}

	
	/**
	 * @return the lastSelectedColumn
	 */
	public int getLastSelectedColumn() {
		return lastSelectedColumn;
	}

	/**
	 * @param lastSelectedColumn the lastSelectedColumn to set
	 */
	public void setLastSelectedColumn(int lastSelectedColumn) {
		this.lastSelectedColumn = lastSelectedColumn;
	}
	
	protected int wmColumn(QPoint p) {
		List<QGraphicsItemInterface> items = items(p);
		if (items != null)
			for (QGraphicsItemInterface item : items)
				if (item instanceof DistributionItemIface)
					return this.motif.dists().indexOf(((DistributionItemIface)item).getDist());
		
		return -1;
	}


	public void toggleSelection(int i) {
		motif.toggleSelection(i);
		repaint();
	}

	public void keyPressEvent(QKeyEvent e) {
		/*
		MotifSetView msetWidget = motifSetView();
		if (msetWidget != null)
			msetWidget.keyPressEvent(e);
		else {
			super.keyPressEvent(e);
			if (Qt.Key.resolve(e.key()) == Qt.Key.Key_Left)
				moveToLeft();
			else if (Qt.Key.resolve(e.key()) == Qt.Key.Key_Right)
				moveToRight();
		}
		
		super.keyPressEvent(e);
		*/
	}
	
	protected void enterEvent(QEvent e) {
		borderItem.setPen(borderHighlightPen);
		
		super.enterEvent(e);
	}
	
	protected void leaveEvent(QEvent e) {
		borderItem.setPen(borderNormalPen);
		
		super.leaveEvent(e);
	}
	
	/*
	private boolean isWithinWidgetRect(QPoint p) {
		System.out.println(p.x() + "," + p.y());
		//System.out.println(rect().width() + "," + rect().height());
		
		return ((p.x() >= 0) && (p.x() <= rect().width()) &&
				(p.y() >= 0) && (p.y() <= rect().height()));
	}
	*/
	
	/*
	public void moveToRight() {
		System.err.println("Move to right");
		if (motifSetView() != null) {
			motifSetView().getUndoStack().push(
				new ShiftCommand(motifSetView().getSelectedMotifs(),1,null));
		} else {
			moveBy(1);
		}
	}
	
	//TODO: Select same column for all motifs

	public void moveToLeft() {
		System.err.println("Move to left");
		if (motifSetView() != null) {
			motifSetView().getUndoStack().push(
				new ShiftCommand(motifSetView().getSelectedMotifs(),-1,null));
		} else {
			moveBy(-1);
		}
	}*/
	
	private void moveItemsBy(int i) {
		for (QGraphicsItemInterface item : items())
			if (item instanceof MotifBoundingBox)
				item.moveBy(blockWidth() * i, 0);
	}
	
	public void moveBy(int i) {
		System.out.println(this.getMotif().getNmicaMotif().getName() + " i:" + i);
		moveItemsBy(i);
		motif.moveBy(i);
		repaint();
	}
	
	public void moveTo(int i) {
		for (QGraphicsItemInterface item : items())
			if (item instanceof MotifBoundingBox)
				item.moveBy(blockWidth() * (motif.getOffset() - i), 0);
		
		motif.moveTo(i);
	}
	
	/*
	private void toggleHighlightingForSelectedColumns() {
		
	}
	
	private void removeSelectedColumns() {
		
	}*/
	

	public void paint(QPainter painter) {
		painter.setRenderHint(QPainter.RenderHint.Antialiasing);
	}
	
	/*
	protected void paintEvent(QPaintEvent paint) {
		QPainter painter = new QPainter(this);
		this.paint(painter);
	}*/

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
	 * @return the nmicaMotif
	 */
	public QMotif getMotif() {
		return motif;
	}

	private void emptyScene() {
		for (QGraphicsItemInterface item : scene().items())
			scene().removeItem(item);
		//scene.addRect(new QRectF(rect()));
	}
	
	/**
	 * @param nmicaMotif the nmicaMotif to set
	 */
	public void setMotif(QMotif motif) {
		this.motif = motif;
		if (motif != null) {
			motif.setupDists();
			motif.setParent(this);
			if (motif.getHorizontalOffset() == 0) //if the motif offset hasn't been set up yet
				motif.setHorizontalOffset(xOffset);
			
			if (!motif.isMetaMotif())
				this.setUpLogo(motif);
			else
				if (motif.isMetaMotif()) {
					setUpLogo(motif);
					QMotif[] qmotifs = motif.sampleMotifsFromMetaMotif(motif,"", SAMPLE_NUM);
					for (int i=0; i < qmotifs.length; i++)
						setUpLogo(qmotifs[i]);
				}
			
			/*if (!offsetApplied) {
				System.out.println("Offset hasn't been applied");
				moveBy(motif.getOffset());
				offsetApplied = true;
			} else {*/
				//System.out.println("Offset has been applied");
				moveItemsBy(motif.getOffset());
			//}
		}
		else
			emptyScene();
	}

	public void removeDistributionGraphicsItems() {
		for (QGraphicsItemInterface qi : this.scene().items())
			if (qi instanceof DistributionItemIface && qi.scene() == this.scene())
				this.scene().removeItem(qi);
	}

	public static void main(String args[]) {
		Motif[] motifs = null;
		try {
			MetaMotif[] mms = MetaMotifIOTools.loadMetaMotifSetXML(
					new FileInputStream(
							"/Users/mz2/workspace/NestedMICA/metamotifs/sim/random5_varalpha.xms"));
			
			motifs = MetaMotifIOTools.metaMotifSetToMotifSet(mms);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (motifs == null)
			System.exit(1);
		
		Motif m = motifs[0];
		QMotif qm = new QMotif(m);
		QApplication.initialize(args);
		QWidget widget = new LogoView(null, 
								new QSize((int)MOTIF_WIDTH,
								(int)MOTIF_HEIGHT),
								qm,
								LogoView.DEFAULT_MAX_COLS, 
								LogoView.DEFAULT_X_OFFSET, true);
		widget.show();
		//widget.resize(500,100);
		QApplication.exec();
	}

	//TODO: Remove this hack. 
	
	/* At the moment the containing motif set widget can be only found
	 * if the object's parent or grandparent is the motif set widget.
	 * 
	 * You should consider handling communication between MotifLogoWidget 
	 * and its containing MotifSetWidget with signals and slots...
	 */
	public MotifSetView motifSetView() {
		QObject qobj = parent();
		if (qobj != null)
			if (qobj instanceof MotifSetView)
				return (MotifSetView)qobj;
			else if ((qobj.parent() != null) && 
					 (qobj.parent() instanceof MotifSetView))
				return (MotifSetView)qobj.parent();
		
		return null;
	}
	
	public QGraphicsRectItem getBorderItem() {
		return this.borderItem;
	}


	/**
	 * @return the lineEdit
	 */
	public MotifLabelLineEdit getLineEdit() {
		return lineEdit;
	}


	/**
	 * @param lineEdit the lineEdit to set
	 */
	public void setLineEdit(MotifLabelLineEdit lineEdit) {
		this.lineEdit = lineEdit;
	}
	
}
