package uk.ac.sanger.motifxplorer.ui.widget;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import net.derkholm.nmica.motif.Motif;
import net.derkholm.nmica.motif.MotifIOTools;
import uk.ac.sanger.motifxplorer.ui.model.QMotif;

import com.trolltech.qt.core.QObject;
import com.trolltech.qt.core.QSize;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QSizePolicy;
import com.trolltech.qt.gui.QWidget;

//TODO: Change drag and drop such that one's actually dragging and dropping MotifLogoWithLabelWidgets
public class LabelledLogoWidget extends QWidget {
	private LogoView motifLogoWidget;
	private QLineEdit lineEdit;
	private boolean infoContentScale;
	
	public static final int DEFAULT_MIN_LINE_EDIT_WIDTH = 150;
	public static final int DEFAULT_MAX_LINE_EDIT_WIDTH = 200;
	
	public static final int DEFAULT_TOTAL_WIDGET_WIDTH = DEFAULT_MIN_LINE_EDIT_WIDTH + LogoView.MOTIF_WIDTH;
	public static final int DEFAULT_TOTAL_WIDGET_HEIGHT = (int)LogoView.MOTIF_HEIGHT * 4;

	public LabelledLogoWidget(QObject parent, QMotif m, int maxCols, boolean infoContentScale) {
		this.setLayout(new QHBoxLayout());
		this.setSizePolicy(QSizePolicy.Policy.Fixed, QSizePolicy.Policy.Fixed);
		this.infoContentScale = infoContentScale;
		//System.out.println("INFOCONTENTSCALE:" + infoContentScale);
		if (m != null) {
			//this.setParent(parent);
			lineEdit = new QLineEdit();
			lineEdit.setMinimumWidth(DEFAULT_MIN_LINE_EDIT_WIDTH);
			lineEdit.setMaximumHeight(LogoView.MOTIF_WIDTH);
			lineEdit.setSizePolicy(QSizePolicy.Policy.Preferred, QSizePolicy.Policy.Preferred);
			lineEdit.setParent(this);
			
			layout().addWidget(lineEdit);
			layout().setMargin(0);
			layout().setContentsMargins(0, 0, 0, 0);
			layout().setWidgetSpacing(0);
		}
		
		if (m != null)
			lineEdit.setText(m.getNmicaMotif().getName());

		motifLogoWidget = new LogoView(	this, 
											new QSize(LogoView.MOTIF_WIDTH, 
													LogoView.MOTIF_HEIGHT), 
											m, 
											maxCols, 
											LogoView.DEFAULT_X_OFFSET,
											infoContentScale);

		motifLogoWidget.setMinimumWidth(LogoView.MOTIF_WIDTH);
		motifLogoWidget.setMinimumHeight(LogoView.MOTIF_HEIGHT);
		motifLogoWidget.setMaximumHeight(LogoView.MOTIF_HEIGHT);
		motifLogoWidget.setSizePolicy(QSizePolicy.Policy.Preferred, QSizePolicy.Policy.Preferred);
		motifLogoWidget.setParent(this);
		layout().addWidget(motifLogoWidget);
	}
	
	public static void main(String args[]) {
		Motif[] motifs = null;
		try {
			motifs = MotifIOTools.loadMotifSetXML(new FileInputStream("/Users/mz2/workspace/NestedMICA/metamotifs/sim/random5_varalpha.xms"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (motifs == null)
			System.exit(1);
		
		Motif m = motifs[0];
		QMotif qm = new QMotif(m);
		QApplication.initialize(args);
		QWidget widget = new LabelledLogoWidget(null, qm, LogoView.DEFAULT_MAX_COLS, true);
		widget.show();
		//widget.resize(500,100);
		QApplication.exec();
	}

	/**
	 * @return the lineEdit
	 */
	public QLineEdit getLineEdit() {
		return lineEdit;
	}

	/**
	 * @param lineEdit the lineEdit to set
	 */
	public void setLineEdit(QLineEdit lineEdit) {
		this.lineEdit = lineEdit;
	}

	/**
	 * @return the motifLogoWidget
	 */
	public LogoView getMotifLogoWidget() {
		return motifLogoWidget;
	}

	/**
	 * @param motifLogoWidget the motifLogoWidget to set
	 */
	public void setMotifLogoWidget(LogoView motifLogoWidget) {
		this.motifLogoWidget = motifLogoWidget;
	}

}
