package play.modules.vsbocms.beans;

import java.util.Date;
import java.util.Set;

public interface Taggable {

	public Long getId();
	public String getName();
	public Date getCreationDate();
	public Date getModificationDate();
	
	/**
	 * A set of tag
	 * @return
	 */
	public Set<String> getTagSet();
}
