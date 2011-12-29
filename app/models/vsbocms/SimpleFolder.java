package models.vsbocms;

import javax.persistence.Entity;

import play.db.jpa.Model;
import play.modules.vsbocms.beans.Classifiable;
import play.modules.vsbocms.beans.Folder;

@SuppressWarnings("serial")
@Entity(name="vsbocms_simplefolder")
public class SimpleFolder extends Model implements Folder{

	public String folderName;

	@Override
	public String getName() {
		return folderName;
	}

	@Override
	public int compareTo(Classifiable o) {
		return o.getName().compareTo(this.folderName);
	}
	
}
