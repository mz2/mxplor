package uk.ac.sanger.motifxplorer.cmd;

import java.util.List;

import net.derkholm.nmica.seq.WmTools;

import org.biojava.bio.Annotation;
import org.biojava.bio.symbol.IllegalAlphabetException;

import uk.ac.sanger.motifxplorer.ui.model.QMotif;
import uk.ac.sanger.motifxplorer.ui.widget.LogoView;

import com.trolltech.qt.gui.QUndoCommand;

//FIXME: Move and invert the annotations (motif regions) too upon reverse complementing!
public class ReverseComplementCommand extends QUndoCommand {
	private List<QMotif> motifs;
	
	public ReverseComplementCommand(List<QMotif> motifs, QUndoCommand parent) {
		super(parent);
		this.motifs = motifs;
		this.setText("Undo/redo rev. complement");
	}
	
	private void reverseComplement(List<QMotif> motifs) throws IllegalAlphabetException {
		for (QMotif motif : motifs) {
			motif.getNmicaMotif().setWeightMatrix(WmTools.reverseComplement(motif.getNmicaMotif().getWeightMatrix()));
			motif.setupDists();
			if (motif.parent() instanceof LogoView) {
				LogoView parentWidget = (LogoView)motif.parent();
				parentWidget.removeDistributionGraphicsItems();
				//parentWidget.setUpLogo(motif);
				
				//motif.getOffset();
				//motif.getNmicaMotif().getAnnotation().setProperty("offset", offset);
				int offset = motif.getOffset();
				System.out.println(offset);
				parentWidget.setMotif(motif);
				//parentWidget.moveTo(motif.getOffset());
				System.out.println(motif.getOffset());
				parentWidget.repaint();
			}
		}
	}
	
	public void redo() {
		try {
			reverseComplement(motifs);
		} catch (IllegalAlphabetException e) {
			e.printStackTrace();
		}
	}
	
	public void undo() {
		redo(); //heh
	}
}
