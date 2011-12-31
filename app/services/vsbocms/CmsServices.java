package services.vsbocms;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import models.vsbocms.FolderAssociation;
import models.vsbocms.SimpleFolder;
import play.Logger;
import play.cache.Cache;
import play.modules.vsbocms.VsboCmsPlugin;
import play.modules.vsbocms.beans.Article;
import play.modules.vsbocms.beans.Classifiable;
import play.modules.vsbocms.beans.Folder;
import beans.vsbocms.TreeNode;

@SuppressWarnings("serial")
public class CmsServices implements Serializable{

	private TreeNode rootNode = null;
	private Map<Long, TreeNode> treeNodeMap = new HashMap<Long, TreeNode>();
	
	/**
	 * build the folder tree
	 * @param folderTree
	 * @param currentRoot
	 * @param folderAlreadyTraited (avoid infinite loop)
	 */
	private void buildTree(TreeNode currentRoot, List<Classifiable> folderAlreadyTraited){
		
		if( folderAlreadyTraited.contains(currentRoot.getClassifiable()) ){
			return;
		}
		folderAlreadyTraited.add(currentRoot.getClassifiable());
		List<FolderAssociation> subFolderList = FolderAssociation.findByFolderId(currentRoot.getClassifiable().getId(), currentRoot.getClassifiable().getClass().getName());
		if( subFolderList != null && !subFolderList.isEmpty() ){
			for( FolderAssociation association : subFolderList ){
				 try {
					Class<?> clazz = Class.forName(association.classOfElement);
					if( Folder.class.isAssignableFrom(clazz) ){
						Method method = clazz.getMethod("findById", Object.class);
						Folder folder = (Folder) method.invoke(null, association.elementId );
						
						TreeNode child = new TreeNode(association.id, folder, currentRoot);
						currentRoot.getChildren().add(child);
						
						buildTree(child, folderAlreadyTraited);
						
					}else if( Article.class.isAssignableFrom(clazz)  ){
						Method method = clazz.getMethod("findById", Object.class);
						Article article= (Article) method.invoke(null, association.elementId );
						
						TreeNode child = new TreeNode(association.id, article, currentRoot);
						currentRoot.getChildren().add(child);
					}
				} catch (Exception e) {
					Logger.error(e, "Error" );
				}
			}
		}
	}
	
	/**
	 * Prepare the root folder wich is not in the database
	 */
	private CmsServices() {
		SimpleFolder root = new SimpleFolder();
		root.id = 0L;
		root.folderName = "vsbocms.rootFolder";
		rootNode = new TreeNode(0L, root, null);
		buildTree(rootNode, new ArrayList<Classifiable>());
		treeToMap(rootNode);
		
	}
	
	private void treeToMap(TreeNode root){
		treeNodeMap.put(root.getAssociationId(), root);
		for( TreeNode child : root.getChildren() ){
			treeToMap(child);
		}
		
	}
	
	/**
	 * A cmsServices instance is in cache for 1 day
	 * @return
	 */
	public static CmsServices getInstance(){
		CmsServices instance = (CmsServices) Cache.get("CmsServices_instance");
		if( instance == null ){
			Lock lock = new ReentrantLock();
			try{
				lock.lock();
				instance = (CmsServices) Cache.get("CmsServices_instance");
				if( instance == null ){
					instance = new CmsServices();
					Cache.safeSet("CmsServices_instance", instance, "1d");
				}
			}finally{
				lock.unlock();
			}
			
		}
		
		return instance;
	}
	
	public TreeNode getRootNode() {
		return rootNode;
	}

	/**
	 * Get articles directly inside a folder (article are detached from database)
	 * @param folder
	 * @return
	 */
	public List<Article> getArticleFromFolder(TreeNode node){
		List<Article> result = new ArrayList<Article>();
		List<TreeNode> treeNodeList = node.getChildren();
		for(TreeNode treeNode : treeNodeList ){
			if( treeNode.getClassifiable() instanceof Article ){
				result.add((Article) treeNode.getClassifiable());
			}
		}
		return result;
	}
	
	/**
	 * Get folders directly inside a folder
	 * @param folder
	 * @return
	 */
	public List<Folder> getFolderFromFolder(TreeNode node){
		List<Folder> result = new ArrayList<Folder>();
		List<TreeNode> treeNodeList = node.getChildren();
		for(TreeNode treeNode : treeNodeList ){
			if( treeNode.getClassifiable() instanceof Folder ){
				result.add((Folder) treeNode.getClassifiable());
			}
		}
		return result;
	}
	
	/**
	 * Put a classifiable object in the folder tree
	 * @param content
	 * @param parentFolder
	 */
	public TreeNode classify(Classifiable content , TreeNode father ){
		// check if the node is already here
		List<TreeNode> children = father.getChildren();
		for( TreeNode child : children){
			if( child.getClassifiable()!= null 
					&& child.getClassifiable().getId().longValue() == content.getId().longValue() 
					&& child.getClassifiable().getClass().getName().equals(content.getClass().getName())){
				child.setClassifiable(content); // update the content
				return child;
			}
		}
		
		FolderAssociation folderAssociation = new FolderAssociation();
		folderAssociation.classOfElement = content.getClass().getName();
		folderAssociation.elementId = content.getId();
		folderAssociation.classOfFolder = father.getClassifiable().getClass().getName();
		folderAssociation.folderId = father.getClassifiable().getId();
		folderAssociation.save();
		
		TreeNode child = new TreeNode(folderAssociation.id, content, father);
		father.getChildren().add( child );
		treeNodeMap.put(child.getAssociationId(), child);
		return child;
	}
	
	
	/**
	 * List of different folder classes existing in the project
	 * @return
	 */
	public List<Class<?>> getFolderClassList(){
		return VsboCmsPlugin.getFoldersClassList();
	}
	
	/**
	 * List of different articles classes existing in the project
	 * @return
	 */
	public List<Class<?>> getArticleClassList(){
		return VsboCmsPlugin.getArticlesClassList();
	}
	
	/**
	 * Delete the classifiable object in the node.
	 * If it's a folder, the object inside are also deleted
	 * @param classifiable
	 */
	public void delete(TreeNode treeNode){
		recursiveDelete(treeNode);
		treeNode.getFather().getChildren().remove(treeNode); // detach the node
	}
	
	private void recursiveDelete(TreeNode treeNode){
		if( treeNode.getClassifiable() != null && treeNode.getClassifiable() instanceof Folder){
			for( TreeNode cl :  treeNode.getChildren()){
				recursiveDelete(cl);
			}
			List<FolderAssociation> folderAssociationList = FolderAssociation.findByFolderId(treeNode.getClassifiable().getId(), treeNode.getClassifiable().getClass().getName());
			for( FolderAssociation fa : folderAssociationList ){
				fa.delete();
			}
			treeNode.getChildren().clear();
		}
		try{
			Classifiable cl = treeNode.getClassifiableFromDatabase();
			if( cl != null ){
				cl.delete();
			}
		} catch (Exception e) {
			Logger.error(e,"");
		}
	}

	public Map<Long, TreeNode> getTreeNodeMap() {
		return treeNodeMap;
	}
	
}

