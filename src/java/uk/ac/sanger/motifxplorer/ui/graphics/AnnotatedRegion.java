package uk.ac.sanger.motifxplorer.ui.graphics;

import uk.ac.sanger.motifxplorer.ui.model.QDistribution;
import uk.ac.sanger.motifxplorer.ui.model.QMotif;

import com.trolltech.qt.core.QPointF;
import com.trolltech.qt.core.QRectF;
import com.trolltech.qt.core.QSizeF;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QBrush;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QGraphicsRectItem;
import com.trolltech.qt.gui.QPen;

public class AnnotatedRegion extends QGraphicsRectItem {
	private QMotif motif;
	private int begin;
	private int length;
	
	private static QBrush defaultBrush = new QBrush(new QColor(255,100,100,20));
	private static QPen defaultPen = new QPen(new QColor(255,150,150,255),2,
			Qt.PenStyle.SolidLine,
			Qt.PenCapStyle.RoundCap,
			Qt.PenJoinStyle.RoundJoin);
	
	protected QBrush defaultBrush() {
		return defaultBrush;
	}
	
	protected QPen defaultPen() {
		return defaultPen;
	}
	
	
	public QMotif getMotif() {
		return this.motif;
	}
	
	public AnnotatedRegion(QMotif m, int begin, int length) {
		init(m, begin, length);
		int end = begin + length;
		QDistribution b = m.dists().get(begin);
		QDistribution e = m.dists().get(end);

		if (b.getBoundItem() != null && e.getBoundItem() != null) {
			updateLocation();
		}
	}
	
	public void updateLocation() {
		QDistribution b = motif.dists().get(begin);
		QDistribution e = motif.dists().get(begin + length);
		QSizeF rectSize = new QSizeF(
				e.getBoundItem().boundingRect().right() - 
				b.getBoundItem().boundingRect().left(),
				e.getBoundItem().boundingRect().height());
		
		setRect(new QRectF(new QPointF(b.getBoundItem().pos().x(),0), rectSize));
	}
	
	private void init(QMotif m, int begin, int length) {
		this.motif = m;
		this.begin = begin;
		this.length = length;
		
		setBrush(defaultBrush());
		setPen(defaultPen());
	}
}
