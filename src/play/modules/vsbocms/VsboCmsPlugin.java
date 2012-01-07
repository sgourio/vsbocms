package play.modules.vsbocms;

import java.util.ArrayList;
import java.util.List;

import play.Logger;
import play.Play;
import play.PlayPlugin;
import play.modules.vsbocms.beans.Article;
import play.modules.vsbocms.beans.Folder;
import play.modules.vsbocms.beans.Taggable;

public class VsboCmsPlugin extends PlayPlugin{

	private static List<Class<?>> foldersClassList = new ArrayList<Class<?>>();
	private static List<Class<?>> articlesClassList = new ArrayList<Class<?>>();
	private static List<Class<?>> taggableClassList = new ArrayList<Class<?>>();
	
	@Override
	public void afterApplicationStart() {
		Logger.info("Load VsboCmsPlugin");
		foldersClassList = new ArrayList<Class<?>>();
		articlesClassList = new ArrayList<Class<?>>();
		taggableClassList = new ArrayList<Class<?>>();
		
        for (Class<?> clazz : Play.classloader.getAllClasses()) {
        	if( !clazz.isAnonymousClass() ){
	            if (Folder.class.isAssignableFrom(clazz)) {
	                foldersClassList.add(clazz);
	            }
	            if (Article.class.isAssignableFrom(clazz)) {
	                articlesClassList.add(clazz);
	            }
	            
	            if( Taggable.class.isAssignableFrom(clazz)){
	            	taggableClassList.add(clazz);
	            }
        	}
        }
	}

	public static List<Class<?>> getFoldersClassList() {
		return foldersClassList;
	}

	public static List<Class<?>> getArticlesClassList() {
		return articlesClassList;
	}
	
	public static List<Class<?>> getTaggableClassList() {
		return taggableClassList;
	}
	
}