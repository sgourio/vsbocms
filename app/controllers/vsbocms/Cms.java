package controllers.vsbocms;

import play.modules.vsbo.Menu;
import play.modules.vsbo.SubMenu;
import play.mvc.Before;
import services.vsbocms.CmsServices;
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
	}
	
	@SubMenu
	public static void index(){
		render();
	}
	
}
