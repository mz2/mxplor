package uk.ac.sanger.motifxplorer.ui.graphics;

import java.util.Iterator;

import net.derkholm.nmica.model.metamotif.MetaMotifIOTools;
import net.derkholm.nmica.motif.Motif;

import org.biojava.bio.BioError;
import org.biojava.bio.dist.Distribution;
import org.biojava.bio.dist.SimpleDistribution;
import org.biojava.bio.dp.SimpleWeightMatrix;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.Symbol;

import uk.ac.sanger.motifxplorer.ui.model.QDistribution;
import uk.ac.sanger.motifxplorer.ui.model.QMotif;
import uk.ac.sanger.motifxplorer.ui.widget.LogoView;

import com.trolltech.qt.core.QPointF;
import com.trolltech.qt.core.QRectF;
import com.trolltech.qt.core.QSizeF;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QBrush;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QGraphicsRectItem;
import com.trolltech.qt.gui.QGraphicsTextItem;
import com.trolltech.qt.gui.QPen;

public class MotifRegion extends QGraphicsRectItem {
	private QMotif motif;
	private int begin;
	private int length;
	private String name;
	private String desc;
	
	protected boolean kept = true;
	private QGraphicsTextItem nameAndDescItem;
	
	private static final QColor DEFAULT_BRUSH_COLOR =  new QColor(255,200,100,100);
	private static final QBrush DEFAULT_BRUSH = new QBrush(DEFAULT_BRUSH_COLOR);
	protected QBrush normalBrush = DEFAULT_BRUSH;
	
	private static final QColor DEFAULT_PEN_COLOR = new QColor(255,200,100,100);
	private static final QPen DEFAULT_UNKEPT_PEN = new QPen(DEFAULT_PEN_COLOR,2,
												Qt.PenStyle.SolidLine,
												Qt.PenCapStyle.RoundCap,
												Qt.PenJoinStyle.RoundJoin);
	
	private static final QPen DEFAULT_KEPT_PEN = new QPen(DEFAULT_PEN_COLOR,4,
												Qt.PenStyle.SolidLine,
												Qt.PenCapStyle.RoundCap,
												Qt.PenJoinStyle.RoundJoin);
	
	protected QColor kPenCol, uPenCol;
	protected QPen kPen = DEFAULT_KEPT_PEN;
	protected QPen uPen = DEFAULT_UNKEPT_PEN;
	
	//protected int annotationSetId;
	protected int numOverlaps;
	private MotifRegionSet regionSet;
	//private static int maxAnnotationSetId = 0;
	//public static int maxAnnotationSetId() {return maxAnnotationSetId;}
	
	public QMotif getMotif() {
		return this.motif;
	}
	
	public MotifRegion(MotifRegionSet mset, QMotif m, int begin, int length) {
		this.motif = m;
		this.begin = begin;
		this.length = length;
		
		
		regionSet = mset;
		if (regionSet != null) {
			mset.getRegions().add(this);
			if (regionSet.color() != null) 
				setColor(regionSet.color());
			else {
				System.out.println("Using default colors");
				setBrushes(defaultBrushColor());
				setPens(defaultPenColor());
			}
		}
			
		int end = begin + length - 1;
		QDistribution b = m.dists().get(begin);
		QDistribution e = m.dists().get(end);
		//annotationSetId = -1;
		numOverlaps = this.motif.overlappingRegions(begin, length);
		
		setZValue(numOverlaps + 1);
		
		if(name != null) {
			nameAndDescItem = new QGraphicsTextItem(name);
			nameAndDescItem.setParentItem(this);
		}
		
		if (b.getBoundItem() != null && e.getBoundItem() != null) {
			updateLocation();
		}
	}
	
	public void updateLocation() {
		if (motif.getBoundingBox() == null) {
			if (this.name != null)
			System.out.println("Bounding box is null when updating " + this.name);
			return;
		}
		else {
			if (this.parentItem() == null && this != motif.getBoundingBox())
				this.setParentItem(motif.getBoundingBox());
		}
		
		QDistribution b = motif.dists().get(begin);
		QDistribution e = motif.dists().get(begin + length - 1);
		double origH,h;
		
		if (b.getBoundItem() != null && e.getBoundItem() != null) {
			
			origH = h = e.getBoundItem().boundingRect().height();
			
			//we reduce the height of the box by 0.1 the end height
			h = Math.max(h*0.2,h * (1 - numOverlaps * 0.25));
			
			QSizeF rectSize = new QSizeF(
					e.getBoundItem().boundingRect().right() - 
					b.getBoundItem().boundingRect().left(),
					h);
			
			setRect(new QRectF(
						new QPointF(b.getBoundItem().rect().x(),
						origH - h), rectSize));
			
			String nameS = "";
			if (name != null)
				nameS = name;
			String descS = "";
			if (desc != null)
				descS = "";
			
			if (!nameS.equals("") || !descS.equals("")) {
				System.out.println("Updating: name item " + name);
				if (nameAndDescItem != null && nameAndDescItem.scene() != null)
					nameAndDescItem.scene().removeItem(nameAndDescItem);
				
				if (nameS.equals("") && descS.equals(""))
					nameAndDescItem = new QGraphicsTextItem(nameS + " - " + descS);
				else if (nameS.equals(""))
					nameAndDescItem = new QGraphicsTextItem(descS);
				else
					nameAndDescItem = new QGraphicsTextItem(nameS);
				
				System.out.println("Name:" + name);
				nameAndDescItem.setParentItem(this);
				nameAndDescItem.moveBy(
							b.getBoundItem().rect().x(),
							rect().y());
			}
		} else {System.out.println("Updating: No begin or end item when adding " + name);}
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
		if (this.name != null) {
			if (nameAndDescItem != null && nameAndDescItem.scene() != null)
				nameAndDescItem.scene().removeItem(nameAndDescItem);
		}
		updateLocation();
	}


	/**
	 * @return the desc
	 */
	public String getDesc() {
		return desc;
	}

	/**
	 * @param desc the desc to set
	 */
	public void setDesc(String desc) {
		this.desc = desc;
		this.name = name;
			if (this.desc != null && 
				nameAndDescItem != null && 
				nameAndDescItem.scene() != null)
				nameAndDescItem.scene().removeItem(nameAndDescItem);
		
		updateLocation();
	}
	

	/**
	 * @return the kept
	 */
	public boolean isKept() {
		return kept;
	}

	/**
	 * @param kept the kept to set
	 */
	public void setKept(boolean kept) {
		this.kept = kept;
		if (this.kept) {
			if (keptPen() != null)
				this.setPen(keptPen());
			
		} else {
			if (unkeptPen() != null)
				this.setPen(unkeptPen());
			
		}
	}

	protected void setNormalBrush(QBrush brush) {
		normalBrush = brush;
	}
	
	protected QBrush normalBrush() {
		return normalBrush;
	}
	
	protected QPen unkeptPen() {
		return uPen;
	}
	
	protected QPen keptPen() {
		return this.kPen;
	}
	
	/**
	 * @return the begin
	 */
	public int getBegin() {
		return begin;
	}

	/**
	 * @return the length
	 */
	public int getLength() {
		return length;
	}	

	/**
	 * @return the annotationSetId
	 */
	public MotifRegionSet getAnnotationSet() {
		return regionSet;
	}

	/**
	 * @param annotationSetId the annotationSetId to set
	 */
	public void setAnnotationSet(MotifRegionSet regSet) {
		regionSet = regSet;
		if (regSet != null && regSet.color() != null) setColor(regSet.color());
	}
	
	//FIXME: Bug in SimpleDistribution(Distribution d) ! Only one of the weights is read
	
	public Motif toMotif() {
		Distribution[] dists = new Distribution[length];
		
		for (int i = 0; i < length; i++) {
			Distribution d = this.motif.getDists().get(begin+i).getDist();
			dists[i] = new SimpleDistribution((FiniteAlphabet)d.getAlphabet());
			for (Iterator it = ((FiniteAlphabet) d.getAlphabet())
					.iterator(); it.hasNext();) {
				Symbol sym = (Symbol) it.next();
				try {
					dists[i].setWeight(sym, d.getWeight(sym));
				} catch (IllegalSymbolException e) {
					throw new BioError("Illegal symbol exception caught", e);
				}
			}
		}
		
		Motif m = new Motif();
		try {
			m.setWeightMatrix(new SimpleWeightMatrix(dists));
		} catch (IllegalAlphabetException e) {
			throw new BioError("Illegal alphabet exception caught");
		}
		m.setName(name);
		
		//FIXME: This alphaSumsFromMotif() is incorrect. the alpha sums wont be here (you just constructed the motif)
		double[] precisions = MetaMotifIOTools.alphaSumsFromMotif(m);
		if (precisions != null)
			for (int i = 0; i < length; i++)
				m.getAnnotation().setProperty("alphasum;column:"+i, precisions[begin+i]);

		return m;
	}


	public void removeFromScene() {
		if (this.scene() != null) {
			this.scene().removeItem(this);
		}
	}
	
	
	public QColor getColor() {
		return this.normalBrush.color();
	}

	public void setColor(QColor color) {
		setBrushes(color);
		setPens(color);
	}
	
	protected QColor defaultBrushColor() {
		return DEFAULT_BRUSH_COLOR;
	}
	
	protected QColor defaultPenColor() {
		return DEFAULT_PEN_COLOR;
	}
	
	public void setBrushes(QColor color) {
		if (color == null) {
			color = defaultBrushColor();
		}
		normalBrush = new QBrush(color);
		setBrush(normalBrush);
	}
	
	protected void setPens(QColor color) {
		if (color == null) {
			color = defaultPenColor();
		}
		
		this.kPen = new QPen(color,4,
						Qt.PenStyle.SolidLine,
						Qt.PenCapStyle.RoundCap,
						Qt.PenJoinStyle.RoundJoin);
		
		this.uPen = new QPen(color,2,
						Qt.PenStyle.SolidLine,
						Qt.PenCapStyle.RoundCap,
						Qt.PenJoinStyle.RoundJoin);
	
		if (kept)
			setPen(kPen);
		else
			setPen(uPen);
	}
	

}
