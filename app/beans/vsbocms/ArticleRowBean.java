package beans.vsbocms;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import play.modules.vsbocms.beans.Taggable;
import play.mvc.Router;

public class ArticleRowBean implements Comparable<ArticleRowBean>{

	private String url = null; // url to edit
	private String name = null;
	private Date date = null;
	private String tags = null; 
	
	
	
	public ArticleRowBean(Taggable article) {
		super();
		this.name = article.getName();
		this.date = article.getModificationDate();
		String s = "";
		for( String t : article.getTagSet()){
			s += ", " + t;
		}
		if( s.startsWith(", ")){
			s = s.substring(2);
		}
		this.tags = s;
		
		String controllerName = article.getClass().getName().replace("models","controllers");
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("id", article.getId() );
		this.url = Router.reverse(controllerName +".tagEdit", parameters).url;
	}
	public String getUrl() {
		return url;
	}
	public String getName() {
		return name;
	}
	public Date getDate() {
		return date;
	}
	public String getTags() {
		return tags;
	}
	
	@Override
	public int compareTo(ArticleRowBean o) {
		if( this.date == null ){
			return 1;
		}
		if( o != null && o.date != null ){
			return o.date.compareTo(this.date);
		}
		return -1;
	}
	
	
	
	
}
