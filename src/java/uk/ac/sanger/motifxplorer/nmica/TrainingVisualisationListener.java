package uk.ac.sanger.motifxplorer.nmica;

import java.util.*;

import org.biojava.bio.BioException;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.IllegalSymbolException;

import uk.ac.sanger.motifxplorer.app.MXplor;
import uk.ac.sanger.motifxplorer.util.QMotifTools;
import net.derkholm.nmica.apps.MetaMotifFinder;
import net.derkholm.nmica.model.metamotif.MetaMotif;
import net.derkholm.nmica.motif.Motif;
import net.derkholm.nmica.trainer.event.CycleCompletedEvent;
import net.derkholm.nmica.trainer.event.IterativeTrainerEvent;
import net.derkholm.nmica.trainer.event.IterativeTrainerEventListener;
import net.derkholm.nmica.trainer.event.IterativeTrainer;

public class TrainingVisualisationListener implements IterativeTrainerEventListener {
	private MXplor window;
	private IterativeTrainer iterativeTrainer;
	private int visInterval;
	
	public TrainingVisualisationListener(MXplor window, IterativeTrainer trainer, int visInterval) {
		this.window = window;
		this.iterativeTrainer = trainer;
		this.visInterval = visInterval;
	}

	synchronized public void trainingEvent(IterativeTrainerEvent event)
	throws IllegalSymbolException, IllegalAlphabetException, BioException {
		System.out.println("Cycle completed... Visualising");
		Motif[] meanMotifs = MetaMotifFinder.meanMotifsFromMetaMotifMultiICAModel(event.getBestModel());
		
		//TODO: Try doing the qmotif conversion inside the motifsetmainwindow (thread issue)
		
		/*
		int i = 0;
		for (Motif m : meanMotifs)
			window.updateShownMotif(m, i++);
		*/
		
		//window.updateShownMotifs(QMotifTools.motifsToQMotifs(meanMotifs));
	}

}
