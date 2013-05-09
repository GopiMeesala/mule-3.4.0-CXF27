/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.launcher;



import org.mule.module.launcher.descriptor.ApplicationDescriptor;
import org.mule.tck.junit4.AbstractMuleTestCase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.mule.util.IOUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test the overriding of app properties by system properties
 */
public class PropertyOverridesTestCase extends AbstractMuleTestCase
{
    private Map<String, String> existingProperties = new HashMap<String, String>();

    private void setSystemProperties()
    {
        setSystemProperty("texas", "province");
        setSystemProperty("-Operu", "nation");
        setSystemProperty("-Dtexas.capital", "houston");
        setSystemProperty("-O-Dperu.capital", "bogota");
        setSystemProperty("-Omule", "wayCool");
        setSystemProperty("-Omule.mmc", "evenCooler");
    }

    @Test
    public void testOverrides() throws Exception
    {
        File tempProps = File.createTempFile("property", "overrides");
        InputStream input = getClass().getClassLoader().getResourceAsStream("overridden.properties");
        FileOutputStream output = new FileOutputStream(tempProps);
        IOUtils.copy(input, output);
        input.close();
        output.close();
        ApplicationDescriptor descriptor = new ApplicationDescriptor();
        DefaultAppBloodhound dab = new DefaultAppBloodhound();
        dab.setApplicationProperties(descriptor, tempProps);
        Map<String, String>appProps = descriptor.getAppProperties();
        assertEquals("state", appProps.get("texas"));
        assertEquals("country", appProps.get("peru"));
        assertEquals("austin", appProps.get("texas.capital"));
        assertEquals("4", appProps.get("peru.capital.numberOfletters"));
        assertEquals("esb", appProps.get("mule"));
        assertEquals("ipaas", appProps.get("mule.ion"));

        try
        {
            setSystemProperties();
            descriptor = new ApplicationDescriptor();
            dab.setApplicationProperties(descriptor, tempProps);
            appProps = descriptor.getAppProperties();
            assertEquals("state", appProps.get("texas"));
            assertEquals("nation", appProps.get("peru"));
            assertEquals("austin", appProps.get("texas.capital"));
            assertEquals("4", appProps.get("peru.capital.numberOfletters"));
            assertEquals("wayCool", appProps.get("mule"));
            assertEquals("ipaas", appProps.get("mule.ion"));
            assertEquals("evenCooler", appProps.get("mule.mmc"));

            descriptor = new ApplicationDescriptor();
            dab.setApplicationProperties(descriptor, new File("nonexistent.nonexistent"));
            appProps = descriptor.getAppProperties();
            assertNull(appProps.get("texas"));
            assertEquals("nation", appProps.get("peru"));
            assertNull(appProps.get("texas.capital"));
            assertNull(appProps.get("peru.capital.numberOfletters"));
            assertEquals("wayCool", appProps.get("mule"));
            assertNull(appProps.get("mule.ion"));
            assertEquals("evenCooler", appProps.get("mule.mmc"));
        }
        finally
        {
            resetSystemProperties();
        }
    }

    private void resetSystemProperties()
    {
        for (Map.Entry<String, String> entry: existingProperties.entrySet())
        {
            String key = entry.getKey();
            String value = entry.getValue();
            if (value == null)
            {
                System.getProperties().remove(key);
            }
            else
            {
                System.setProperty(key, entry.getValue());
            }
        }
    }

    private void setSystemProperty(String key, String value)
    {
        String previous = System.setProperty(key, value);
        existingProperties.put(key, previous);
    }
}
