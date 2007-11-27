package uk.ac.sanger.motifxplorer.ui.graphics;

import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QBrush;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QPen;

import uk.ac.sanger.motifxplorer.ui.model.QMotif;

public class ScoredAnnotatedRegion extends AnnotatedRegion {
	private double score;
	
	private static QBrush defaultBrush = new QBrush(new QColor(255,200,200,20));
	private static QPen defaultPen = new QPen(new QColor(255,150,150,255),2,
			Qt.PenStyle.SolidLine,
			Qt.PenCapStyle.RoundCap,
			Qt.PenJoinStyle.RoundJoin);
	
	protected QBrush defaultBrush() {
		QBrush brush = new QBrush(defaultBrush);
		brush.color().setAlphaF(score*10);
		return brush;
	}
	
	protected QPen defaultPen() {
		QPen pen = new QPen(defaultPen);
		pen.color().setAlphaF(score*200);
		return pen;
	}
	
	public ScoredAnnotatedRegion(QMotif m, int begin, int length, double score) {
		super(m,begin,length);
		this.score = score;
	}

	public double score() {
		return this.score;
	}
	
	public void setScore(double s) {
		this.score = s;
	}
	
}
