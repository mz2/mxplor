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
	private boolean kept;
	
	private QGraphicsTextItem nameItem;
	
	private static final QBrush defaultBrush = new QBrush(new QColor(255,200,100,20));
	private QBrush dBrush = defaultBrush;
	
	private static final QPen unkeptPen = new QPen(new QColor(255,150,150,255),2,
												Qt.PenStyle.SolidLine,
												Qt.PenCapStyle.RoundCap,
												Qt.PenJoinStyle.RoundJoin);
	
	private static final QPen keptPen = new QPen(new QColor(255,150,150,255),4,
												Qt.PenStyle.SolidLine,
												Qt.PenCapStyle.RoundCap,
												Qt.PenJoinStyle.RoundJoin);
	
	protected QPen kPen = keptPen;
	protected QPen uPen = unkeptPen;
	
	
	private int annotationSetId;
	private int numOverlaps;
	private static int maxAnnotationSetId = 0;
	public static int maxAnnotationSetId() {return maxAnnotationSetId;}
	
	
	public QMotif getMotif() {
		return this.motif;
	}
	
	public MotifRegion(QMotif m, int begin, int length) {
		init(m, begin, length);
		int end = begin + length - 1;
		QDistribution b = m.dists().get(begin);
		QDistribution e = m.dists().get(end);
		annotationSetId = -1;
		numOverlaps = this.motif.overlappingRegions(begin, length);
		
		setZValue(numOverlaps + 1);
		
		if(name != null) {
			nameItem = new QGraphicsTextItem(name);
			nameItem.setParentItem(this);
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
			
			if (name != null && !name.equals("")) {
				System.out.println("Updating: name item " + name);
				if (nameItem != null && nameItem.scene() != null)
					nameItem.scene().removeItem(nameItem);
				
				nameItem = new QGraphicsTextItem(name);
				System.out.println("Name:" + name);
				nameItem.setParentItem(this);
				nameItem.moveBy(
							b.getBoundItem().rect().x(),
							rect().y());
			}
		} else {System.out.println("Updating: No begin or end item when adding " + name);}
	}
	
	private void init(QMotif m, int begin, int length) {
		this.motif = m;
		this.begin = begin;
		this.length = length;
		
		setBrush(normalBrush());
		setPen(unkeptPen());
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
			if (nameItem != null && nameItem.scene() != null)
				nameItem.scene().removeItem(nameItem);
		}
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
		dBrush = brush;
	}
	
	protected QBrush normalBrush() {
		return dBrush;
	}
	
	protected QPen unkeptPen() {
		return uPen;
	}
	
	protected void setUnkeptPen(QPen pen) {
		this.uPen = pen;
	}
	
	protected QPen keptPen() {
		return this.kPen;
	}
	
	protected void setKeptPen(QPen pen) {
		this.kPen = pen;
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
	public int getAnnotationSetId() {
		return annotationSetId;
	}

	/**
	 * @param annotationSetId the annotationSetId to set
	 */
	public void setAnnotationSetId(int annotationSetId) {
		this.annotationSetId = annotationSetId;
		if (annotationSetId > maxAnnotationSetId) maxAnnotationSetId = annotationSetId;
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
	

}
