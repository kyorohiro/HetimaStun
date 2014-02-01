package net.hetimatan.util.http;

import net.hetimatan.util.test.TestUtil;
import junit.framework.TestCase;

public class TestForHttpObjectHelper extends TestCase {

	@Deprecated
	public void testHttpHeaderRange() {
		{
			String input = "0-1";
			long[] expect = {0, 1};
			long[] output = HttpObjectHelper.getRange(input);
			TestUtil.assertArrayEquals(this, "", expect, output);
		}
		{
			String input = "100-200";
			long[] expect = {100, 200};
			long[] output = HttpObjectHelper.getRange(input);
			TestUtil.assertArrayEquals(this, "", expect, output);
		}
		{
			String input = "100-200,201-300";
			long[] expect = {100, 200, 201, 300};
			long[] output = HttpObjectHelper.getRange(input);
			TestUtil.assertArrayEquals(this, "", expect, output);
		}

		{
			String input = "1-";
			long[] expect = {1, Long.MAX_VALUE-1};
			long[] output = HttpObjectHelper.getRange(input);
			TestUtil.assertArrayEquals(this, "", expect, output);
		}

		{
			String input = "-";
			long[] expect = {0,Long.MAX_VALUE-1};
			long[] output = HttpObjectHelper.getRange(input);
			TestUtil.assertArrayEquals(this, "", expect, output);
		}

		{
			String input = ",";
			long[] expect = {0,Long.MAX_VALUE};
			long[] output = HttpObjectHelper.getRange(input);
			TestUtil.assertArrayEquals(this, "", expect, output);
		}

		{
			String input = "a-0";
			long[] expect = {0,Long.MAX_VALUE-1};
			long[] output = HttpObjectHelper.getRange(input);
			TestUtil.assertArrayEquals(this, "", expect, output);
		}

	}
}
