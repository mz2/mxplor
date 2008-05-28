package uk.ac.sanger.motifxplorer.cmd;

import java.util.ArrayList;

import uk.ac.sanger.motifxplorer.ui.model.QMotif;
import uk.ac.sanger.motifxplorer.ui.widget.MotifSetView;

import com.trolltech.qt.gui.QUndoCommand;
import com.trolltech.qt.gui.QVBoxLayout;

//FIXME: Are you sure there are no mutable lists that have to be updated when removing/adding?
public class RemoveMotifCommand extends QUndoCommand {
	private int removedIndex;
	private QMotif removedMotif;
	
	private MotifSetView setWidget;
	
	public RemoveMotifCommand(MotifSetView setWidget, QMotif m) {
		this.setWidget = setWidget;
		removedMotif = m;
		removedIndex = findRowForMotif(setWidget, m);
	}
	
	private int findRowForMotif(MotifSetView setWidget, QMotif m) {
		return setWidget.getMotifs().indexOf(m);
	}
	public void redo() {
		setWidget.removeMotif(removedMotif);
		setWidget.repaint();
	}
	
	//FIXME: Reinsert the motif in the correct position in the layout
	public void undo() {
		setWidget.addMotif(removedMotif);
		setWidget.repaint();
	}
}
