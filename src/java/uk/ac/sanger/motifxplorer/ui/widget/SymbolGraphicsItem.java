package uk.ac.sanger.motifxplorer.ui.widget;

import uk.ac.sanger.motifxplorer.ui.model.QDistribution;

import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QBrush;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QGraphicsItemInterface;
import com.trolltech.qt.gui.QGraphicsPathItem;
import com.trolltech.qt.gui.QGraphicsScene;
import com.trolltech.qt.gui.QGraphicsSceneMouseEvent;
import com.trolltech.qt.gui.QKeyEvent;
import com.trolltech.qt.gui.QPainterPath;
import com.trolltech.qt.gui.QPen;

public class SymbolGraphicsItem extends QGraphicsPathItem implements DistributionItemIface, Comparable<SymbolGraphicsItem> {
	private QDistribution dist;
	private static QBrush highlightBrush;
	//private static QBrush normalBrush;
	private static QPen selectionPen;

	private QPen normalPen;
	private QBrush normalBrush;
	
	static {
		highlightBrush = new QBrush(new QColor(100,100,200,100));
		//normalBrush = new QBrush(new QColor(50,50,50,30));
		selectionPen = new QPen(
				new QColor(255,100,100,200),
				0.05,
				Qt.PenStyle.SolidLine,
				Qt.PenCapStyle.RoundCap,
				Qt.PenJoinStyle.RoundJoin);
		
			
	}
	
	private void commonInit() {
		//setBrush(normalBrush);
		setAcceptsHoverEvents(true);
	}
	public void mousePressEvent(QGraphicsSceneMouseEvent event) {
		System.out.println("SymbolGraphicsItem received a mouse press event!");
	}

	public SymbolGraphicsItem() {
		super();
	}

	public SymbolGraphicsItem(QDistribution dist, QGraphicsItemInterface arg0) {
		super(arg0);
		this.dist = dist;
		commonInit();
	}

	public SymbolGraphicsItem(QDistribution dist, QPainterPath arg0) {
		super(arg0);
		this.dist = dist;
		commonInit();
	}

	public SymbolGraphicsItem(QDistribution dist, QPainterPath arg0,
			QGraphicsItemInterface arg1) {
		super(arg0, arg1);
		this.dist = dist;
		commonInit();
	}

	public QDistribution getDist() {
		return dist;
		
	}

	/*
	public boolean isHighlightedDist() {
		return dist.isHighlighted();
	}

	public void setHighlightedDist(boolean bool) {
		setHighlightedDist(bool, false);
	}
	
	//TODO:Maybe these methods (the ones with relayToParent/relayToChildren) should be required by the interface too?
	public void setHighlightedDist(boolean bool, boolean relayToParent) {
		dist.setHighlighted(bool);
		System.out.println("Setting highlighted item");
		if (relayToParent && (parentItem() != null) && (parentItem() instanceof BoundaryItem)) {
			System.out.println(parentItem());
			BoundaryItem boundItem = (BoundaryItem)parentItem();
			boundItem.setHighlightedDist(bool, false); //don't want a cycle, hence false as the second arg
		}
	}

	public void toggleHighlightedDist() {
		toggleHighlightedDist(true);
	}
	
	public void toggleHighlightedDist(boolean relayToParent) {
		dist.setHighlighted(!dist.isHighlighted());
		setHighlightedDist(!isHighlightedDist(), relayToParent);
	}

	
	public void setSelectedDist(boolean bool) {
		setSelectedDist(bool, false);
	}
	
	public void setSelectedDist(boolean bool, boolean relayToParent) {
		dist.setSelected(bool);
		if (relayToParent && (parentItem() != null) && (parentItem() instanceof BoundaryItem)) {
			BoundaryItem boundItem = (BoundaryItem)parentItem();
			boundItem.setSelectedDist(bool, false); //don't want a cycle, hence false as the second arg
		}
	}
	
	public boolean isSelectedDist() {
		return dist.isSelected();
	}
	
	public void toggleSelectedDist() {
		toggleSelectedDist(true);
	}
	
	public void toggleSelectedDist(boolean relayToParent) {
		dist.setSelected(!dist.isSelected());
		setSelectedDist(!isSelectedDist(), relayToParent);
	}*/

	public int compareTo(SymbolGraphicsItem o) {
		if (this.equals(o))
			return 0;
		else if (this.hashCode() > 0)
			return -1;
		else
			return 1;
	}
	public void selectionStateUpdated() {
		if (dist.isSelected()) {
			//prevNormalPen = this.pen();
			setPen(SymbolGraphicsItem.selectionPen);
		} else {
			setPen(normalPen);
			
		}
	}
	
	public int column() {
		return this.dist.getColumn();
	}
	public void highlightStateUpdated() {
		if (dist.isHighlighted()) {
			//prevNormalBrush = this.brush();
			setBrush(SymbolGraphicsItem.highlightBrush);
		} else {
			setBrush(normalBrush);
		}
	}
	/**
	 * @return the normalPen
	 */
	public QPen getNormalPen() {
		return normalPen;
	}
	/**
	 * @param normalPen the normalPen to set
	 */
	public void setNormalPen(QPen normalPen) {
		this.normalPen = normalPen;
	}
	/**
	 * @return the normalBrush
	 */
	public QBrush getNormalBrush() {
		return normalBrush;
	}
	/**
	 * @param normalBrush the normalBrush to set
	 */
	public void setNormalBrush(QBrush normalBrush) {
		this.normalBrush = normalBrush;
	}
}
