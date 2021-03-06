/*
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */
package org.polymap.openlayers.rap.widget.controls;

import java.util.Collection;

import org.polymap.openlayers.rap.widget.features.VectorFeature;
import org.polymap.openlayers.rap.widget.layers.Layer;

/**
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 */
public class SelectFeatureControl extends Control {

    /** Triggered before a feature is highlighted. */
    public final static String        EVENT_BEFORE_HIGHLIGHTED = "beforefeaturehighlighted";
    
    /** Triggered when a feature is highlighted. */
    public final static String        EVENT_HIGHLIGHTED = "featurehighlighted";

    /** Triggered when a feature is unhighlighted. */
    public final static String        EVENT_UNHIGHLIGHTED = "featureunhighlighted";
    
    /** Triggered when a feature is highlighted. */
    public final static String        EVENT_SELECTED = "featureselected";
    
    public final static int 		  FLAG_BOX=1;
    
    public final static int 		  FLAG_HOVER=2;
    
    public final static int           FLAG_MULTIPLE=4;
    
    public final static int           FLAG_HIGHLIGHT_ONLY=8;
    
    
    
    public boolean             started_with_hover_enabled = false;

    public boolean             started_with_box_enabled   = false;
    
    private Layer              layer;


	public SelectFeatureControl(Layer layer) {
        this.layer = layer;
		super.create("new OpenLayers.Control.SelectFeature("
				+ layer.getJSObjRef() + ");");
	}

	/*
	 * @deprecated	use SelectFeatureControl(Layer layer, int flags) instead
	 */	
	@Deprecated public SelectFeatureControl(Layer layer, boolean hover) {
		this.started_with_hover_enabled = hover;
        this.layer = layer;
		super.create("new OpenLayers.Control.SelectFeature("
				+ layer.getJSObjRef() + ", {    multiple: false, hover: "
				+ hover + " } );");
	}

	/*
	 * @deprecated	use SelectFeatureControl(Layer layer, int flags) instead
	 */
	@Deprecated public SelectFeatureControl(Layer layer, boolean hover, boolean box) {
		this.started_with_hover_enabled = hover;
		this.started_with_box_enabled = box;
        this.layer = layer;
		super.create("new OpenLayers.Control.SelectFeature("
				+ layer.getJSObjRef() + ", {    multiple: false, hover: "
				+ hover + ", box: " + box + " } );");
	}

	public SelectFeatureControl(Layer layer, int flags) {
        this.layer = layer;
        this.started_with_hover_enabled = (flags & FLAG_HOVER) != 0;
        boolean highlightOnly = (flags & FLAG_HIGHLIGHT_ONLY) != 0;
		this.started_with_box_enabled = (flags & FLAG_BOX ) != 0;
		boolean multiple = (flags & FLAG_MULTIPLE) != 0;
		
		super.create("new OpenLayers.Control.SelectFeature("
				+ layer.getJSObjRef() 
				+ ",{ multiple: " + multiple 
				+ ", hover: " + started_with_hover_enabled 
				+ ", box: " + started_with_box_enabled 
				+ " } );");
	}

	public SelectFeatureControl(Layer[] layers) {
		super.create("new OpenLayers.Control.SelectFeature(" + getJSObj(layers)
				+ ");");
	}

    /**
     * {Boolean} Select on mouse over and deselect on mouse out.
     */
    public void setHover( boolean hover ) {
        assert started_with_hover_enabled : "if you want to toggle hoover you have to start with hover=true in the constructor";
        super.setObjAttr( "hover", hover );
    }

	/**
	 * {Boolean} Allow feature selection by drawing a box.
	 */
	public void setBox(boolean box) {
		assert started_with_box_enabled : "if you want to toggle box selection you have to start with box=true in the constructor";
		super.setObjAttr("box", box);
	}

	/**
	 * {String} An event modifier ("altKey" or "shiftKey") that temporarily sets
	 * the multiple property to true. Default is null.
	 */
	public void setMultipleKey(String key) {
		super.setObjAttr("multipleKey", key);
	}

	/**
	 * {String} An event modifier ("altKey" or "shiftKey") that temporarily sets
	 * the toggle property to true. Default is null.
	 */
	public void setToggleKey(String key) {
		super.setObjAttr("toggleKey", key);
	}

	/**
	 * {Boolean} Allow selection of multiple geometries. Default is false.
	 */
	public void setMultiple(boolean multiple) {
		super.setObjAttr("multiple", multiple);
	}

	public void setRenderIntent(String intent) {
		super.setObjAttr("renderIntent", intent);
	}

	/*
	 * {Boolean} If true do not actually select features (i.e. place them in the
	 * layers selected features array), just highlight them. This property has
	 * no effect if hover is false. Defaults to false.
	 */
	public void setHighlightOnly(Boolean highlight_only) {
		super.setObjAttr("highlightOnly", highlight_only);
	}
	
    public void selectFeatures(Collection<VectorFeature> features) {
        for (VectorFeature feature : features) {
            super.addObjModCode( "select", feature );
        }
    }

    public void selectFids(Collection<String> fids) {
        for (String fid : fids) {
            super.addObjModCode( "var layer = " + layer.getJSObjRef() + ";" +
                    "for (var i=0; i<layer.features.length; i++) {" +
                    "    var feature = layer.features[i];" +
                    "    if (feature.fid == '" + fid + "') {" +
                    "        " + getJSObjRef() + ".select( feature );" + 
                    "    }" +
                    "}"
            );
        }
    }
    
    public void unselectAll() {
        addObjModCode( getJSObjRef() + ".unselectAll();" );
    }

    public void revealFids(Collection<String> fids) {
        
    }
    
}
