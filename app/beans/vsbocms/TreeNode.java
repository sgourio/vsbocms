package beans.vsbocms;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.Logger;
import play.modules.vsbocms.beans.Article;
import play.modules.vsbocms.beans.Classifiable;
import play.modules.vsbocms.beans.Folder;
import play.mvc.Router;

/**
 * Represent a node of the tree
 * @author Sylvain Gourio
 *
 */
public class TreeNode {
	private Long associationId = null ;
	private Classifiable classifiable = null;
	private String url = null; // url to edit
	
	private TreeNode father = null; 
	private List<TreeNode> children = new ArrayList<TreeNode>();

	public TreeNode(Long associationId, Classifiable classifiable, TreeNode father) {
		this.associationId = associationId;
		this.classifiable = classifiable;
		this.father = father;
		
		String controllerName = classifiable.getClass().getName().replace("models","controllers");
		Map<String, Object> parameters = new HashMap<String, Object>();
		if( father != null ){
			parameters.put("fatherNodeId", father.associationId);
		}
		parameters.put("treeNodeId", this.getAssociationId());
		this.url = Router.reverse(controllerName +".edit", parameters).url;
	}
	
	public Classifiable getClassifiable() {
		return classifiable;
	}
	public Classifiable getClassifiableFromDatabase(){
		if( this.classifiable != null ){
			Classifiable cl = null;
			try {
				Method method = this.getClassifiable().getClass().getMethod("findById", Object.class);
				cl = (Classifiable) method.invoke(null, this.classifiable.getId() );
			} catch (Exception e) {
				Logger.error(e,"");
			}
			return cl;
		}else{
			return null;
		}
	}
	
	public boolean isFolder(){
		return this.classifiable != null && this.classifiable instanceof Folder;
	}
	
	public boolean isArticle(){
		return this.classifiable != null && this.classifiable instanceof Article;
	}

	public void setClassifiable(Classifiable classifiable) {
		this.classifiable = classifiable;
	}

	public TreeNode getFather() {
		return father;
	}

	public String getUrl() {
		return url;
	}

	public List<TreeNode> getChildren() {
		return children;
	}

	public Long getAssociationId() {
		return associationId;
	}
	public Long getId() {
		return getAssociationId();
	}
	
	public String getName(){
		return this.classifiable.getName();
	}
	
	@Override
	public boolean equals(Object obj) {
		if( obj instanceof TreeNode){
			TreeNode tn = (TreeNode) obj;
			return this.associationId == tn.associationId;
		}
		return false;
	}
}
