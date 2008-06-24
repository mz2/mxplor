package net.piipari.mxplor.ui.graphics;

import net.piipari.mxplor.ui.model.QDistribution;
import net.piipari.mxplor.ui.widget.DistributionItemIface;

import com.trolltech.qt.core.QRectF;
import com.trolltech.qt.core.Qt;

import com.trolltech.qt.gui.QBrush;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QGraphicsRectItem;
import com.trolltech.qt.gui.QGraphicsSceneMouseEvent;
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
	
	public void mousePressEvent(QGraphicsSceneMouseEvent event) {
		if (!event.modifiers().isSet(Qt.KeyboardModifier.ControlModifier))
			getDist().toggleSelected();
		
		super.mousePressEvent(event);
	}
	
	public BoundaryItem(QDistribution dist, QRectF arg0) {
		super(arg0);
		this.dist = dist;
		setBrush(normalBrush);
		setPen(normalPen);
		if (dist.motif().isSample()) {
			setAcceptsHoverEvents(false);
			setAcceptedMouseButtons(new Qt.MouseButtons(Qt.MouseButton.NoButton));
		} else {
			setAcceptsHoverEvents(true);
			setAcceptedMouseButtons(new Qt.MouseButtons(
												Qt.MouseButton.LeftButton,
												Qt.MouseButton.RightButton));
		}
		dist.setBoundItem(this);
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
