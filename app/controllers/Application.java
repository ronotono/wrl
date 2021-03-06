package controllers;
import services.BookmarkService;

import org.springframework.stereotype.Component;

import play.*;
import play.mvc.*;
import play.data.Form;
import play.libs.Json;

import views.html.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import models.Bookmark;
import models.BookmarkForm;

import javax.inject.Inject;

import status.StatusCode;

@Component
public class Application extends Controller {
	
	@Inject private BookmarkService bookmarkService;
	
    private static final Logger log = LoggerFactory.getLogger(Application.class);
	
    public Result index() {
    	return ok(index.render(Form.form(models.BookmarkForm.class), bookmarkService.getAllBookmarks()));
    }
    
    public Result addBookmark() {
        final Form<BookmarkForm> form = Form.form(models.BookmarkForm.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest(index.render(form, bookmarkService.getAllBookmarks()));
        }

        BookmarkForm bookmarkForm = form.get();
        Bookmark bookmark = new Bookmark(bookmarkForm.getUrl(), bookmarkForm.getDesc(), false);
        
        StatusCode status = bookmarkService.addBookmark(bookmark);
     
        if (!status.getSuccess()) {
        	if (status == StatusCode.ADD_DUPLICATE_BOOKMARK) {
        		log.error("{}", status.getDescription());
        		form.reject("url", status.toString());
        	}
        	return badRequest(index.render(form, bookmarkService.getAllBookmarks()));
        }
        
        log.info("{}", status.getDescription());
        return redirect(routes.Application.index());        
    }
    
    public Result removeBookmark(Long id) {
    	StatusCode status = bookmarkService.removeBookmarkById(id);
    	if (!status.getSuccess()) {
    		log.info("{}, id = {}", status.getDescription(), id);
    		return badRequest(index.render(Form.form(models.BookmarkForm.class), bookmarkService.getAllBookmarks()));
        }
    	
    	log.info("{} (id = {})", status.getDescription(), id);
    	return redirect(routes.Application.index());
    }
    
    public Result toggleBookmarkComplete(Long id) {
    	StatusCode status = bookmarkService.toggleBookmarkCompleteById(id);

   	 	if (!status.getSuccess()) {
   	 		log.info("{} (id = {})", status.getDescription(), id);
   	 		return badRequest(index.render(Form.form(models.BookmarkForm.class), bookmarkService.getAllBookmarks()));
   	 	}
   	 	
   	 	log.info("{} (id = {}) ", status.getDescription(), id);
   	 	return redirect(routes.Application.index());
    }
}
