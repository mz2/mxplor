package uk.ac.sanger.motifxplorer.ui.graphics;

import uk.ac.sanger.motifxplorer.ui.model.QMotif;

import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QBrush;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QPen;

public class MotifBoundingBox extends MotifRegion {
	private static final QBrush defaultBrush = new QBrush(new QColor(0,0,0,0));
	private static final QPen keptPen = new QPen(new QColor(0,0,0,0),0,
			Qt.PenStyle.SolidLine,
			Qt.PenCapStyle.RoundCap,
			Qt.PenJoinStyle.RoundJoin);
	private static final QPen unkeptPen = new QPen(new QColor(0,0,0,0),0,
			Qt.PenStyle.SolidLine,
			Qt.PenCapStyle.RoundCap,
			Qt.PenJoinStyle.RoundJoin);
	
	protected QBrush normalBrush() {
		return defaultBrush;
	}
	
	@Override
	protected QPen keptPen() {
		return keptPen;
	}
	
	@Override
	protected QPen unkeptPen() {
		return unkeptPen;
	}
	
	
	
	public MotifBoundingBox(QMotif motif) {
		super(motif,0,motif.getNmicaMotif().getWeightMatrix().columns());
	}
}