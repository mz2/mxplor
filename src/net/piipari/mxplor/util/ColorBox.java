package net.piipari.mxplor.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.piipari.mxplor.ui.model.QMotif;


import com.trolltech.qt.gui.QColor;

public class ColorBox<T> {
	private List<QColor> colors = new ArrayList<QColor>();
	private static final int DEFAULT_OPACITY = 50;
	private int opacity;
	private int colorIndex = 0;
	private HashMap<T, QColor> colorMap = new HashMap<T, QColor>();
	
	public QColor colorFor(T t) {
		QColor col;
		if ((col = colorMap.get(t)) != null) return col;
		
		col = nextColor();
		setColor(t,col);
		return col;
	}
	
	public void setColor(T t, QColor color) {
		colorMap.put(t, color);
		//motif.setColor(color);
	}
	
	public int currentIndex() {
		return colorIndex;
	}
	
	public QColor color(int i) {
		return colors.get(i % colors.size());
	}
	
	public QColor nextColor() {
		return colors.get(colorIndex++ % colors.size());
	}
	
	public void hueOffset(double d) {
		if (d < -1 || d > 1) throw new IllegalArgumentException("Illegal offset:" + d);
		
		for (int i = 0, size=colors.size(); i < size; i++) {
			QColor c = colors.get(i);
			
			double offsetHue = c.hueF() + d;
			if (offsetHue >= 1 || offsetHue <= 0)
				offsetHue = c.hueF() - d;
			colors.get(i).setHsvF(offsetHue, c.saturationF(), c.valueF());
		}
	}
	
	public void saturationOffset(double d) {
		if (d < -1 || d > 1) throw new IllegalArgumentException("Illegal offset:" + d);
		
		for (int i = 0, size=colors.size(); i < size; i++) {
			QColor c = colors.get(i);
			
			double offsetSaturation = c.saturationF() + d;
			if (offsetSaturation >= 1 || offsetSaturation <= 0)
				offsetSaturation = c.hueF() - d;
			colors.get(i).setHsvF(c.hueF(), offsetSaturation, c.valueF());
		}
	}
	
	public void valueOffset(double d) {
		if (d < -1 || d > 1) throw new IllegalArgumentException("Illegal offset:" + d);
		
		for (int i = 0, size=colors.size(); i < size; i++) {
			QColor c = colors.get(i);
			
			double offsetValue = c.valueF() + d;
			if (offsetValue >= 1 || offsetValue <= 0)
				offsetValue = c.hueF() - d;
			colors.get(i).setHsvF(c.hueF(), offsetValue, c.valueF());
		}
	}
	
	public ColorBox() {
		this(DEFAULT_OPACITY);
	}
	
	public ColorBox(int opac) {
		this.opacity = opac;
		//FIXME: Take the colors off a configuration file...
		//nice handpicked colours (doc/motifsetcolors.svg)
		colors.add(new QColor(0,177,0,opacity));
		colors.add(new QColor(0,0,255,opacity));
		colors.add(new QColor(255,0,0,opacity));
		colors.add(new QColor(211,141,95,opacity));
		colors.add(new QColor(170,212,0,opacity));
		colors.add(new QColor(255,102,0,opacity));
		colors.add(new QColor(0,68,170,opacity));
		colors.add(new QColor(55,200,55,opacity));
		colors.add(new QColor(255,128,128,opacity));
		colors.add(new QColor(255,0,102,opacity));
		colors.add(new QColor(85,153,255,opacity));
		colors.add(new QColor(204,0,255,opacity));
		colors.add(new QColor(145,111,124,opacity));
		colors.add(new QColor(128,128,255,opacity));
		colors.add(new QColor(0,255,255,opacity));
		colors.add(new QColor(128,128,0,opacity));
		colors.add(new QColor(222,135,170,opacity));
		colors.add(new QColor(102,0,255,opacity));
		colors.add(new QColor(255,85,85,opacity));
		colors.add(new QColor(170,212,0,opacity));
		colors.add(new QColor(0,102,128,opacity));
		colors.add(new QColor(160,44,90,opacity));
		colors.add(new QColor(204,255,0,opacity));
		colors.add(new QColor(120,68,33,opacity));
			
		List<QColor> lighterCs = new ArrayList<QColor>(colors);
		List<QColor> darkerCs = new ArrayList<QColor>(colors);
		List<QColor> muchLighterCs = new ArrayList<QColor>(colors);
		List<QColor> muchDarkerCs = new ArrayList<QColor>(colors);
			
		for (QColor c : lighterCs)
			c = c.darker(3);
		
		for (QColor c : darkerCs)
			c = c.lighter(3);
		
		for (QColor c : muchLighterCs)
			c = c.darker(6);
		
		for (QColor c : muchDarkerCs)
			c = c.lighter(6);
		
		colors.addAll(lighterCs);
		colors.addAll(darkerCs);
		colors.addAll(muchLighterCs);
		colors.addAll(muchDarkerCs);
	}
}
