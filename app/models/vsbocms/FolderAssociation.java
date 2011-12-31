package models.vsbocms;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import play.data.validation.Required;
import play.db.jpa.Model;

/**
 * Association between a classifiable object and a folder
 * @author Sylvain Gourio
 *
 */
@SuppressWarnings("serial")
@Entity(name="vsbocms_folder_association")
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"folderId","elementId","classOfElement"}))
public class FolderAssociation extends Model{
	
	/**
	 * if folderId is 0, means that the element is placed on the root element
	 * if folderId is null the element is not classified
	 */
	@Required
	public Long folderId;
	@Required
	public String classOfFolder;
	
	@Required
	public Long elementId;

	@Required
	public String classOfElement;	
	
	public static List<FolderAssociation> findByFolderId(Long folderId, String classOfFolder){
		return find("byFolderIdAndClassOfFolder", folderId, classOfFolder).fetch();
	}
	public static List<FolderAssociation> findByElementId(Long elementId, String classOfElement){
		return find("byElementIdAndClassOfElement", elementId, classOfElement).fetch();
	}
}