package play.modules.vsbocms.beans;

import java.util.Date;

import play.db.jpa.JPABase;

public interface Classifiable extends Comparable<Classifiable>{
	public Long getId();
	public String getName();
	public Date getCreationDate();
	public Date getModificationDate();
	public <T extends JPABase> T delete();
}
