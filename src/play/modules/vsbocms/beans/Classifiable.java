package play.modules.vsbocms.beans;

import play.db.jpa.JPABase;

public interface Classifiable extends Comparable<Classifiable>{
	public Long getId();
	public String getName();
	public <T extends JPABase> T delete();
}
