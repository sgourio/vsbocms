package services.vsbocms;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import play.modules.vsbocms.beans.Taggable;
import beans.vsbocms.TreeNode;

@SuppressWarnings("serial")
public class CmsServices implements Serializable{

	private TreeNode rootNode = null;
	private Map<Long, TreeNode> treeNodeMap = new HashMap<Long, TreeNode>();
	
	public static final String CACHE_KEY = "CmsServices_instance";
	
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
						
						if( folder != null ){
							TreeNode child = new TreeNode(association.id, folder, currentRoot);
							currentRoot.getChildren().add(child);
							buildTree(child, folderAlreadyTraited);
						}
						
					}else if( Article.class.isAssignableFrom(clazz)  ){
						Method method = clazz.getMethod("findById", Object.class);
						Article article= (Article) method.invoke(null, association.elementId );
						if( article != null ){
							TreeNode child = new TreeNode(association.id, article, currentRoot);
							currentRoot.getChildren().add(child);
						}
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
		CmsServices instance = (CmsServices) Cache.get(CACHE_KEY);
		if( instance == null ){
			Lock lock = new ReentrantLock();
			try{
				lock.lock();
				instance = (CmsServices) Cache.get(CACHE_KEY);
				if( instance == null ){
					instance = new CmsServices();
					Cache.safeSet(CACHE_KEY, instance, "1d");
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
	 * Get articles of type A directly inside a folder (article are detached from database)
	 * @param folder
	 * @return
	 */
	 @SuppressWarnings("unchecked")
	private <A extends Article> Set<A> getArticleFromFolder(TreeNode node, Class<A> articleClass){
		 Set<A> result = new HashSet<A>();
		List<TreeNode> treeNodeList = node.getChildren();
		for(TreeNode treeNode : treeNodeList ){
			if( articleClass.isAssignableFrom(treeNode.getClassifiable().getClass()) ){
				result.add((A) treeNode.getClassifiable());
			}
		}
		return result;
	}
	 
	 /**
	  * Return a tree node from an Association
	  * @param association
	  * @return
	  */
	 private TreeNode getTreeNodeFromAssociation(FolderAssociation association){
		 return treeNodeMap.get(association.getId());
	 }
	 
	 /**
	  * Get the tree node list for a folder
	  * @param folder
	  * @return Set<TreeNode>
	  */
	 private Set<TreeNode> getTreeNodeForFolder(Folder folder){
		 List<FolderAssociation> folderAssociationList = FolderAssociation.findByElementId(folder.getId(), folder.getClass().getName());
		 Set<TreeNode> treeNodeList = new HashSet<TreeNode>();
		 for( FolderAssociation association : folderAssociationList ){
			 TreeNode treeNode = getTreeNodeFromAssociation(association);
			 if( treeNode != null ){
				 treeNodeList.add(treeNode);
			 }
		 }
		 return treeNodeList;
	 }
	 
	 /**
	  * Get articles of type A directly inside a folder (article are detached from database)
	  * @param <A>
	  * @param folder
	  * @param articleClass
	  * @return
	  */
	 public <A extends Article> Set<A> getArticleFromFolder(Folder folder, Class<A> articleClass){
		 Set<A> result = new HashSet<A>();
		 
		 Set<TreeNode> treeNodeSet = getTreeNodeForFolder(folder);
		 
		 for( TreeNode tn : treeNodeSet){
			 result.addAll( getArticleFromFolder(tn,articleClass) );
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
	 * List of different articles classes existing in the project
	 * @return
	 */
	public List<Class<?>> getTaggableClassList(){
		return VsboCmsPlugin.getTaggableClassList();
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
	
	private Set<String> allTagSet = null ;
	private Map<String, Set<Taggable>> taggableArticleMap = null; 
	
	/**
	 * Get all existing tag in the database
	 * @return
	 */
	public Set<String> getAllExistingTagList(){
		if( allTagSet == null ){
			Lock lock = new ReentrantLock();
			try{
				lock.lock();
				if( allTagSet == null ){
					buildAllExistingTagList();
				}
			}finally{
				lock.unlock();
			}
		}
		return allTagSet;
	}
	
	public Map<String, Set<Taggable>> getTaggableArticleMap(){
		if( taggableArticleMap == null ){
			Lock lock = new ReentrantLock();
			try{
				lock.lock();
				if( taggableArticleMap == null ){
					buildAllExistingTagList();
				}
			}finally{
				lock.unlock();
			}
		}
		return taggableArticleMap;
	}
	
	
	private void buildAllExistingTagList(){
		allTagSet = new HashSet<String>();
		taggableArticleMap = new HashMap<String, Set<Taggable>>();
		
		for( Class<?> clazz : getTaggableClassList() ){
			try {
				Method method = clazz.getMethod("findAll");
				@SuppressWarnings("unchecked")
				List<Taggable> taggableList = (List<Taggable>) method.invoke(null);
				for( Taggable article : taggableList ){
					allTagSet.addAll(article.getTagSet());
					for( String currentTag : article.getTagSet()){
						Set<Taggable> tagList = taggableArticleMap.get(currentTag);
						if( tagList == null ){
							tagList = new HashSet<Taggable>();
							taggableArticleMap.put(currentTag, tagList);
						}
						tagList.add(article);
					}
				}
			} catch (Exception e) {
				Logger.error(e, "Method findAll not found for class " + clazz.getName());
			}
		}
	}
	
	/**
	 * Refresh tag list with the new article
	 * @param taggable
	 */
	public void refreshTaggableList(Taggable taggable){
		Set<String> myTags = taggable.getTagSet();
		for( String tag : myTags ){
			allTagSet.add(tag);
			Set<Taggable> tagList = taggableArticleMap.get(tag);
			if( tagList == null ){
				tagList = new HashSet<Taggable>();
				taggableArticleMap.put(tag, tagList);
			}
			tagList.add(taggable);
		}
		
	}
}

