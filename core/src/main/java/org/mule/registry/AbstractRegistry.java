/*
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.registry;

import org.mule.api.MuleContext;
import org.mule.api.MuleRuntimeException;
import org.mule.api.lifecycle.Disposable;
import org.mule.api.lifecycle.Initialisable;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.lifecycle.LifecycleException;
import org.mule.api.lifecycle.Stoppable;
import org.mule.api.registry.RegistrationException;
import org.mule.api.registry.Registry;
import org.mule.config.i18n.CoreMessages;
import org.mule.config.i18n.MessageFactory;
import org.mule.lifecycle.RegistryLifecycleManager;
import org.mule.util.UUID;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public abstract class AbstractRegistry implements Registry
{
    /** the unique id for this Registry */
    private String id;

    protected transient Log logger = LogFactory.getLog(getClass());

    protected MuleContext muleContext;

    protected RegistryLifecycleManager lifecycleManager;

    protected AbstractRegistry(String id, MuleContext muleContext)
    {
        if (id == null)
        {
            throw new MuleRuntimeException(CoreMessages.objectIsNull("RegistryID"));
        }
        this.id = id;
        this.muleContext = muleContext;
        lifecycleManager = createLifecycleManager();
    }

    @Override
    public final synchronized void dispose()
    {
        if(lifecycleManager.getState().isStarted())
        {
            try
            {
                getLifecycleManager().fireLifecycle(Stoppable.PHASE_NAME);
            }
            catch (LifecycleException e)
            {
                logger.error("Failed to shut down registry cleanly: " + getRegistryId(), e);
            }
        }
        //Fire dispose lifecycle before calling doDispose() that that registries can clear any object caches once all objects
        //are disposed
        try
        {
            getLifecycleManager().fireLifecycle(Disposable.PHASE_NAME);
        }
        catch (LifecycleException e)
        {
            logger.error("Failed to shut down registry cleanly: " + getRegistryId(), e);
        }

        try
        {
            doDispose();
        }
        catch (Exception e)
        {
            logger.error("Failed to cleanly dispose: " + e.getMessage(), e);
        }
    }

    protected RegistryLifecycleManager createLifecycleManager()
    {
        return new RegistryLifecycleManager(getRegistryId(), this, muleContext);
    }

    abstract protected void doInitialise() throws InitialisationException;

    abstract protected void doDispose();

    @Override
    public final void initialise() throws InitialisationException
    {
        if (id == null)
        {
            logger.warn("No unique id has been set on this registry");
            id = UUID.getUUID();
        }
        try
        {
            doInitialise();
        }
        catch (InitialisationException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new InitialisationException(e, this);
        }
        try
        {
            fireLifecycle(Initialisable.PHASE_NAME);
        }
        catch (InitialisationException e)
        {
            throw e;
        }
        catch (LifecycleException e)
        {
            throw new InitialisationException(e, this);
        }
    }

    public RegistryLifecycleManager getLifecycleManager()
    {
        return lifecycleManager;
    }

    @Override
    public void fireLifecycle(String phase) throws LifecycleException
    {
        //Implicitly call stop if necessary when disposing
        if(Disposable.PHASE_NAME.equals(phase) && lifecycleManager.getState().isStarted())
        {
            getLifecycleManager().fireLifecycle(Stoppable.PHASE_NAME);
        }
        getLifecycleManager().fireLifecycle(phase);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key)
    {
        return (T) lookupObject(key); // do not remove this cast, the CI server fails to compile the code without it
    }

    @Override
    public <T> T lookupObject(Class<T> type) throws RegistrationException
    {
        // Accumulate objects from all registries.
        Collection<T> objects = lookupObjects(type);

        if (objects.size() == 1)
        {
            return objects.iterator().next();
        }
        else if (objects.size() > 1)
        {
            throw new RegistrationException(MessageFactory.createStaticMessage("More than one object of type " + type + " registered but only one expected."));
        }
        else
        {
            return null;
        }
    }

    @Override
    public <T> Collection<T> lookupObjectsForLifecycle(Class<T> type)
    {
        // By default use the normal lookup. If a registry implementation needs a
        // different lookup implementation for lifecycle it should override this
        // method
        return lookupObjects(type);
    }


    // /////////////////////////////////////////////////////////////////////////
    // Registry Metadata
    // /////////////////////////////////////////////////////////////////////////

    @Override
    public final String getRegistryId()
    {
        return id;
    }
}
