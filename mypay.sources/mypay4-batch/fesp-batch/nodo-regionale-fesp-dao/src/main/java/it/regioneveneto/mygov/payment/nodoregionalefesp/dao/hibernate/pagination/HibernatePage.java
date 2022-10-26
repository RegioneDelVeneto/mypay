/**
 * 
 */
package it.regioneveneto.mygov.payment.nodoregionalefesp.dao.hibernate.pagination;

import it.regioneveneto.mygov.payment.nodoregionalefesp.pagination.Page;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Criteria;
import org.springframework.util.Assert;

/**
 * @author regione del veneto
 * 
 */
public class HibernatePage<E> extends Page<E> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param query
	 *            HQL query to execute and paginate
	 * @param page
	 *            page number starting from 0
	 * @param pageSize
	 *            number of entities per page
	 */
	@SuppressWarnings("unchecked")
	public HibernatePage(final Criteria criteria, final int page, final int pageSize) {
		super();

		Assert.notNull(criteria, "query parameter must not be null");
		Assert.isTrue((page >= 0), "page parameter must not be negative");
		Assert.isTrue((pageSize >= 0), "pageSize parameter must not be negative");

		setPage(page);
		setPageSize(pageSize);
		setPreviousPage(page > 1);
		List<E> results = criteria.setFirstResult((page - 1) * pageSize).setMaxResults(page * (pageSize) + 1).list();

		List<E> list = new ArrayList<E>();

		Iterator<E> resultsIterator = results.iterator();
		while (resultsIterator.hasNext()) {
			Object result = resultsIterator.next();
			if ((result instanceof Object[]) && (((Object[]) result).length == 1))
				list.add((E) ((Object[]) result)[0]);
			else
				list.add((E) result);
		}

		setList(results.size() > pageSize ? list.subList(0, pageSize) : results);
	}

	/**
	 * @param query
	 * @param page
	 * @param pageSize
	 * @param countQuery
	 */
	public HibernatePage(final Criteria query, final int page, final int pageSize, final Criteria countQuery) {
		this(query, page, pageSize);

		Assert.notNull(countQuery, "count query parameter must not be null");

		Object res = countQuery.uniqueResult();
		if (res == null) {
			setTotalRecords(0);
			setTotalPages(0);
		} else {

			int numElem = ((Long) res).intValue();

			setTotalRecords(numElem);
			if (numElem % getPageSize() == 0)
				setTotalPages(numElem / getPageSize());
			else
				setTotalPages((numElem / getPageSize()) + 1);

			setNextPage(page + 1 <= getTotalPages());
		}
	}

}
