/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.xmpp.filters;

import org.mule.util.ClassUtils;

import org.jivesoftware.smack.filter.PacketFilter;

/**
 * <code>XmppPacketTypeFilter</code> is an Xmpp PacketTypeFilter adapter.
 */
public class XmppPacketTypeFilter extends AbstractXmppFilter
{
    private volatile Class<?> expectedType;

    public XmppPacketTypeFilter()
    {
        super();
    }

    public XmppPacketTypeFilter(Class<?> expectedType)
    {
        setExpectedType(expectedType);
    }

    public Class<?> getExpectedType()
    {
        return expectedType;
    }

    public void setExpectedType(Class<?> expectedType)
    {
        this.expectedType = expectedType;
    }

    @Override
    protected PacketFilter createFilter()
    {
        return new org.jivesoftware.smack.filter.PacketTypeFilter(expectedType);
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        XmppPacketTypeFilter other = (XmppPacketTypeFilter) obj;
        return ClassUtils.equal(expectedType, other.expectedType);
    }

    @Override
    public int hashCode()
    {
        return ClassUtils.hash(new Object[]{this.getClass(), expectedType});
    }
}
