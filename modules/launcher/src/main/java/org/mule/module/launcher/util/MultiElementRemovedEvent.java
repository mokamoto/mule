/*
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.launcher.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
*
*/
public class MultiElementRemovedEvent extends ElementEvent {

    private List values = new ArrayList();

    public MultiElementRemovedEvent(Object source, List values) {
        super(source, OLDVALUE, NEWVALUE, ElementEvent.MULTI_ADD, 0);
        if (values != null) {
            this.values.addAll(values);
        }
    }

    public List getValues() {
        return Collections.unmodifiableList(values);
    }
}
