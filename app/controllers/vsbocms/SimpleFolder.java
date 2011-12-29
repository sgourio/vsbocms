package controllers.vsbocms;

import play.modules.vsbocms.beans.Classifiable;
import play.modules.vsbocms.beans.Folder;
import services.CmsServices;

public class SimpleFolder extends Cms{

	public static void edit(Long currentFolderId, Long id){
		if( currentFolderId != null ){
			Folder currentFolder = (Folder) CmsServices.getInstance().getContentIdMap().get(currentFolderId);
			if( currentFolder != null ){
				renderArgs.put("currentFolder", currentFolder);
			}
		}
		
		if( id == null){
			render();
		}else{
			models.vsbocms.SimpleFolder simpleFolder = models.vsbocms.SimpleFolder.findById(id);
			render(simpleFolder);
		}
	}
	
	public static void createSimpleFolder(String folderName, Long parentFolderId){
		models.vsbocms.SimpleFolder simpleFolder = new models.vsbocms.SimpleFolder();
		simpleFolder.folderName = folderName;
		simpleFolder.save();
		
		Classifiable parentFolder = CmsServices.getInstance().getContentIdMap().get(parentFolderId);
		CmsServices.getInstance().classify(simpleFolder, parentFolder);
		
		folder(simpleFolder.id );
	}
}
