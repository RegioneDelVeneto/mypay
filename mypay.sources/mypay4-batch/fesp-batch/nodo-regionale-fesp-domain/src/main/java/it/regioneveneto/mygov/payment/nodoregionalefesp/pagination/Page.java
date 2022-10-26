/**
 * 
 */
package it.regioneveneto.mygov.payment.nodoregionalefesp.pagination;

import java.io.Serializable;
import java.util.List;

/**
 * @author regione del veneto
 * 
 */
public class Page<E> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	private int page;

	/**
	 * 
	 */
	private int pageSize;

	/**
	 * 
	 */
	private boolean previousPage;

	/**
	 * 
	 */
	private boolean nextPage;

	/**
	 * 
	 */
	private List<E> list;

	/**
	 * 
	 */
	private int totalPages;

	/**
	 * 
	 */
	private int totalRecords;

	/**
	 * 
	 */
	public Page() {
		super();

		this.page = -1;
		this.pageSize = -1;
		this.previousPage = false;
		this.nextPage = false;
		this.list = null;
		this.totalPages = -1;
	}

	/**
	 * @param page
	 * @param pageSize
	 * @param previousPage
	 * @param nextPage
	 * @param list
	 * @param totalPages
	 */
	public Page(final int page, final int pageSize, final boolean previousPage, final boolean nextPage, final List<E> list, final int totalPages) {
		super();

		this.page = page;
		this.pageSize = pageSize;
		this.previousPage = previousPage;
		this.nextPage = nextPage;
		this.list = list;
		this.totalPages = totalPages;
	}

	/**
	 * @return the page
	 */
	public int getPage() {
		return page;
	}

	/**
	 * @param page the page to set
	 */
	public void setPage(int page) {
		this.page = page;
	}

	/**
	 * @return the pageSize
	 */
	public int getPageSize() {
		return pageSize;
	}

	/**
	 * @param pageSize the pageSize to set
	 */
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	/**
	 * @return the previousPage
	 */
	public boolean isPreviousPage() {
		return previousPage;
	}

	/**
	 * @param previousPage the previousPage to set
	 */
	public void setPreviousPage(boolean previousPage) {
		this.previousPage = previousPage;
	}

	/**
	 * @return the nextPage
	 */
	public boolean isNextPage() {
		return nextPage;
	}

	/**
	 * @param nextPage the nextPage to set
	 */
	public void setNextPage(boolean nextPage) {
		this.nextPage = nextPage;
	}

	/**
	 * @return the list
	 */
	public List<E> getList() {
		return list;
	}

	/**
	 * @param list the list to set
	 */
	public void setList(List<E> list) {
		this.list = list;
	}

	/**
	 * @return the totalPages
	 */
	public int getTotalPages() {
		return totalPages;
	}

	/**
	 * @param totalPages the totalPages to set
	 */
	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

	/**
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public int getTotalRecords() {
		return totalRecords;
	}

	public void setTotalRecords(int totalRecords) {
		this.totalRecords = totalRecords;
	}
}
