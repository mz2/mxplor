package uk.ac.sanger.motifxplorer.ui.graphics;

import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QBrush;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QPen;

import uk.ac.sanger.motifxplorer.ui.model.QMotif;

public class ScoredMotifRegion extends SelectableMotifRegion {
	private double score;
	
	private static final QPen defaultUnkeptPen = new QPen(new QColor(0,176,0,100),2,
			Qt.PenStyle.SolidLine,
			Qt.PenCapStyle.RoundCap,
			Qt.PenJoinStyle.RoundJoin);
	private static final QPen defaultKeptPen = new QPen(new QColor(0,176,0,100),4,
			Qt.PenStyle.SolidLine,
			Qt.PenCapStyle.RoundCap,
			Qt.PenJoinStyle.RoundJoin);

	private QPen uPen = defaultUnkeptPen;
	private QPen kPen = defaultKeptPen;
	
	private static final QBrush defaultUnselectedBrush = new QBrush(new QColor(255,100,200,20));
	private static final QBrush defaultSelectedBrush = new QBrush(new QColor(255,120,200,100));

	private QBrush unselectedBrush = defaultUnselectedBrush;
	private QBrush selectedBrush = defaultSelectedBrush;
	
	protected QBrush unselectedBrush() {
		return unselectedBrush;
	}
	
	protected QBrush selectedBrush() {
		return selectedBrush;
	}
	
	protected void setUnkeptPen(QPen pen) {
		this.uPen = pen;
	}
	
	protected QPen keptPen() {
		return this.kPen;
	}
	
	/*
	protected QBrush defaultBrush() {
		QBrush brush = new QBrush(defaultBrush);
		brush.color().setAlphaF(score*10);
		return brush;
	}
	
	protected QPen defaultPen() {
		QPen pen = new QPen(defaultPen);
		pen.color().setAlphaF(score*200);
		return pen;
	}*/
	
	public ScoredMotifRegion(QMotif m, int begin, int length, double score) {
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
