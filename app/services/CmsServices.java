package services;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import models.vsbocms.FolderAssociation;
import play.Logger;
import play.modules.vsbocms.VsboCmsPlugin;
import play.modules.vsbocms.beans.Classifiable;
import play.modules.vsbocms.beans.Folder;


public class CmsServices {

	private static CmsServices instance = new CmsServices();
	
	private Folder rootFolder = new Folder() {
		@Override
		public Long getId() {
			return 0L;
		}
		@Override
		public String getName() {
			return "vsbocms.rootFolder";
		}
		@Override
		public int compareTo(Classifiable o) {
			return o.getName().compareTo(this.getName());
		}
	};
	
	private Map<Classifiable, List<Classifiable>> contentTree = Collections.synchronizedSortedMap(new TreeMap<Classifiable, List<Classifiable>>());
	private Map<Long, Classifiable> contentIdMap = Collections.synchronizedMap(new HashMap<Long, Classifiable>());
	
	private CmsServices() {
	}
	
	public static CmsServices getInstance(){
		return instance;
	}
	
	public Map<Classifiable, List<Classifiable>> getFolderTree(){
		if( contentTree.isEmpty() ){
			Lock lock = new ReentrantLock();
			try{
				lock.lock();
				buildFolderTree(contentTree, rootFolder, new ArrayList<Long>());
			}finally{
				lock.unlock();
			}
		}
		return contentTree;
	}
	
	/**
	 * build the folder tree
	 * @param folderTree
	 * @param currentRoot
	 * @param folderAlreadyTraited (avoid infinite loop)
	 */
	private void buildFolderTree(final Map<Classifiable, List<Classifiable>> folderTree, Classifiable currentRoot, List<Long> folderAlreadyTraited){
		
		if( folderAlreadyTraited.contains(currentRoot.getId()) ){
			return;
		}
		folderAlreadyTraited.add(currentRoot.getId());
		
		List<FolderAssociation> subFolderList = FolderAssociation.findByFolderId(currentRoot.getId());
		if( subFolderList != null && !subFolderList.isEmpty() ){
			for( FolderAssociation association : subFolderList ){
				 try {
					Class<?> clazz = Class.forName(association.classOfElement);
					if( Folder.class.isAssignableFrom(clazz) ){
						Method method = clazz.getMethod("findById", Object.class);
						Folder folder = (Folder) method.invoke(null, association.elementId );
						List<Classifiable> currentList = folderTree.get(currentRoot);
						if( currentList == null ){
							currentList = new ArrayList<Classifiable>();
							folderTree.put(currentRoot, currentList);
						}
						currentList.add(folder);
						buildFolderTree(folderTree, folder, folderAlreadyTraited);
					}
				} catch (Exception e) {
					Logger.error(e, "Error" );
				}
			}
		}else{
			folderTree.put(currentRoot, null);
		}
	}
	
	public Map<Long, Classifiable> getContentIdMap() {
		if( this.contentIdMap.isEmpty() ){
			Lock lock = new ReentrantLock();
			try{
				lock.lock();
				// init the folderIdMap ( id , folder )
				for ( Classifiable f : this.getFolderTree().keySet()){
					this.contentIdMap.put(f.getId(), f);
				}
			}finally{
				lock.unlock();
			}
		}
		return contentIdMap;
	}

	public Folder getRootFolder() {
		return rootFolder;
	}
	
	public void classify(Classifiable content , Classifiable parentFolder ){
		FolderAssociation folderAssociation = new FolderAssociation();
		folderAssociation.classOfElement = content.getClass().getName();
		folderAssociation.elementId = content.getId();
		folderAssociation.folderId = parentFolder.getId();
		folderAssociation.save();
		List<Classifiable> folderList = contentTree.get(parentFolder);
		if( folderList == null ){
			folderList = new ArrayList<Classifiable>();
			contentTree.put(parentFolder, folderList);
		}
		folderList.add(content);
		contentIdMap.put(content.getId(), content);
	}
	
	public Classifiable findFolderById(Long folderId){
		if( folderId == 0L){
			return rootFolder;
		}else{
			return contentIdMap.get(folderId);
		}
	}
	
	public List<Class<?>> getFolderClassList(){
		
		return VsboCmsPlugin.getFoldersClassList();
	}
	
	public List<Class<?>> getArticleClassList(){
		return VsboCmsPlugin.getArticlesClassList();
	}
}
