/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.registry;

import org.mule.api.MuleContext;
import org.mule.api.context.MuleContextAware;
import org.mule.api.lifecycle.Disposable;
import org.mule.api.registry.MuleRegistry;
import org.mule.api.registry.RegistrationException;
import org.mule.tck.junit4.AbstractMuleContextTestCase;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TransientRegistryTestCase extends AbstractMuleContextTestCase
{
    @Test
    public void testObjectLifecycle() throws Exception
    {
        muleContext.start();

        InterfaceBasedTracker tracker = new InterfaceBasedTracker();
        muleContext.getRegistry().registerObject("test", tracker);

        muleContext.dispose();
        assertEquals("[setMuleContext, initialise, start, stop, dispose]", tracker.getTracker().toString());
    }

    @Test
    public void testJSR250ObjectLifecycle() throws Exception
    {
        muleContext.start();

        JSR250ObjectLifecycleTracker tracker = new JSR250ObjectLifecycleTracker();
        muleContext.getRegistry().registerObject("test", tracker);

        muleContext.dispose();
        assertEquals("[setMuleContext, initialise, dispose]", tracker.getTracker().toString());
    }

    @Test
    public void testObjectBypassLifecycle() throws Exception
    {
        muleContext.start();

        InterfaceBasedTracker tracker = new InterfaceBasedTracker();
        muleContext.getRegistry().registerObject("test", tracker, MuleRegistry.LIFECYCLE_BYPASS_FLAG);
        muleContext.dispose();
        assertEquals("[setMuleContext, stop, dispose]", tracker.getTracker().toString());
    }

    @Test
    public void testObjectBypassInjectors() throws Exception
    {
        muleContext.start();
        InterfaceBasedTracker tracker = new InterfaceBasedTracker();
        muleContext.getRegistry().registerObject("test", tracker, MuleRegistry.INJECT_PROCESSORS_BYPASS_FLAG);
        muleContext.dispose();
        assertEquals("[initialise, start, stop, dispose]", tracker.getTracker().toString());
    }

    @Test
    public void testObjectBypassLifecycleAndInjectors() throws Exception
    {
        muleContext.start();

        InterfaceBasedTracker tracker = new InterfaceBasedTracker();
        muleContext.getRegistry().registerObject("test", tracker, MuleRegistry.LIFECYCLE_BYPASS_FLAG + MuleRegistry.INJECT_PROCESSORS_BYPASS_FLAG);
        muleContext.dispose();
        assertEquals("[stop, dispose]", tracker.getTracker().toString());
    }

    @Test
    public void testObjectLifecycleStates() throws Exception
    {
        InterfaceBasedTracker tracker = new InterfaceBasedTracker();
        muleContext.getRegistry().registerObject("test", tracker);
        assertEquals("[setMuleContext, initialise]", tracker.getTracker().toString());

        try
        {
            muleContext.initialise();
            fail("context already initialised");
        }
        catch (IllegalStateException e)
        {
            //expected
        }

        muleContext.start();
        assertEquals("[setMuleContext, initialise, start]", tracker.getTracker().toString());

        try
        {
            muleContext.start();
            fail("context already started");
        }
        catch (IllegalStateException e)
        {
            //expected
        }

        muleContext.stop();
        assertEquals("[setMuleContext, initialise, start, stop]", tracker.getTracker().toString());

        try
        {
            muleContext.stop();
            fail("context already stopped");
        }
        catch (IllegalStateException e)
        {
            //expected
        }

        muleContext.dispose();
        assertEquals("[setMuleContext, initialise, start, stop, dispose]", tracker.getTracker().toString());

        try
        {
            muleContext.dispose();
            fail("context already disposed");
        }
        catch (IllegalStateException e)
        {
            //expected
        }
    }

    @Test
    public void testObjectLifecycleRestart() throws Exception
    {
        InterfaceBasedTracker tracker = new InterfaceBasedTracker();
        muleContext.getRegistry().registerObject("test", tracker);

        muleContext.start();
        assertEquals("[setMuleContext, initialise, start]", tracker.getTracker().toString());

        muleContext.stop();
        assertEquals("[setMuleContext, initialise, start, stop]", tracker.getTracker().toString());

        muleContext.start();
        assertEquals("[setMuleContext, initialise, start, stop, start]", tracker.getTracker().toString());

        muleContext.dispose();
        assertEquals("[setMuleContext, initialise, start, stop, start, stop, dispose]", tracker.getTracker().toString());
    }

    @Test
    public void testLifecycleStateOutOfSequenceDisposeFirstWithTransientRegistryDirectly() throws Exception
    {
        TransientRegistry reg = new TransientRegistry(muleContext);

        reg.fireLifecycle(Disposable.PHASE_NAME);
        
        InterfaceBasedTracker tracker = new InterfaceBasedTracker();
        try
        {
            reg.registerObject("test", tracker);
            fail("Cannot register objects on a disposed registry");
        }
        catch (RegistrationException e)
        {
            //Expected
        }
    }

    @Test
    public void testLifecycleStateOutOfSequenceStartFirst() throws Exception
    {
        muleContext.start();
        InterfaceBasedTracker tracker = new InterfaceBasedTracker();
        muleContext.getRegistry().registerObject("test", tracker);
        //Initialise called implicitly because you cannot start a component without initialising it first
        assertEquals("[setMuleContext, initialise, start]", tracker.getTracker().toString());

        muleContext.dispose();
        //Stop called implicitly because you cannot dispose component without stopping it first
        assertEquals("[setMuleContext, initialise, start, stop, dispose]", tracker.getTracker().toString());
    }

    @Test
    public void testLifecycleStateOutOfSequenceStopFirst() throws Exception
    {
        try
        {
            muleContext.stop();
            fail("Cannot not stop the context if not started");
        }
        catch (IllegalStateException e)
        {
            //expected
        }

        muleContext.start();
        muleContext.stop();
        InterfaceBasedTracker tracker = new InterfaceBasedTracker();
        muleContext.getRegistry().registerObject("test", tracker);
        //Start is bypassed because the component was added when the registry was stopped, hence no need to start the component
        //Stop isn't called either because start was not called
        //Initialised is called because that pahse has completed in the registry
        assertEquals("[setMuleContext, initialise]", tracker.getTracker().toString());

        muleContext.dispose();
        assertEquals("[setMuleContext, initialise, dispose]", tracker.getTracker().toString());
    }

    @Test
    public void testLifecycleStateOutOfSequenceDisposeFirst() throws Exception
    {
        muleContext.dispose();

        InterfaceBasedTracker tracker = new InterfaceBasedTracker();
        try
        {
            muleContext.getRegistry().registerObject("test", tracker);
            fail("cannot register objects on a disposed registry");
        }
        catch (RegistrationException e)
        {
            //Expected
        }
    }

    public class InterfaceBasedTracker extends AbstractLifecycleTracker
    {
        // no custom methods
    }

    public class JSR250ObjectLifecycleTracker implements MuleContextAware
    {
        private final List<String> tracker = new ArrayList<String>();

        public List<String> getTracker() {
            return tracker;
        }

        public void setMuleContext(MuleContext context)
        {
            tracker.add("setMuleContext");
        }

        @PostConstruct
        public void init()
        {
            tracker.add("initialise");
        }

        @PreDestroy
        public void dispose()
        {
            tracker.add("dispose");
        }
    }
}
