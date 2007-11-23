package uk.ac.sanger.motifxplorer.cmd;

import java.util.ArrayList;
import java.util.List;

import uk.ac.sanger.motifxplorer.ui.model.QMotif;
import uk.ac.sanger.motifxplorer.ui.widget.MotifSetView;

import com.trolltech.qt.gui.QUndoCommand;

//FIXME: Make this work again!
public class RemoveMotifFromSetCommand extends QUndoCommand {
	private List<QMotif> motifsBeforeRemoval;
	private List<QMotif> motifsAfterRemoval;
	private MotifSetView setWidget;
	private QMotif motif;
	
	public RemoveMotifFromSetCommand(MotifSetView setWidget, QMotif m) {
		this.setWidget = setWidget;
		this.motif = m;
	}
	
	private int findRowForMotif(MotifSetView setWidget, QMotif m) {
		return setWidget.getMotifs().indexOf(m);
	}
	public void redo() {
		this.motifsBeforeRemoval = setWidget.getMotifs();
		this.motifsAfterRemoval = new ArrayList<QMotif>(motifsBeforeRemoval);
		this.motifsAfterRemoval.remove(findRowForMotif(setWidget, motif));
		//setWidget.setMotifs(motifsAfterRemoval);
	}
	
	public void undo() {
		//setWidget.setMotifs(motifsBeforeRemoval);
	}
}
