package controllers.vsbocms;

import play.modules.vsbocms.beans.Classifiable;
import play.modules.vsbocms.beans.Folder;
import services.CmsServices;

public class SimpleArticle extends Cms{
	
	public static void edit(Long currentFolderId, String currentFolderClass, Long id){
		if( currentFolderId != null ){
			Folder currentFolder = (Folder) CmsServices.getInstance().getContentIdMap().get(currentFolderClass + "_" + currentFolderId);
			if( currentFolder != null ){
				renderArgs.put("currentFolder", currentFolder);
			}
		}
		
		if( id == null){
			render();
		}else{
			models.vsbocms.SimpleArticle simpleArticle = models.vsbocms.SimpleArticle.findById(id);
			render(simpleArticle);
		}
	}
	
	public static void createSimpleArticle(Long id, String articleName, String title, String content, String parentFolderClass, Long parentFolderId){
		models.vsbocms.SimpleArticle simpleArticle = null;
		if( id != null){
			simpleArticle = models.vsbocms.SimpleArticle.findById(id);
		}else{
			simpleArticle = new models.vsbocms.SimpleArticle();
		}
		
		simpleArticle.articleName = articleName;
		simpleArticle.content = content;
		simpleArticle.title = title;
		simpleArticle.save();
		
		Classifiable parentFolder = CmsServices.getInstance().getContentIdMap().get(parentFolderClass + "_" + parentFolderId);
		CmsServices.getInstance().classify(simpleArticle, parentFolder);
		
		folder(parentFolderId , parentFolder.getClass().getName());
	}
}
