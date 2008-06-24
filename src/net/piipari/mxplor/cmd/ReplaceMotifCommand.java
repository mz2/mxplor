package net.piipari.mxplor.cmd;

import net.piipari.mxplor.ui.model.QMotif;
import net.piipari.mxplor.ui.widget.LogoView;

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
