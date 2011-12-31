package controllers.vsbocms;

import play.modules.vsbocms.beans.Article;
import play.modules.vsbocms.beans.Folder;
import services.vsbocms.CmsServices;
import beans.vsbocms.TreeNode;

public class SimpleArticle extends Cms{
	
	public static void edit(Long fatherNodeId, Long treeNodeId){
		
		if( treeNodeId != null ){
			TreeNode treeNode = CmsServices.getInstance().getTreeNodeMap().get(treeNodeId);
			if( treeNode != null ){
				renderArgs.put("currentNode", treeNode);// highlith the current menu folder / or article
			}
			Article simpleArticle = (Article) treeNode.getClassifiable();
			TreeNode fatherNode = treeNode.getFather();
			render(simpleArticle, fatherNode);
		}else{
			TreeNode fatherNode = CmsServices.getInstance().getTreeNodeMap().get(fatherNodeId);
			
			Folder currentFolder = (Folder) fatherNode.getClassifiable();
			if( currentFolder != null ){
				renderArgs.put("currentNode", currentFolder); // highlith the current menu folder / or article
			}
			render(fatherNode);
		}
	}
	
	public static void createSimpleArticle(Long id, Long fatherNodeId, String articleName, String title, String content){
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
		
		TreeNode father = CmsServices.getInstance().getTreeNodeMap().get(fatherNodeId);
		CmsServices.getInstance().classify(simpleArticle, father);
		
		SimpleFolder.edit(null, father.getAssociationId());
	}
}
