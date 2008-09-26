package net.sourceforge.pmd.eclipse.ui.preferences;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import net.sourceforge.pmd.Rule;

/**
 * This class implements a content provider for the rule properties table of
 * the PMD Preference page
 * 
 * @author Philippe Herlin
 * @version $Revision$
 * 
 * $Log$
 * Revision 1.1  2006/05/22 21:23:41  phherlin
 * Refactor the plug-in architecture to better support future evolutions
 *
 * Revision 1.1  2003/06/30 20:16:06  phherlin
 * Redesigning plugin configuration
 *
 *
 */
public class RulePropertiesContentProvider extends AbstractStructuredContentProvider {

    /**
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(Object)
     */
    public Object[] getElements(Object inputElement) {
        Object[] result = new Object[0];
        
        if (inputElement instanceof Rule) {
            Rule rule = (Rule) inputElement;
            Enumeration keys = rule.getProperties().keys();
            List propertyList = new ArrayList();
            while (keys.hasMoreElements()) {
                propertyList.add(new RuleProperty(rule, (String) keys.nextElement()));
            }
            result = propertyList.toArray();
        }
        
        return result;
    }
}