package org.cytoscape.property.bookmark;

import java.util.List;
/**
 * A set of utility methods to manipulate the bookmarks
 */
public interface BookmarksUtil {

	/**
	 * Traverse bookmark tree and get a list of data sources from the specified
	 * category.
	 * 
	 * @param categoryName
	 * @return
	 */
	public List<DataSource> getDataSourceList(String categoryName,
			List<Category> categoryList);

	/**
	 * Select specific category from a list of categories.
	 * 
	 * @param categoryName
	 * @param categoryList
	 * @return
	 */
	public Category getCategory(String categoryName, List<Category> categoryList);

	/**
	 * Given the attribute name, return the value in a bookmark 
	 * 
	 * @param source a bookmark object
	 * @param attrName an attribute name
	 * 
	 * @return The value related to the attribute name
	 */
	public String getAttribute(DataSource source, String attrName);

	/**
	 * Store a bookmark object in bookmarks object
	 * 
	 * @param pBookmarks bookmarks object
	 * @param pCategoryName category name
	 * @param pDataSource a single bookmark
	 */
	public void saveBookmark(Bookmarks pBookmarks, String pCategoryName,
			DataSource pDataSource);

	/**
	 * Delete a bookmark (pDataSource) from the categoty (pCategoryName) in the bookmarks object (pBookmarks)
	 * 
	 * @param pBookmarks Bookmark object, which hold a set of bookmark
	 *  
	 * @param pCategoryName category name
	 * 
	 * @param pDataSource a single bookmark object
	 * 
	 * @return True if the bookmark is deleted successfully, False otherwise.
	 */
	public boolean deleteBookmark(Bookmarks pBookmarks, String pCategoryName,
			DataSource pDataSource);

	/**
	 * Check if a bookmark is in the bookmarks.
	 * 
	 * @param pBookmarks bookmarks object
	 * @param pCategoryName category name
	 * @param pDataSource a bookmark
	 * 
	 * @return True if the bookmark is in bookmarks, False otherwise
	 */
	public boolean isInBookmarks(Bookmarks pBookmarks, String pCategoryName,
			DataSource pDataSource);

}