package net.piipari.mxplor.ui.widget;

import net.piipari.mxplor.ui.model.QDistribution;

/*
 * This interface is used for distinguishing all graphical items supposed to belong to 
 * a given QDistribution object (so they can be moved, regenerated, etc treated specifically)
 *
 * The method naming is a bit stupid, but this is because QGraphicsPathItem already contains
 * final methods setSelected and isSelected, so I thought it's best to keep consistent -- mp4
 */

public interface DistributionItemIface {
	public QDistribution getDist();
	
	/*
	public void setDist(QDistribution dist);
	
	public boolean isHighlightedDist();
	public void setHighlightedDist(boolean bool);
	public void toggleHighlightedDist();
	
	public boolean isSelectedDist();
	public void setSelectedDist(boolean bool);
	public void toggleSelectedDist();
	*/
	//public boolean isUpdatable();
	public void selectionStateUpdated();
	public void highlightStateUpdated();
	
}
