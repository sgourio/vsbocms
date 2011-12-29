package controllers.vsbocms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import play.modules.vsbo.Menu;
import play.modules.vsbo.SubMenu;
import play.modules.vsbocms.beans.Classifiable;
import play.modules.vsbocms.beans.Folder;
import play.mvc.Before;
import services.CmsServices;
import controllers.vsbo.Backoffice;

@Menu(id="menu-vsbocms")
public class Cms extends Backoffice{

	@Before
	public static void prepare(){
		Map<Classifiable, List<Classifiable>> folderTree = CmsServices.getInstance().getFolderTree();
		Folder root = CmsServices.getInstance().getRootFolder();
		renderArgs.put("root", root);
		renderArgs.put("folderTree", folderTree);
		
		String idFolder = params.get("currentFolderId");
		if( StringUtils.isNumeric(idFolder) ){
			Long currentFolderId = Long.valueOf(idFolder);
			Folder currentFolder = (Folder) CmsServices.getInstance().getContentIdMap().get(currentFolderId);
			if( currentFolder != null ){
				renderArgs.put("currentFolder", currentFolder);
			}
		}
	}
	
	@SubMenu
	public static void index(){
		render();
	}
	
	public static void folder(Long currentFolderId){
		List<Class<?>> folderClassList = CmsServices.getInstance().getFolderClassList(); 
		Map<String, String> urlParameters = new HashMap<String, String>();
		urlParameters.put("currentFolderId", params.get("currentFolderId"));
		render(folderClassList, urlParameters);
	}
	
	
	@SubMenu
	public static void article(){
		render();
	}
}
