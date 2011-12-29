package models.vsbocms;

import java.util.List;

import javax.persistence.Entity;

import play.db.jpa.Model;

@SuppressWarnings("serial")
@Entity
public class FolderAssociation extends Model{

	public Long elementId;
	/**
	 * if folderId is 0, means that the element is placed on the root element
	 * if folderId is null the element is not classified
	 */
	public Long folderId;
	public String classOfElement;	
	
	public static List<FolderAssociation> findByFolderId(Long folderId){
		return find("byFolderId", folderId).fetch();
	}
}