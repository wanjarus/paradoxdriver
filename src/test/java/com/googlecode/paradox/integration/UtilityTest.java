package com.googlecode.paradox.integration;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.googlecode.paradox.utils.DateUtils;
import com.googlecode.paradox.utils.StringUtils;
import com.googlecode.paradox.utils.TestUtil;
import com.googlecode.paradox.utils.Utils;

/**
 * Generic tests for all utility classes.
 *
 * @author Leonardo Alves da Costa
 * @since 1.2
 * @version 1.0
 */
@Category(IntegrationTest.class)
public class UtilityTest {

    @Test
    public void testClassesIntegrity() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        TestUtil.assertUtilityClassWellDefined(Utils.class);
        TestUtil.assertUtilityClassWellDefined(StringUtils.class);
        TestUtil.assertUtilityClassWellDefined(DateUtils.class);
    }
}