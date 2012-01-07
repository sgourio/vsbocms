package models.vsbocms;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.StringUtils;

import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.vsbocms.beans.Article;
import play.modules.vsbocms.beans.Classifiable;
import play.modules.vsbocms.beans.Folder;
import play.modules.vsbocms.beans.Taggable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * A simple article is an article only define by a title and a content
 * @author Sylvain Gourio
 *
 */
@SuppressWarnings("serial")
@Entity(name="vsbocms_simplearticle")
public class SimpleArticle extends Model implements Article, Taggable{

	@Required
	public String articleName;

	@Lob
	@MaxSize(50000)
	public String content;
	
	@Override
	public String getName() {
		return articleName;
	}
	@Lob
	@MaxSize(50000)	
	private String tags;
	
	@Required
	@Temporal(TemporalType.TIMESTAMP)
	@Column(updatable = false)
	public Date creationDate;
	
	@Required
	@Temporal(TemporalType.TIMESTAMP)
	public Date modificationDate;	

	@Override
	public int compareTo(Classifiable o) {
		if( o instanceof Folder ){
			return 1;
		}
		return this.articleName.compareTo(o.getName());
	}
	
	public void setTags(String[] tags) {
		Gson gson = new Gson();
		this.tags = gson.toJson(tags);
	}

	@Override
	public Set<String> getTagSet() {
		Set<String> tagSet = new HashSet<String>();
		if( !StringUtils.isBlank( tags ) ){
			Gson gson = new Gson();
			Type typeOfCollectionOfString = new TypeToken<String[]>(){}.getType();
			String[] s = gson.fromJson(tags, typeOfCollectionOfString);
			Collections.addAll(tagSet, s);
		}
		return tagSet;
	}

	public String getTagString(){
		String retour = "";
		for(String t : getTagSet() ){
			retour += t + " ";
		}
		return retour;
	}
	
	public Date getCreationDate() {
		return creationDate;
	}

	public Date getModificationDate() {
		return modificationDate;
	}
	
	
}
