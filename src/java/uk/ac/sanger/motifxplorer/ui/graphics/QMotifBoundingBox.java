package uk.ac.sanger.motifxplorer.ui.graphics;

import uk.ac.sanger.motifxplorer.ui.model.QMotif;

import com.trolltech.qt.core.QRectF;
import com.trolltech.qt.gui.QGraphicsItemInterface;
import com.trolltech.qt.gui.QGraphicsRectItem;
import com.trolltech.qt.gui.QGraphicsScene;

public class QMotifBoundingBox extends QGraphicsRectItem {
	private QMotif motif;
	
	public QMotifBoundingBox(QMotif motif) {
		super();
		this.motif = motif;
	}
	
	public QMotif motif() {
		return motif;
	}
	
	public QMotifBoundingBox(QMotif motif, QGraphicsItemInterface arg0) {
		super(arg0);
		this.motif = motif;
	}

	public QMotifBoundingBox(QMotif motif,QRectF arg0) {
		super(arg0);
		this.motif = motif;
	}

	public QMotifBoundingBox(QMotif motif, QPrivateConstructor arg0) {
		super(arg0);
		this.motif = motif;
	}

	public QMotifBoundingBox(QMotif motif,QGraphicsItemInterface arg0, QGraphicsScene arg1) {
		super(arg0, arg1);
		this.motif = motif;
	}

	public QMotifBoundingBox(QMotif motif, QRectF arg0, QGraphicsItemInterface arg1) {
		super(arg0, arg1);
		this.motif = motif;
	}

	public QMotifBoundingBox(QMotif motif, QRectF arg0, QGraphicsItemInterface arg1,
			QGraphicsScene arg2) {
		super(arg0, arg1, arg2);
		this.motif = motif;
	}

	public QMotifBoundingBox(QMotif motif, double arg0, double arg1, double arg2, double arg3) {
		super(arg0, arg1, arg2, arg3);
		this.motif = motif;
	}

	public QMotifBoundingBox(QMotif motif, double arg0, double arg1, double arg2,
			double arg3, QGraphicsItemInterface arg4) {
		super(arg0, arg1, arg2, arg3, arg4);
		this.motif = motif;
	}

	public QMotifBoundingBox(QMotif motif, double arg0, double arg1, double arg2,
			double arg3, QGraphicsItemInterface arg4, QGraphicsScene arg5) {
		super(arg0, arg1, arg2, arg3, arg4, arg5);
		this.motif = motif;
	}

}
