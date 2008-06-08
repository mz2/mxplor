package uk.ac.sanger.motifxplorer.cmd;

import java.util.List;

import uk.ac.sanger.motifxplorer.ui.model.QMotif;
import uk.ac.sanger.motifxplorer.ui.widget.LogoView;

import com.trolltech.qt.core.QObject;
import com.trolltech.qt.gui.QUndoCommand;

public class ShiftCommand extends QUndoCommand {
	private int movedBy;
	private List<QMotif> motifs;
	
	public ShiftCommand(List<QMotif> motifs, int moveBy, QUndoCommand parent) {
		super(parent);
		this.motifs = motifs;
		this.movedBy = moveBy;
		
		//moveBy(this.movedBy);
	}
	
	private void moveBy(int i) {
		for (QMotif m : motifs) {
			if (m.parent() instanceof LogoView)
				((LogoView)m.parent()).moveBy(i);
			else
				m.moveBy(i);
		}
	}
	
	public void redo() {
		moveBy(movedBy);
	}
	
	public void undo() {
		moveBy(-movedBy);
	}
}
