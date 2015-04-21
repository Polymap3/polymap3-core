/*
 * polymap.org
 * Copyright (C) 2009-2015, Polymap GmbH. All rights reserved.
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
 */
package org.polymap.openlayers.rap.widget.controls;

/**
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ScaleLineControl extends Control {

	public ScaleLineControl( boolean geodesic ) {
		super.create("new OpenLayers.Control.ScaleLine({geodesic:" + geodesic + "});");
	}

	public ScaleLineControl setTopOutUnits(String units) {
		super.addObjModCode("obj.topOutUnits='" + units + "';");
        return this;
	}
	
	public ScaleLineControl setBottomOutUnits(String units) {
		super.addObjModCode("obj.bottomOutUnits='" + units + "';");
		return this;
	}

	public ScaleLineControl setTopInUnits(String units) {
		super.addObjModCode("obj.topInUnits='" + units + "';");
	    return this;
	}
	
	public ScaleLineControl setBottomInUnits(String units) {
		super.addObjModCode("obj.bottomInUnits='" + units + "';");
        return this;
	}
	
}
