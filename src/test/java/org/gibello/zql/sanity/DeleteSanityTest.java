package org.gibello.zql.sanity;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.gibello.zql.ParseException;
import org.gibello.zql.statement.ZStatement;
import org.junit.Test;

/**
 * @author Bogdan Mariesan, Romania, on 14-10-2015
 */
public class DeleteSanityTest extends ZQLTestCase {

	@Test
	public void deleteSanityCheck1() throws ParseException, IOException {
		final List<ZStatement> statements = super.parseSQL("DELETE FROM ANTIQUES WHERE ITEM = 'Ottoman';");
		assertEquals("delete from ANTIQUES where (ITEM = 'Ottoman')", statements.get(0).toString());
	}

	@Test
	public void deleteSanityCheck2() throws ParseException, IOException {
		final List<ZStatement> statements = super.parseSQL("DELETE FROM ANTIQUES WHERE ITEM = 'Ottoman' AND BUYERID = 01 AND SELLERID = 21;");
		assertEquals("delete from ANTIQUES where ((ITEM = 'Ottoman') AND (BUYERID = 01) AND (SELLERID = 21))", statements.get(0).toString());
	}
}
