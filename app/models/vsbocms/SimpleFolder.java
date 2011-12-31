package models.vsbocms;

import javax.persistence.Entity;

import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.vsbocms.beans.Article;
import play.modules.vsbocms.beans.Classifiable;
import play.modules.vsbocms.beans.Folder;

/**
 * A simple folder is a folder only defined by its name
 * @author Sylvain Gourio
 *
 */
@SuppressWarnings("serial")
@Entity(name="vsbocms_simplefolder")
public class SimpleFolder extends Model implements Folder{

	@Required
	public String folderName;

	@Override
	public String getName() {
		return folderName;
	}

	@Override
	public int compareTo(Classifiable o) {
		if( o instanceof Article ){
			return -1;
		}
		return this.folderName.compareTo(o.getName());
	}
	
}
