package uk.ac.sanger.motifxplorer.ui.graphics;

import uk.ac.sanger.motifxplorer.ui.model.QMotif;

import com.trolltech.qt.core.QRectF;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QBrush;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QGraphicsItemInterface;
import com.trolltech.qt.gui.QGraphicsRectItem;
import com.trolltech.qt.gui.QGraphicsScene;
import com.trolltech.qt.gui.QPen;

public class QMotifBoundingBox extends AnnotatedRegion {
	private static QBrush defaultBrush = new QBrush(new QColor(0,0,0,0));
	private static QPen defaultPen = new QPen(new QColor(0,0,0,0),0,
			Qt.PenStyle.SolidLine,
			Qt.PenCapStyle.RoundCap,
			Qt.PenJoinStyle.RoundJoin);
	
	protected QBrush defaultBrush() {
		return defaultBrush;
	}
	
	protected QPen defaultPen() {
		return defaultPen;
	}
	
	
	public QMotifBoundingBox(QMotif motif) {
		super(motif,0,motif.getNmicaMotif().getWeightMatrix().columns()-1);
	}
}
