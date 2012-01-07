package controllers.vsbocms;

import java.util.Date;

import play.modules.vsbocms.beans.Article;
import play.modules.vsbocms.beans.Folder;
import services.vsbocms.CmsServices;
import beans.vsbocms.TreeNode;

import com.google.gson.Gson;

public class SimpleArticle extends Cms{
	
	public static void edit(Long fatherNodeId, Long treeNodeId){
		
		String[] existingTagsTab = CmsServices.getInstance().getAllExistingTagList().toArray(new String[]{} );
		Gson gson = new Gson();
		String existingTags = gson.toJson(existingTagsTab);
		
		if( treeNodeId != null ){
			TreeNode treeNode = CmsServices.getInstance().getTreeNodeMap().get(treeNodeId);
			if( treeNode != null ){
				renderArgs.put("currentNode", treeNode);// highlith the current menu folder / or article
			}
			Article simpleArticle = (Article) treeNode.getClassifiable();
			TreeNode fatherNode = treeNode.getFather();
			render(simpleArticle, fatherNode, existingTags);
		}else{
			TreeNode fatherNode = CmsServices.getInstance().getTreeNodeMap().get(fatherNodeId);
			
			Folder currentFolder = (Folder) fatherNode.getClassifiable();
			if( currentFolder != null ){
				renderArgs.put("currentNode", currentFolder); // highlith the current menu folder / or article
			}
			render(fatherNode, existingTags);
		}
	}
	
	public static void createSimpleArticle(Long id, Long fatherNodeId, String articleName, String content, String tags){
		models.vsbocms.SimpleArticle simpleArticle = null;
		if( id != null){
			simpleArticle = models.vsbocms.SimpleArticle.findById(id);
		}else{
			simpleArticle = new models.vsbocms.SimpleArticle();
			simpleArticle.creationDate = new Date();
		}
		
		simpleArticle.articleName = articleName;
		simpleArticle.content = content;
		simpleArticle.modificationDate = new Date();
		
		String[] t = tags.trim().split(" ");
		simpleArticle.setTags(t);
		simpleArticle.save();
		
		CmsServices.getInstance().getAllExistingTagList().addAll(simpleArticle.getTagSet());
		
		if( fatherNodeId != null ){
			TreeNode father = CmsServices.getInstance().getTreeNodeMap().get(fatherNodeId);
			CmsServices.getInstance().classify(simpleArticle, father);
			
			SimpleFolder.edit(null, father.getAssociationId());
		}else{
			TreeNode father = CmsServices.getInstance().getRootNode();
			CmsServices.getInstance().classify(simpleArticle, father);
			Cms.articles(null);
		}
	}
	
	public static void tagEdit(Long id){
		String[] existingTagsTab = CmsServices.getInstance().getAllExistingTagList().toArray(new String[]{} );
		Gson gson = new Gson();
		String existingTags = gson.toJson(existingTagsTab);
		
		if( id != null ){
			models.vsbocms.SimpleArticle simpleArticle = models.vsbocms.SimpleArticle.findById(id);
			render(simpleArticle, existingTags);
		}
		render( existingTags );
	}
}
