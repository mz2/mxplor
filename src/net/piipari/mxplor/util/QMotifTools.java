package net.piipari.mxplor.util;

import java.util.ArrayList;
import java.util.List;

import net.derkholm.nmica.motif.Motif;
import net.piipari.mxplor.ui.model.QMotif;

public class QMotifTools {

	//TODO: Move to a Util class
	public static List<QMotif> motifsToQMotifs(Motif[] mms) {
		List<QMotif> qmotifs = new ArrayList<QMotif>(mms.length);
		for (Motif m : mms)
			qmotifs.add(new QMotif(m));
		return qmotifs;
	}

}
