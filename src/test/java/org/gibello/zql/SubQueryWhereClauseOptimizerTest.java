package org.gibello.zql;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.gibello.zql.sanity.ZQLTestCase;
import org.junit.Test;

public class SubQueryWhereClauseOptimizerTest extends ZQLTestCase {

	@Test
	public void subQueryWhereClauseOptimizerCheck1() throws ParseException, IOException {
		String from = FileUtils.readFileToString(new File("src/test/resources/sqlFrom.txt"), Charset.defaultCharset());
		String where = FileUtils.readFileToString(new File("src/test/resources/sqlWhere.txt"), Charset.defaultCharset());
		SubQueryWhereClauseOptimizer opt = new SubQueryWhereClauseOptimizer(from, where);
		opt.optimizeQuery();
		assertEquals(opt.getWhere(),
				"((large_table.small_ref_id = small_table.id) AND (large_table.mid_ref_id = mid_table.id) AND (small_table.group_name = 'MyGroup') AND (((large_table.date_time1 BETWEEN '2010-01-01' AND '2017-01-01') AND (mid_table.type = 'Type1')) OR (large_table.id IN (select large_table.id from other_table where ((large_table.date_time2 BETWEEN '2010-06-01' AND '2017-01-01') AND (mid_table.type = 'Type2')))) OR ((mid_table.date_time3 BETWEEN '2010-08-01' AND '2017-01-01') AND (mid_table.type = 'Type3'))))");
	}

}
