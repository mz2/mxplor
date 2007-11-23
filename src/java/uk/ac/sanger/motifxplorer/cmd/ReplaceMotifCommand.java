package uk.ac.sanger.motifxplorer.cmd;

import uk.ac.sanger.motifxplorer.ui.model.QMotif;
import uk.ac.sanger.motifxplorer.ui.widget.LogoView;

import com.trolltech.qt.core.QObject;
import com.trolltech.qt.gui.QUndoCommand;

public class ReplaceMotifCommand extends QUndoCommand {
	private LogoView widget;
	private QMotif newMotif;
	private QMotif oldMotif;
	
	public ReplaceMotifCommand(LogoView widget, QMotif newMotif, QUndoCommand parent) {
		super(parent);
		this.newMotif = newMotif;
		this.oldMotif = widget.getMotif();
		

	}
	
	public void undo() {
		this.widget.setMotif(this.oldMotif);
		widget.update();
	}
	
	public void redo() {
		this.widget.setMotif(this.newMotif);
		widget.update();
	}
}
