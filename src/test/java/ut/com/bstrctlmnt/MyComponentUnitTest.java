package ut.com.bstrctlmnt;

import org.junit.Test;
import com.bstrctlmnt.api.MyPluginComponent;
import com.bstrctlmnt.impl.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}