package uk.ac.sanger.motifxplorer.ui.graphics;

import uk.ac.sanger.motifxplorer.ui.model.QDistribution;
import uk.ac.sanger.motifxplorer.ui.widget.DistributionItemIface;

import com.trolltech.qt.core.QRectF;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QBrush;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QGraphicsItemInterface;
import com.trolltech.qt.gui.QGraphicsRectItem;
import com.trolltech.qt.gui.QGraphicsScene;
import com.trolltech.qt.gui.QMouseEvent;
import com.trolltech.qt.gui.QPen;

public class BoundaryItem extends QGraphicsRectItem implements DistributionItemIface {
	QDistribution dist;
	private static QBrush normalBrush, highlightBrush, selectedBrush;
	private static QPen normalPen, highlightPen, selectedPen;
	
	static {
		normalBrush = new QBrush(new QColor(255,255,255,0));
		normalPen = new QPen(
					new QColor(255,255,255,0),
					0,
					Qt.PenStyle.NoPen,
					Qt.PenCapStyle.RoundCap,
					Qt.PenJoinStyle.RoundJoin);
				
		highlightBrush = new QBrush(new QColor(255,255,255,0));
		highlightPen = new QPen(
					new QColor(255,255,255,0),
					0,
					Qt.PenStyle.NoPen,
					Qt.PenCapStyle.RoundCap,
					Qt.PenJoinStyle.RoundJoin);
					
		
	}
	
	private void commonInit() {
		setBrush(normalBrush);
		setPen(normalPen);
		setAcceptsHoverEvents(true);
	}
	
	public BoundaryItem() {
		super();
		commonInit();
	}

	public BoundaryItem(QDistribution dist, QGraphicsItemInterface arg0) {
		super(arg0);
		this.dist = dist;
		commonInit();
	}

	public BoundaryItem(QDistribution dist, QRectF arg0) {
		super(arg0);
		this.dist = dist;
		commonInit();
	}

	public BoundaryItem(QDistribution dist, QPrivateConstructor arg0) {
		super(arg0);
		this.dist = dist;
		commonInit();
	}

	public BoundaryItem(QDistribution dist, QGraphicsItemInterface arg0, QGraphicsScene arg1) {
		super(arg0, arg1);
		this.dist = dist;
		commonInit();
	}

	public BoundaryItem(QDistribution dist, QRectF arg0, QGraphicsItemInterface arg1) {
		super(arg0, arg1);
		this.dist = dist;
		commonInit();
	}

	public BoundaryItem(QDistribution dist, QRectF arg0, QGraphicsItemInterface arg1,
			QGraphicsScene arg2) {
		super(arg0, arg1, arg2);
		this.dist = dist;
		commonInit();
	}

	public BoundaryItem(QDistribution dist, double arg0, double arg1, double arg2, double arg3) {
		super(arg0, arg1, arg2, arg3);
		this.dist = dist;
	}

	public BoundaryItem(QDistribution dist, double arg0, double arg1, double arg2, double arg3,
			QGraphicsItemInterface arg4) {
		super(arg0, arg1, arg2, arg3, arg4);
		this.dist = dist;
		commonInit();
	}

	public BoundaryItem(QDistribution dist, double arg0, double arg1, double arg2, double arg3,
			QGraphicsItemInterface arg4, QGraphicsScene arg5) {
		super(arg0, arg1, arg2, arg3, arg4, arg5);
		this.dist = dist;
		commonInit();
	}

	public QDistribution getDist() {
		return dist;
	}
	
	public void selectionStateUpdated() {
		if (dist.isSelected()) {
			setBrush(BoundaryItem.highlightBrush);
		} else {
			setPen(BoundaryItem.normalPen);
			setBrush(BoundaryItem.normalBrush);
		}
	}
	
	public void highlightStateUpdated() {
		if (dist.isHighlighted()) {
			setPen(BoundaryItem.highlightPen);
			setBrush(BoundaryItem.highlightBrush);
		} else {
			setPen(BoundaryItem.normalPen);
			setBrush(BoundaryItem.normalBrush);
		}	
	}
	
}
