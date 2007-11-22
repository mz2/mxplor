package uk.ac.sanger.motifxplorer.nmica;

import net.derkholm.nmica.trainer.event.CycleCompletedEvent;
import net.derkholm.nmica.trainer.event.IterativeTrainerEventListener;
import net.derkholm.nmica.trainer.event.IterativeTrainer;
import net.derkholm.nmica.trainer.event.IterativeTrainerEvent;

public class CycleTriggeringCycleCompletedEventListener implements
		IterativeTrainerEventListener {

	private IterativeTrainer iterativeTrainer;
	
	public CycleTriggeringCycleCompletedEventListener(IterativeTrainer trainer) {
		iterativeTrainer = trainer;
	}
	
	public void trainingEvent(IterativeTrainerEvent event) throws Exception {
		if (!event.isConverged())
			iterativeTrainer.trainingCycle();
			
	}

}
