package uk.ac.sanger.motifxplorer.ui.graphics;

import uk.ac.sanger.motifxplorer.ui.model.QDistribution;
import uk.ac.sanger.motifxplorer.ui.widget.DistributionItemIface;

import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QBrush;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QGraphicsItemInterface;
import com.trolltech.qt.gui.QGraphicsPathItem;
import com.trolltech.qt.gui.QGraphicsSceneMouseEvent;
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
	
	private void commonInit(boolean acceptsHover) {
		//setBrush(normalBrush);
		setAcceptsHoverEvents(acceptsHover);
		if (acceptsHover) {
			setAcceptedMouseButtons(new Qt.MouseButtons(Qt.MouseButton.RightButton, Qt.MouseButton.LeftButton));
			setEnabled(true);
		} else {
			setAcceptedMouseButtons(new Qt.MouseButtons(Qt.MouseButton.NoButton));
			setEnabled(false);
		}
	}
	
	public void mousePressEvent(QGraphicsSceneMouseEvent event) {
		System.out.println("SymbolGraphicsItem received a mouse press event!");
	}

	public SymbolGraphicsItem(boolean acceptsHover) {
		super();
		commonInit(acceptsHover);
	}

	public SymbolGraphicsItem(QDistribution dist, QGraphicsItemInterface arg0, boolean acceptsHover) {
		super(arg0);
		this.dist = dist;
		commonInit(acceptsHover);
	}

	public SymbolGraphicsItem(QDistribution dist, QPainterPath arg0, boolean acceptsHover) {
		super(arg0);
		this.dist = dist;
		commonInit(acceptsHover);
	}

	public SymbolGraphicsItem(QDistribution dist, QPainterPath arg0,
			QGraphicsItemInterface arg1, boolean acceptsHover) {
		super(arg0, arg1);
		this.dist = dist;
		commonInit(acceptsHover);
	}

	public QDistribution getDist() {
		return dist;
		
	}

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
