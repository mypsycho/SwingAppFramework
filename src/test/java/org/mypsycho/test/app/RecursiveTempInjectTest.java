/*
 * Copyright (C) 2012 Nicolas Peransin. All rights reserved.
 * Use is subject to license terms.
 */
package org.mypsycho.test.app;

import org.junit.Test;
import org.mypsycho.beans.Injector;
import org.mypsycho.text.TextMap;


/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Peransin Nicolas
 *
 */
public class RecursiveTempInjectTest extends Injector {

    
    static public class Bean {
        TextMap texts;
        
        
        /**
         * Returns the texts.
         *
         * @return the texts
         */
        public TextMap getTexts() {
            return texts;
        }
        
        
        /**
         * Sets the texts.
         *
         * @param texts the texts to set
         */
        public void setTexts(TextMap texts) {
            this.texts = texts;
        }
        
    }
    
    @Test
    public void testError() {
        Bean bean = inject(new Bean());
        System.out.println(bean.getTexts());
        
        
    }
    
}
