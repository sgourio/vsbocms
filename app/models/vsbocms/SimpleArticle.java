package models.vsbocms;

import javax.persistence.Entity;
import javax.persistence.Lob;

import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.vsbocms.beans.Article;
import play.modules.vsbocms.beans.Classifiable;
import play.modules.vsbocms.beans.Folder;

/**
 * A simple article is an article only define by a title and a content
 * @author Sylvain Gourio
 *
 */
@SuppressWarnings("serial")
@Entity(name="vsbocms_simplearticle")
public class SimpleArticle extends Model implements Article{

	@Required
	public String articleName;
	@Required
	public String title;
	@Lob
	@MaxSize(50000)
	public String content;
	
	@Override
	public String getName() {
		return articleName;
	}

	@Override
	public int compareTo(Classifiable o) {
		if( o instanceof Folder ){
			return 1;
		}
		return this.articleName.compareTo(o.getName());
	}

	
}
