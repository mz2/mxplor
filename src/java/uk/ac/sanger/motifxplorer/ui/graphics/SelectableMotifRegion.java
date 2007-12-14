package uk.ac.sanger.motifxplorer.ui.graphics;

import uk.ac.sanger.motifxplorer.ui.model.QMotif;

import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QBrush;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QGraphicsItem;
import com.trolltech.qt.gui.QGraphicsSceneHoverEvent;
import com.trolltech.qt.gui.QGraphicsSceneMouseEvent;
import com.trolltech.qt.gui.QPen;

public class SelectableMotifRegion extends MotifRegion {
	private boolean selected;
	
	private static final QColor DEFAULT_UNSELECTED_COLOR = new QColor(120,200,255,20);
	private static final QColor DEFAULT_SELECTED_COLOR = new QColor(150,230,255,50);
	private static final QColor DEFAULT_PEN_COLOR = new QColor(120,200,255,20);

	private static final QBrush DEFAULT_SELECTED_BRUSH = new QBrush(DEFAULT_SELECTED_COLOR);
	protected QBrush selectedBrush = DEFAULT_SELECTED_BRUSH;
	
	public SelectableMotifRegion(MotifRegionSet mset, QMotif m, int begin, int length) {
		super(mset, m, begin, length);
		setAcceptedMouseButtons(new Qt.MouseButtons(
									Qt.MouseButton.LeftButton, 
									Qt.MouseButton.RightButton));
		
		setBrushes(DEFAULT_UNSELECTED_COLOR);
		setPens(DEFAULT_PEN_COLOR);
		this.kept = true;
		setBrush(normalBrush());
		setPen(keptPen());
		
		setAcceptsHoverEvents(true);
		
		/*setFlag(
			QGraphicsItem.GraphicsItemFlag.ItemIsSelectable, 
			true);*/ //aren't using the QT QGraphicsView selection methods (how to customise its look-and-feel?)
		setEnabled(true);
	}
	
	public void mousePressEvent(QGraphicsSceneMouseEvent event) {
		if (event.modifiers().isSet(Qt.KeyboardModifier.ControlModifier))
			toggleSelectedRegion();
		
		
		//FIXME: Should only pick the highermost of overlapping selectable regions
		super.mousePressEvent(event);
	}
	
	//TODO: Highlight
	public void hoverEnterEvent(QGraphicsSceneHoverEvent event) {
		super.hoverEnterEvent(event);
		
	}

	//TODO: Unhighlight
	public void hoverLeaveEvent(QGraphicsSceneHoverEvent event) {
		super.hoverLeaveEvent(event);
	}
	
	/**
	 * @param selected the selected to set
	 */
	public void setSelectedRegion(boolean selected) {
		this.selected = selected;
		if (this.selected) {
			setBrush(selectedBrush());
		} else {
			setBrush(normalBrush());
		}
		this.setSelected(selected);
	}

	/**
	 * @return the selected
	 */
	public boolean isSelectedRegion() {
		return selected;
	}
	
	public void toggleSelectedRegion() {
		setSelectedRegion(!isSelectedRegion());
		System.out.println("Is selected:" + isSelectedRegion());
	}
	
	protected QBrush selectedBrush() {
		return selectedBrush;
	}
	
	public void setBrushes(QColor color) {
		if (color == null) {
			color = DEFAULT_UNSELECTED_COLOR;
		}
		normalBrush = new QBrush(color);
		
		QColor selCol = new QColor(color);
		selCol.setAlpha(color.alpha() + 50);
		this.selectedBrush = new QBrush(selCol);
		if (selected)
			setBrush(selectedBrush);
		else 
			setBrush(normalBrush);
	}
}
