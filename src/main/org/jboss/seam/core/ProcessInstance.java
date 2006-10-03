/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.core;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.Unwrap;
import org.jboss.seam.contexts.Contexts;

/**
 * A Seam component that allows injection of the current
 * jBPM ProcessInstance.
 * 
 * @author Gavin King
 * @version $Revision$
 */
@Scope(ScopeType.APPLICATION)
@Name("processInstance")
@Startup
public class ProcessInstance 
{
   
   @Unwrap
   @Transactional
   public org.jbpm.graph.exe.ProcessInstance getProcessInstance()
   {
      if ( !Contexts.isConversationContextActive() ) return null;
      
      Long processId = BusinessProcess.instance().getProcessId();
      if (processId!=null)
      {
         //TODO: do we need to cache this??
         //TODO: use getProcessInstance(), which returns null!
         return ManagedJbpmContext.instance().getGraphSession().loadProcessInstance(processId);
      }
      else
      {
         return null;
      }
   }
   
   public static org.jbpm.graph.exe.ProcessInstance instance()
   {
      if ( !Contexts.isConversationContextActive() || !BusinessProcess.instance().hasCurrentProcess() ) return null; //so we don't start a txn
      
      if ( !Contexts.isApplicationContextActive() )
      {
         throw new IllegalStateException("No active application context");
      }
      return (org.jbpm.graph.exe.ProcessInstance) Component.getInstance(ProcessInstance.class, ScopeType.APPLICATION, true);
   }
}
