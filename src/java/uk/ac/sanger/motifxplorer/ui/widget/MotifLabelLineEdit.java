package uk.ac.sanger.motifxplorer.ui.widget;

import uk.ac.sanger.motifxplorer.ui.model.QMotif;

import com.trolltech.qt.gui.QBrush;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QPaintEvent;
import com.trolltech.qt.gui.QPainter;
import com.trolltech.qt.gui.QWidget;

public class MotifLabelLineEdit extends QLineEdit {
	private QBrush brush;
	private QMotif motif;
	
	/**
	 * @return the motif
	 */
	public QMotif getMotif() {
		return motif;
	}

	/**
	 * @param motif the motif to set
	 */
	public void setMotif(QMotif motif) {
		this.motif = motif;
	}

	private void init() {
		if (this.motif.color() != null)
			brush = new QBrush(new QColor(this.motif.color()));
	}
	
	public MotifLabelLineEdit(QMotif motif) {
		init();
	}

	public MotifLabelLineEdit(QMotif motif, QWidget arg0) {
		super(arg0);
		init();
	}

	public MotifLabelLineEdit(QMotif motif, String arg0) {
		super(arg0);
		init();
	}

	public MotifLabelLineEdit(QMotif motif, QPrivateConstructor arg0) {
		super(arg0);
		init();
	}

	public MotifLabelLineEdit(QMotif motif, String arg0, QWidget arg1) {
		super(arg0, arg1);
		init();
	}

	protected void paintEvent(QPaintEvent qpe) {
		super.paintEvent(qpe);
		if (motif != null && motif.color() != null) {
			QPainter p = new QPainter(this);
			p.setBrush(brush);
			p.drawRect(this.rect());
		}
	}
}
