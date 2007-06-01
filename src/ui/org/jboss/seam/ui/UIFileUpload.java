package org.jboss.seam.ui;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.el.ValueExpression;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.servlet.ServletRequest;

import org.jboss.seam.web.MultipartRequest;

/**
 * A file upload component.
 * 
 * @author Shane Bryzak
 */
public class UIFileUpload extends UIInput
{
   public static final String COMPONENT_TYPE   = "org.jboss.seam.ui.UIFileUpload";
   public static final String COMPONENT_FAMILY = "org.jboss.seam.ui.FileUpload";
      
   private String accept;
   private String styleClass;
   private String style;   
   
   private MultipartRequest request;
   
   @Override
   public void decode(FacesContext context)
   {
      super.decode(context);
      
      ServletRequest request = (ServletRequest) context.getExternalContext().getRequest();
      
      if (!(request instanceof MultipartRequest))
      {
         request = unwrapMultipartRequest(request);
      }

      if (request instanceof MultipartRequest)
      {
         this.request = (MultipartRequest) request;
         
         String clientId = getClientId(context);         
         
         this.request.getFileName(clientId);   
      }      
      
      if (request == null)
      {
         throw new IllegalStateException("Request is not an instance of MultipartRequest, " +
                  "or does not wrap it.  Please ensure that SeamFilter is installed in web.xml");
      }
   }
   
   @Override
   public void processUpdates(FacesContext context)
   {                         
      if (request != null)
      {
         String clientId = getClientId(context);
         String contentType = request.getFileContentType(clientId);
         String fileName = request.getFileName(clientId);
         int fileSize = request.getFileSize(clientId);       
         
         ValueExpression dataBinding = getValueExpression("data");
         if (dataBinding != null)
         {
            Class cls = dataBinding.getType(context.getELContext());
            if (cls.isAssignableFrom(InputStream.class))
            {
               dataBinding.setValue(context.getELContext(), request.getFileInputStream(clientId));
            }
            else if (cls.isAssignableFrom(byte[].class))
            {
               dataBinding.setValue(context.getELContext(), request.getFileBytes(clientId));
            }
         }
         
         ValueExpression vb = getValueExpression("contentType");
         if (vb != null)
            vb.setValue(context.getELContext(), contentType);
         
         vb = getValueExpression("fileName");
         if (vb != null)
            vb.setValue(context.getELContext(), fileName);
         
         vb = getValueExpression("fileSize");
         if (vb != null)
            vb.setValue(context.getELContext(), fileSize);     
      }
   }   
      
   /**
    * Finds an instance of MultipartRequest wrapped within a request or its
    * (recursively) wrapped requests. 
    */
   private ServletRequest unwrapMultipartRequest(ServletRequest request)
   {      
      while (!(request instanceof MultipartRequest))
      {
         boolean found = false;
         
         for (Method m : request.getClass().getMethods())
         {
            if (ServletRequest.class.isAssignableFrom(m.getReturnType()) && 
                m.getParameterTypes().length == 0)
            {
               try
               {
                  request = (ServletRequest) m.invoke(request);
                  found = true;
                  break;
               }
               catch (Exception ex) { /* Ignore, try the next one */ } 
            }
         }
         
         if (!found)
         {
            for (Field f : request.getClass().getDeclaredFields())
            {
               if (ServletRequest.class.isAssignableFrom(f.getType()))
               {
                  try
                  {
                     request = (ServletRequest) f.get(request);
                  }
                  catch (Exception ex) { /* Ignore */}
               }
            }
         }
         
         if (!found) break;
      }
      
      return request;     
   }
      
   @Override
   public void encodeEnd(FacesContext context) 
      throws IOException
   {
      super.encodeEnd(context);

      ResponseWriter writer = context.getResponseWriter();
      writer.startElement(HTML.INPUT_ELEM, this);      
      writer.writeAttribute(HTML.TYPE_ATTR, HTML.FILE_ATTR, null);      
      
      String clientId = this.getClientId(context);      
      writer.writeAttribute(HTML.ID_ATTR, clientId, null);     
      writer.writeAttribute(HTML.NAME_ATTR, clientId, null);
      
      ValueExpression vb = getValueExpression("accept");
      if (vb != null)
      {
         writer.writeAttribute(HTML.ACCEPT_ATTR, vb.getValue(context.getELContext()), null);
      }
      else if (accept != null)
      {
         writer.writeAttribute(HTML.ACCEPT_ATTR, accept, null);
      }
      
      if (styleClass != null)
      {
         writer.writeAttribute(HTML.CLASS_ATTR, styleClass, null);
      }
      
      if (style != null)
      {
         writer.writeAttribute(HTML.STYLE_ATTR, style,  null);
      }
      
      writer.endElement(HTML.INPUT_ELEM);
   }
   
   @Override
   public String getFamily()
   {
      return COMPONENT_FAMILY;
   }

   public String getAccept()
   {
      return accept;
   }

   public void setAccept(String accept)
   {
      this.accept = accept;
   }

   public String getStyle()
   {
      return style;
   }

   public void setStyle(String style)
   {
      this.style = style;
   }

   public String getStyleClass()
   {
      return styleClass;
   }

   public void setStyleClass(String styleClass)
   {
      this.styleClass = styleClass;
   }

}
