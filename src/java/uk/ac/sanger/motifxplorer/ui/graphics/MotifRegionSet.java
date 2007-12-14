package uk.ac.sanger.motifxplorer.ui.graphics;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.trolltech.qt.gui.QColor;

public class MotifRegionSet {
	private final List<MotifRegion> regions = new ArrayList<MotifRegion>();
	private QColor color;
	private int id;
	private String name;
	
	private static SortedSet<Integer> setIds = new TreeSet<Integer>();
	
	public MotifRegionSet() {
		if (setIds.size() > 0)
			id = setIds.last() + 1;
		else 
			id = 1;
		
		setIds.add(id);
	}
	
	public MotifRegionSet(int id) {
		if (setIds.contains(id))
			throw new IllegalArgumentException(
				"Motif region set with id " + id + " already exists");
		else {
			setIds.add(id);
			this.id = id;
		}
	}
	
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	public void setColor(QColor color) {
		this.color = color;
		for (MotifRegion region : regions) {
			region.setColor(this.color);
		}
	}
	
	public QColor color() {
		return color;
	}

	public List<MotifRegion> getRegions() {
		return this.regions;
	}
	
}
