/*
 * Copyright 2009 www.scribble.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.scribble.protocol.designer.osgi;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.scribble.protocol.designer.DesignerServices;
import org.scribble.protocol.designer.editor.ScribblePartitionScanner;
import org.scribble.protocol.designer.editor.lang.ScribbleCodeScanner;
import org.scribble.protocol.designer.editor.util.ScribbleColorProvider;
import org.scribble.protocol.designer.validator.ProtocolValidator;
import org.scribble.protocol.export.ProtocolExportManagerFactory;
import org.scribble.protocol.export.monitor.MonitorProtocolExporter;
import org.scribble.protocol.export.text.TextProtocolExporter;
import org.scribble.protocol.monitor.ProtocolMonitor;
import org.scribble.protocol.parser.ProtocolParser;
import org.scribble.protocol.parser.ProtocolParserManager;
import org.scribble.protocol.projection.ProtocolProjector;
import org.scribble.protocol.validation.DefaultProtocolValidationManager;
import org.scribble.protocol.validation.ProtocolValidationManager;

/**
 * The activator class controls the plug-in life cycle.
 */
public class Activator extends AbstractUIPlugin {
    
    private static Logger logger = Logger.getLogger("org.scribble.protocol.designer");

    /**
     * Scribble partitioning.
     */
    public final static String SCRIBBLE_PARTITIONING= "__scribble_partitioning";   //$NON-NLS-1$
    
    private ScribblePartitionScanner _fPartitionScanner;
    private ScribbleColorProvider _fColorProvider;
    private ScribbleCodeScanner _fCodeScanner;
    
    private ProtocolValidator _validator=new ProtocolValidator();

    /**
     * Plugin id.
     */
    public static final String PLUGIN_ID = "org.scribble.protocol.designer";

    // The shared instance
    private static Activator plugin;
    
    /**
     * The constructor.
     */
    public Activator() {
    }

    /**
     * {@inheritDoc}
     */
    public void start(final BundleContext context) throws Exception {
        super.start(context);
        plugin = this;

        // Make sure any bundles, associated with scribble, are started (excluding
        // the designer itself)
        Bundle[] bundles=context.getBundles();
        for (int i=0; i < bundles.length; i++) {
            Bundle bundle=bundles[i];
            if (bundle != null) {
                if (bundle.getSymbolicName().startsWith("org.scribble.")
                        && !bundle.getSymbolicName().endsWith("designer")) {
                    logger.fine("Pre-empt bundle start: "+bundle);
                    bundle.start();
                }
            }
        }

        // Obtain reference to protocol parser
        ServiceReference<?> sr=context.getServiceReference(ProtocolParserManager.class.getName());
        ProtocolParserManager pp=null;
        if (sr != null) {
            pp = (ProtocolParserManager)context.getService(sr);
        }            
        if (pp != null) {
            DesignerServices.setParserManager(pp);                
        } else {         
            ServiceListener sl1 = new ServiceListener() {
                public void serviceChanged(ServiceEvent ev) {
                    ServiceReference<?> sr = ev.getServiceReference();
                    switch(ev.getType()) {
                    case ServiceEvent.REGISTERED:
                        ProtocolParserManager pp=
                            (ProtocolParserManager)context.getService(sr);
                        DesignerServices.setParserManager(pp);
                        break;
                    case ServiceEvent.UNREGISTERING:
                        break;
                    }
                }
            };
                  
            String filter1 = "(objectclass=" + ProtocolParser.class.getName() + ")";            
            try {
                context.addServiceListener(sl1, filter1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // Obtain reference to protocol validation manager
        sr=context.getServiceReference(ProtocolValidationManager.class.getName());
        ProtocolValidationManager pvm=null;
        if (sr != null) {
            pvm = (ProtocolValidationManager)context.getService(sr);
        }            
        if (pvm != null) {
            DesignerServices.setValidationManager(pvm);                
        } else {         
            ServiceListener sl1 = new ServiceListener() {
                public void serviceChanged(ServiceEvent ev) {
                    ServiceReference<?> sr = ev.getServiceReference();
                    switch(ev.getType()) {
                    case ServiceEvent.REGISTERED:
                        ProtocolValidationManager pvm=
                            (ProtocolValidationManager)context.getService(sr);
                        DesignerServices.setValidationManager(pvm);
                        break;
                    case ServiceEvent.UNREGISTERING:
                        break;
                    }
                }
            };
                  
            String filter1 = "(objectclass=" + ProtocolValidationManager.class.getName() + ")";            
            try {
                context.addServiceListener(sl1, filter1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // Obtain reference to protocol projector
        sr=context.getServiceReference(ProtocolProjector.class.getName());   
        ProtocolProjector ppj=null;        
        if (sr != null) {
            ppj = (ProtocolProjector)context.getService(sr);
        }
        if (ppj != null) {
            DesignerServices.setProtocolProjector(ppj);                
        } else {           
            ServiceListener sl1 = new ServiceListener() {
                public void serviceChanged(ServiceEvent ev) {
                    ServiceReference<?> sr = ev.getServiceReference();
                    switch(ev.getType()) {
                    case ServiceEvent.REGISTERED:
                        ProtocolProjector ppj=
                            (ProtocolProjector)context.getService(sr);
                        DesignerServices.setProtocolProjector(ppj);
                        break;
                    case ServiceEvent.UNREGISTERING:
                        break;
                    }
                }
            };
                  
            String filter1 = "(objectclass=" + ProtocolProjector.class.getName() + ")";           
            try {
                context.addServiceListener(sl1, filter1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // Create the export manager
        DesignerServices.setProtocolExportManager(ProtocolExportManagerFactory.getExportManager());
        ProtocolExportManagerFactory.getExportManager().getExporters().add(new TextProtocolExporter());
        ProtocolExportManagerFactory.getExportManager().getExporters().add(new MonitorProtocolExporter());
        
        // Register protocol monitor
        sr=context.getServiceReference(ProtocolMonitor.class.getName());
        
        ProtocolMonitor pm=null;        
        if (sr != null) {
            pm = (ProtocolMonitor)context.getService(sr);
        }            
        if (pm != null) {
            DesignerServices.setProtocolMonitor(pm);                
        } else {            
            ServiceListener sl1 = new ServiceListener() {
                public void serviceChanged(ServiceEvent ev) {
                    ServiceReference<?> sr = ev.getServiceReference();
                    switch(ev.getType()) {
                    case ServiceEvent.REGISTERED:
                        ProtocolMonitor pm=
                            (ProtocolMonitor)context.getService(sr);
                        DesignerServices.setProtocolMonitor(pm);
                        break;
                    case ServiceEvent.UNREGISTERING:
                        break;
                    }
                }
            };
                  
            String filter1 = "(objectclass=" + ProtocolMonitor.class.getName() + ")";            
            try {
                context.addServiceListener(sl1, filter1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // Register resource change listener
        IResourceChangeListener rcl=new IResourceChangeListener() {
            public void resourceChanged(IResourceChangeEvent evt) {       
                try {
                    evt.getDelta().accept(new IResourceDeltaVisitor() {                        
                        public boolean visit(IResourceDelta delta) {
                            boolean ret=true;
                            IResource res = delta.getResource();                           
                            // Determine if the change is relevant
                            if (isChangeRelevant(res, delta)) {
                                // Validate the resource
                                _validator.validateResource(res);
                            }
                            return (ret);
                        }
                     });
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Failed to process resource change event", e);
                }
            }
        };
        
        // Register the resource change listener
        ResourcesPlugin.getWorkspace().addResourceChangeListener(rcl,
                IResourceChangeEvent.POST_CHANGE);        
    }
    
    /**
     * This method determines if the change is relevant.
     * 
     * @param res The resource
     * @param delta The delta
     * @return Whether the change is relevant
     */
    protected boolean isChangeRelevant(IResource res, IResourceDelta delta) {
        return (res instanceof IFile
                && DesignerServices.PROTOCOL_FILE_EXTENSION.equals(((IFile)res).getFileExtension())
                && (((delta.getFlags() & IResourceDelta.CONTENT) != 0)
                        || delta.getKind() == IResourceDelta.ADDED));
    }

    /**
     * {@inheritDoc}
     */
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance.
     *
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    /**
     * This method logs an error against the plugin.
     * 
     * @param mesg The error message
     * @param t The optional exception
     */
    public static void logError(String mesg, Throwable t) {
        
        if (getDefault() != null) {
            Status status=new Status(IStatus.ERROR,
                    PLUGIN_ID, 0, mesg, t);
            
            getDefault().getLog().log(status);
        }
        
        logger.severe("LOG ERROR: "+mesg+(t == null ? "" : ": "+t));
    }
    
    /**
     * Return a scanner for creating Java partitions.
     * 
     * @return a scanner for creating Java partitions
     */
    public ScribblePartitionScanner getScribblePartitionScanner() {
        if (_fPartitionScanner == null) {
            _fPartitionScanner= new ScribblePartitionScanner();
        }
        return _fPartitionScanner;
    }
    
    /**
     * Returns the singleton Java code scanner.
     * 
     * @return the singleton Java code scanner
     */
    public RuleBasedScanner getScribbleCodeScanner() {
         if (_fCodeScanner == null) {
            _fCodeScanner= new ScribbleCodeScanner(getScribbleColorProvider());
         }
         return _fCodeScanner;
    }
    
    /**
     * Returns the singleton Java color provider.
     * 
     * @return the singleton Java color provider
     */
    public ScribbleColorProvider getScribbleColorProvider() {
         if (_fColorProvider == null) {
            _fColorProvider= new ScribbleColorProvider();
         }
        return _fColorProvider;
    }
}
