package controllers.vsbocms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.modules.vsbocms.beans.Folder;
import services.vsbocms.CmsServices;
import beans.vsbocms.TreeNode;

public class SimpleFolder extends Cms{

	public static void edit(Long fatherNodeId, Long treeNodeId){
		
		if( treeNodeId != null ){
			TreeNode treeNode = CmsServices.getInstance().getTreeNodeMap().get(treeNodeId);
			if( treeNode != null ){
				renderArgs.put("currentNode", treeNode); // highlith the current menu folder
			}
			Folder simpleFolder = (Folder) treeNode.getClassifiable();
			List<Class<?>> folderClassList = CmsServices.getInstance().getFolderClassList();
			List<Class<?>> articleClassList = CmsServices.getInstance().getArticleClassList();

			Map<String, Object> actionParametersMap = new HashMap<String, Object>();
			actionParametersMap.put("fatherNodeId", treeNodeId);
			render(simpleFolder, folderClassList, articleClassList, actionParametersMap, treeNode);
		}else{
			TreeNode treeNode = CmsServices.getInstance().getTreeNodeMap().get(fatherNodeId);
			if( treeNode != null ){
				renderArgs.put("currentNode", treeNode); // highlith the current menu folder
			}
			render(treeNode, fatherNodeId);
		}
	}
	
	public static void createSimpleFolder(Long id, Long fatherNodeId, String folderName){
		models.vsbocms.SimpleFolder simpleFolder = null;
		if( id != null){
			simpleFolder = models.vsbocms.SimpleFolder.findById(id);
		}else{
			simpleFolder = new models.vsbocms.SimpleFolder();
		}
		
		simpleFolder.folderName = folderName;
		simpleFolder.save();
		
		TreeNode fatherNode = CmsServices.getInstance().getTreeNodeMap().get(fatherNodeId);
		TreeNode child = CmsServices.getInstance().classify(simpleFolder, fatherNode);
		
		edit(fatherNodeId, child.getAssociationId() );
	}
	
	
	public static void delete(Long treeNodeId){
		if( treeNodeId != null){
			TreeNode treeNode = CmsServices.getInstance().getTreeNodeMap().get(treeNodeId);
			CmsServices.getInstance().delete(treeNode);
		}
		edit(0L, null);
	}
}
