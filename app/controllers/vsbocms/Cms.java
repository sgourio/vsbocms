package controllers.vsbocms;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.modules.vsbo.Menu;
import play.modules.vsbo.SubMenu;
import play.modules.vsbocms.beans.Taggable;
import play.mvc.Before;
import services.vsbocms.CmsServices;
import beans.vsbocms.ArticleRowBean;
import beans.vsbocms.TreeNode;
import controllers.vsbo.Backoffice;

/**
 * Main controller of cms section
 * @author Sylvain Gourio
 *
 */
@Menu(id="menu-vsbocms")
public class Cms extends Backoffice{

	/**
	 * Prepare the folder tree
	 */
	@Before
	public static void prepare(){
		TreeNode root = CmsServices.getInstance().getRootNode();
		renderArgs.put("root", root);
		Set<String> existingTagSet = CmsServices.getInstance().getAllExistingTagList();
		renderArgs.put("existingTagSet", existingTagSet);
	}
	
	@SubMenu
	public static void folders(){
		render();
	}
	
	@SuppressWarnings("unchecked")
	@SubMenu
	public static void articles(String tag){
		List<ArticleRowBean> articleList = new ArrayList<ArticleRowBean>();
		if( StringUtils.isEmpty(tag) ){
			for(Class<?> clazz : CmsServices.getInstance().getTaggableClassList() ){
				try {
					Method method = clazz.getMethod("findAll");
					List<Taggable> taggableList = (List<Taggable>) method.invoke(null);
					for( Taggable t : taggableList ){
						articleList.add(new ArticleRowBean(t));
					}
				} catch (Exception e) {
					Logger.error(e, "Method findAll not found for class " + clazz.getName());
				}
			}
		}else{
			Set<Taggable> taggableList = CmsServices.getInstance().getTaggableArticleMap().get(tag);
			if( taggableList != null ){
				for( Taggable t : taggableList ){
					articleList.add(new ArticleRowBean(t));
				}
			}
		}
		
		Collections.sort(articleList);
		List<Class<?>> taggableClassList = CmsServices.getInstance().getTaggableClassList();
		
		render(articleList, taggableClassList, tag);
	}
}
