package test;

import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.jsf.SeamPhaseListener;
import org.jboss.seam.jsf.TransactionalSeamPhaseListener;
import org.jboss.seam.mock.SeamTest;
import org.jboss.seam.security.Identity;
import org.testng.annotations.Test;

import actions.BlogService;
import actions.SearchService;
import domain.Blog;
import domain.BlogEntry;

public class BlogTest extends SeamTest
{
      
   @Test
   public void testPost() throws Exception
   {
      new FacesRequest()
      {
         @Override
         protected void updateModelValues() throws Exception
         {
            Identity.instance().setPassword("tokyo");
         }
         @Override
         protected void invokeApplication() throws Exception
         {
            Identity.instance().authenticate();
         }
      }.run();
      
      new FacesRequest("/post.xhtml")
      {

         @Override
         protected void updateModelValues() throws Exception
         {            
            BlogEntry entry = (BlogEntry) getInstance("blogEntry");
            entry.setId("testing");
            entry.setTitle("Integration testing Seam applications is easy!");
            entry.setBody("This post is about SeamTest...");
         }
         
         @Override
         protected void invokeApplication() throws Exception
         {
            // post now returns void
            // assert invokeMethod("#{postAction.post}").equals("/index.xhtml");
            invokeMethod("#{postAction.post}");
            setOutcome("/index.xhtml");
         }
         
         @Override
         protected void afterRequest()
         {
            assert isInvokeApplicationComplete();
            assert !isRenderResponseBegun();
         }
         
      }.run();

      new NonFacesRequest("/index.xhtml")
      {

         @Override
         protected void renderResponse() throws Exception
         {
            List<BlogEntry> blogEntries = ( (Blog) getInstance(BlogService.class) ).getBlogEntries();
            assert blogEntries.size()==4;
            BlogEntry blogEntry = blogEntries.get(0);
            assert blogEntry.getId().equals("testing");
            assert blogEntry.getBody().equals("This post is about SeamTest...");
            assert blogEntry.getTitle().equals("Integration testing Seam applications is easy!");
         }

      }.run();
      
      new FacesRequest()
      {
         @Override
         protected void invokeApplication() throws Exception
         {
            ( (EntityManager) getInstance("entityManager") ).createQuery("delete from BlogEntry where id='testing'").executeUpdate();
         }  
      }.run();

   }
   
   @Test
   public void testLatest() throws Exception
   {
      new NonFacesRequest("/index.xhtml")
      {

         @Override
         protected void renderResponse() throws Exception
         {
            assert ( (Blog) getInstance(BlogService.class) ).getBlogEntries().size()==3;
         }
         
      }.run();
   }
   
   @Test
   public void testEntry() throws Exception
   {
      new NonFacesRequest("/entry.xhtml")
      {
         
         @Override
         protected void beforeRequest()
         {
            setParameter("blogEntryId", "seamtext");
         }
         
         @Override
         protected void renderResponse() throws Exception
         {
            BlogEntry blogEntry = (BlogEntry) Contexts.getEventContext().get("blogEntry");
            assert blogEntry!=null;
            assert blogEntry.getId().equals("seamtext");

            // make sure the entry is really there
            assert blogEntry.getBody().length() > 0;
            assert blogEntry.getTitle().equals("Introducing Seam Text");
         }
         
      }.run();
   }
   
   @Test
   public void testSearch() throws Exception
   {
      String id = new FacesRequest()
      {
         
         @Override
         protected void updateModelValues() throws Exception
         {
            ( (SearchService) getInstance(SearchService.class) ).setSearchPattern("seam");
         }
         
         @Override
         protected String getInvokeApplicationOutcome()
         {
            return "/search.xhtml";
         }

         @Override
         protected void afterRequest()
         {
            assert !isRenderResponseBegun();
         }
         
      }.run();

      new NonFacesRequest("/search.xhtml", id)
      {

         @Override
         protected void beforeRequest()
         {
            setParameter("searchPattern", "seam text");
         }
         
         @Override
         protected void renderResponse() throws Exception
         {
            List<BlogEntry> results = (List<BlogEntry>) getInstance("searchResults");
            assert results.size()==1;
         }
         
      }.run();
   }

    @Override
    protected SeamPhaseListener createPhaseListener()
    {
        return new TransactionalSeamPhaseListener();
    }
}
