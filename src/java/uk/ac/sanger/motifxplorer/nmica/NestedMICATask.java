package uk.ac.sanger.motifxplorer.nmica;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.biojava.bio.BioError;
import org.bjv2.util.cli.CliTools;
import org.bjv2.util.cli.ConfigurationException;
import org.bjv2.util.cli.ConsoleMessages;
import org.bjv2.util.cli.UserLevel;

import uk.ac.sanger.motifxplorer.app.MXplor;

import net.derkholm.nmica.apps.MetaMotifFinder;
import net.derkholm.nmica.model.metamotif.NamedMotifSet;
import net.derkholm.nmica.trainer.event.CycleCompletedEvent;
import net.derkholm.nmica.trainer.event.IterativeTrainerEventListener;
import net.derkholm.nmica.trainer.event.IterativeTrainer;

import com.trolltech.qt.QThread;
import com.trolltech.qt.core.QObject;

public class NestedMICATask extends Thread {
	private List<IterativeTrainerEventListener> cycleCompletedEventListeners = new ArrayList<IterativeTrainerEventListener>();
	IterativeTrainer iterativeTrainer;
	private boolean alive;
	private boolean restart;
	private MetaMotifFinder metaMotifFinder;
	private MXplor motifSetMainWindow; //TODO: This should definitely be an interface
	private String[] args;
	
	public NestedMICATask(MXplor motifSetMainWindow, String[] args) {
		//super(parent);
		super();
		this.motifSetMainWindow = motifSetMainWindow;
		this.iterativeTrainer = initNMICATask(args);
		System.out.println("NestedMICA task constructed.");
		this.args = args;
		
	}
	
	public synchronized void startTraining() {
        if (!isAlive()) {
            start();
        } else {
            restart = true;
            notify();
        }
	}

	public void run() {
    	do {
    		try {
    			synchronized(this) {
    				iterativeTrainer.handleTrainerEvent(iterativeTrainer.trainingCycle());
    			}
    		} catch (Exception e) {
    			e.printStackTrace();
    			throw new BioError("NMICA training cycle failed");
    		}
    		System.out.println("Cycle completed.");
		} while (iterativeTrainer.moreCyclesNeeded());
	}
	
	private void fireCycleCompletedEvent(CycleCompletedEvent event) throws Exception {
		for (IterativeTrainerEventListener listener : cycleCompletedEventListeners)
			listener.trainingEvent(event);
		
	}
	

	public MetaMotifFinder initNMICATask(String[] args) {

		metaMotifFinder = new MetaMotifFinder();
		List<String> argList = Arrays.asList(args);
		if (argList.indexOf("-help") >= 0) {
			ConsoleMessages.helpMessage(metaMotifFinder, System.err, UserLevel.USER, 80);
			return null;
		}
		try {
			/*
			 * Eventually this should be done through commandline arguments 
			 * given to MetaMotifFinder, or through loading the 
			 * motif sets within MotifSetMainWindow
			 */
			args = CliTools.configureBean(metaMotifFinder, args);
			NamedMotifSet[] motifSets = new NamedMotifSet[1];
			/*motifSets[0] = MetaMotifFinder.loadMotifSet(
					new File("/Users/mz2/workspace/TransCrypt/data/motif_sets/old/all/Cys2His2.xms"));
			*/
			//metaMotifFinder.setMotifs(motifSets);
			metaMotifFinder.setIsStandalone(false);
			metaMotifFinder.setTerminateOnConvergance(false);
			metaMotifFinder.addCycleCompletedEventListener(
								new TrainingVisualisationListener(motifSetMainWindow, metaMotifFinder, metaMotifFinder.getLogInterval()));
			
			metaMotifFinder.main(args); //TODO: Rename MetaMotifFinder.main()
			
		} catch (ConfigurationException ex) {
			ConsoleMessages.errorMessage(metaMotifFinder, System.err, 80, ex);
			System.exit(1);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
		
		return metaMotifFinder;
	}

}
