package uk.ac.sanger.motifxplorer.ui.graphics;

import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QBrush;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QPen;

import uk.ac.sanger.motifxplorer.ui.model.QMotif;

public class ScoredMotifRegion extends SelectableMotifRegion {
	private double score;
	
	private static final QColor DEFAULT_COLOR = new QColor(100,255,100,50);	
	private static final QBrush DEFAULT_BRUSH = new QBrush(DEFAULT_COLOR);
	public ScoredMotifRegion(MotifRegionSet mset, QMotif m, int begin, int length, double score) {
		super(mset, m, begin, length);
		this.score = score;
		setBrushes(DEFAULT_COLOR);
		setPens(DEFAULT_COLOR);
	}

	public double score() {
		return this.score;
	}
	
	public void setScore(double s) {
		this.score = s;
	}
}
