package models.vsbocms;

import javax.persistence.Entity;

import play.db.jpa.Model;
import play.modules.vsbocms.beans.Article;
import play.modules.vsbocms.beans.Classifiable;

@SuppressWarnings("serial")
@Entity(name="vsbocms_simplearticle")
public class SimpleArticle extends Model implements Article{

	public String articleName;
	public String title;
	public String content;
	
	@Override
	public String getName() {
		return articleName;
	}

	@Override
	public int compareTo(Classifiable o) {
		return o.getName().compareTo(this.articleName);
	}

	
}
