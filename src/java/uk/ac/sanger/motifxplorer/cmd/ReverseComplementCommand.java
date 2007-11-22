package uk.ac.sanger.motifxplorer.cmd;

import java.util.List;

import net.derkholm.nmica.motif.Motif;
import net.derkholm.nmica.seq.WmTools;

import org.biojava.bio.dp.WeightMatrix;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.MotifTools;

import uk.ac.sanger.motifxplorer.ui.model.QMotif;
import uk.ac.sanger.motifxplorer.ui.widget.LogoWidget;

import com.trolltech.qt.core.QObject;
import com.trolltech.qt.gui.QUndoCommand;

public class ReverseComplementCommand extends QUndoCommand {
	private List<QMotif> motifs;
	
	public ReverseComplementCommand(List<QMotif> motifs, QUndoCommand parent) {
		super(parent);
		this.motifs = motifs;
		this.setText("Undo/redo rev. complement");
	}
	
	private void reverseComplement(List<QMotif> motifs) throws IllegalAlphabetException {

		System.out.println("Reverse complementing");
		for (QMotif motif : motifs) {
			System.out.println("Reverse complementing");
			motif.getNmicaMotif().setWeightMatrix(WmTools.reverseComplement(motif.getNmicaMotif().getWeightMatrix()));
			motif.setupDists();
			if (motif.parent() instanceof LogoWidget) {
				LogoWidget parentWidget = (LogoWidget)motif.parent();
				parentWidget.setUpLogo(motif);
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
