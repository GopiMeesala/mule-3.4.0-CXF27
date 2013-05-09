/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.api.context;

import org.mule.api.MuleException;
import org.mule.config.i18n.Message;

/**
 * <code>MuleContextException</code> is thrown when an exception occurs with Mule Context
 * objects
 */
public class MuleContextException extends MuleException
{
    /**
     * Serial version
     */
    private static final long serialVersionUID = -1526680893293714180L;

    /**
     * @param message the exception message
     */
    public MuleContextException(Message message)
    {
        super(message);
    }

    /**
     * @param message the exception message
     * @param cause the exception that cause this exception to be thrown
     */
    public MuleContextException(Message message, Throwable cause)
    {
        super(message, cause);
    }
}
