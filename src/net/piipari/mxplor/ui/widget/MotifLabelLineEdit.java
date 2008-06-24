package net.piipari.mxplor.ui.widget;

import net.piipari.mxplor.ui.model.QMotif;

import com.trolltech.qt.gui.QBrush;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QPaintEvent;
import com.trolltech.qt.gui.QPainter;
import com.trolltech.qt.gui.QWidget;

public class MotifLabelLineEdit extends QLineEdit {
	private static final int ALPHA_OFFSET = 80;
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
		init(motif);
	}

	private void init(QMotif motif) {
		this.motif = motif;
		setText(motif.getNmicaMotif().getName());
		//System.out.println(text());
		if (this.motif != null && this.motif.color() != null) {
			QColor color = new QColor(this.motif.color());
			//System.out.println(color);
			color.setAlpha(ALPHA_OFFSET);
			brush = new QBrush(color);
		}
		repaint();
	}
	
	public MotifLabelLineEdit(QMotif motif) {
		init(motif);
	}

	public MotifLabelLineEdit(QMotif motif, QWidget arg0) {
		super(arg0);
		init(motif);
	}

	public MotifLabelLineEdit(QMotif motif, String arg0) {
		super(arg0);
		init(motif);
	}

	public MotifLabelLineEdit(QMotif motif, QPrivateConstructor arg0) {
		super(arg0);
		init(motif);
	}

	public MotifLabelLineEdit(QMotif motif, String arg0, QWidget arg1) {
		super(arg0, arg1);
		init(motif);
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
