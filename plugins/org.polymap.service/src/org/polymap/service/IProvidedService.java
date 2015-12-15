/* 
 * polymap.org
 * Copyright 2009-2015, Polymap GmbH. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.service;

import java.util.List;

import org.polymap.core.model.Entity;
import org.polymap.core.model.ModelProperty;
import org.polymap.core.project.IMap;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IProvidedService
        extends Entity {

    public static final String  PROP_PATHSPEC = "PATH_SPEC";
    public static final String  PROP_SRS = "SRS";
    public static final String  PROP_SERVICE_TYPE = "SERVICE_TYPE";
    public static final String  PROP_ENABLED = "ENABLED";
    public static final String  PROP_DESCRIPTION = "DESCRIPTION";
    public static final String  PROP_NAMESPACE = "NAMESPACE";
    public static final String  PROP_FLAVOURS = "FLAVOURS";
    
    public static final String  PROP_ADDRESS_CITY = "ADDRESS_CITY";
    public static final String  PROP_ADDRESS_COUNTRY = "ADDRESS_COUNTRY";
    public static final String  PROP_ADDRESS_POSTALCODE = "ADDRESS_POSTALCODE";
    public static final String  PROP_ADDRESS = "ADDRESS";

    public static final String  PROP_CONTACT_PERSON = "CONTACT_PERSON";
    public static final String  PROP_CONTACT_ORG = "CONTACT_ORG";
    public static final String  PROP_CONTACT_VOICE = "CONTACT_VOICE";
    public static final String  PROP_CONTACT_EMAIL = "CONTACT_EMAIL";

    
    public boolean isEnabled();
    
    public boolean isStarted();
    
    @ModelProperty(PROP_ENABLED)
    public void setEnabled( Boolean enabled );
    
    public String getPathSpec();
    
    @ModelProperty(PROP_PATHSPEC)
    public void setPathSpec( String url );
    
    public String getDescription();
    
    @ModelProperty(PROP_DESCRIPTION)
    public void setDescription( String description );
    
    // address ********************************************
    
    public String getAddressCity();
    
    @ModelProperty(PROP_ADDRESS_CITY)
    public void setAddressCity( String value );
    
    public String getAddressCountry();
    
    @ModelProperty(PROP_ADDRESS_COUNTRY)
    public void setAddressCountry( String value );
    
    public String getAddressPostalcode();
    
    @ModelProperty(PROP_ADDRESS_POSTALCODE)
    public void setAddressPostalcode( String value );
    
    public String getAddress();
    
    @ModelProperty(PROP_ADDRESS)
    public void setAdress( String value );
    
    // contact ********************************************
    
    public String getContactPerson();
    
    @ModelProperty(PROP_CONTACT_PERSON)
    public void setContactPerson( String value );
    
    public String getContactOrg();
    
    @ModelProperty(PROP_CONTACT_ORG)
    public void setContactOrg( String value );
    
    public String getContactEmail();
    
    @ModelProperty(PROP_CONTACT_EMAIL)
    public void setContactEmail( String value );
    
    public String getContactVoice();
    
    @ModelProperty(PROP_CONTACT_VOICE)
    public void setContactVoice( String value );
    
    
    public String getNamespace();
    
    @ModelProperty(PROP_NAMESPACE)
    public void setNamespace( String namespace );
    
    public String getMapId();

    public IMap getMap();

    /**
     * The type(s) of this service.
     * 
     * @return One of {@link ServicesPlugin#SERVICE_TYPE_WMS}, {@link ServicesPlugin#SERVICE_TYPE_WFS}.
     */
    public String getServiceType();

    /**
     * 
     * @param One of {@link ServicesPlugin#SERVICE_TYPE_WMS}, {@link ServicesPlugin#SERVICE_TYPE_WFS}.
     */
    public boolean isServiceType( String serviceType );

//    @ModelProperty(PROP_SERVICE_TYPE)
//    public void setServiceTypes( List<String> serviceTypes );
    
    public List<String> getSRS();
    
    @ModelProperty(PROP_SRS)
    public void setSRS( List<String> srs );
    
    public List<String> getFlavours();
    
    @ModelProperty(PROP_FLAVOURS)
    public void setFlavours( List<String> flavours );
    
    
    public void start() throws Exception;
    
    public void stop() throws Exception;

}
