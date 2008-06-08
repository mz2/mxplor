package uk.ac.sanger.motifxplorer.ui.graphics;

import uk.ac.sanger.motifxplorer.ui.model.QMotif;

import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QBrush;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QPen;

public class MotifBoundingBox extends MotifRegion {
	private static final QColor DEFAULT_COLOR = new QColor(255,255,255,0);
	
	private static final QBrush DEFAULT_BRUSH = new QBrush(DEFAULT_COLOR);
	private static final QPen DEFAULT_KEPT_PEN = new QPen(DEFAULT_COLOR,0,
			Qt.PenStyle.SolidLine,
			Qt.PenCapStyle.RoundCap,
			Qt.PenJoinStyle.RoundJoin);
	private static final QPen unkeptPen = new QPen(DEFAULT_COLOR,0,
			Qt.PenStyle.SolidLine,
			Qt.PenCapStyle.RoundCap,
			Qt.PenJoinStyle.RoundJoin);
		
	public MotifBoundingBox(QMotif motif) {
		super(null, motif,0,motif.getNmicaMotif().getWeightMatrix().columns());
		setColor(DEFAULT_COLOR);
	}
	
	public QColor defaultBrushColor() {
		return DEFAULT_COLOR;
	}
	
	public QColor defaultPenColor() {
		return DEFAULT_COLOR;
	}
}