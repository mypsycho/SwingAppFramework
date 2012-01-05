/*
 * Copyright (C) 2012 Nicolas Peransin. All rights reserved.
 * Use is subject to license terms.
 */
package com.psycho.test.app;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.mypsycho.beans.InjectionTemplate;


/**
 * Class for ...
 * <p>Details</p>
 *
 * @author Peransin Nicolas
 *
 */
public class InjectionTemplateTest extends InjectionTemplate {
    
    
    
    public void reg(String n, Pattern p, String s) {
        if (s == null) {
            return;
        }

        
        Matcher matcher = p.matcher(s);
        boolean found = matcher.find();

        System.out.println(s + " == " + n + "." + found);
        if (!found) {
            return;
        }

        
        for (int i = 1; i < matcher.groupCount(); i++) {
            System.out.println(i + " == " + matcher.group(i));
        }

    }
    
    @Test
    public void tests() {
        System.out.println(REG);

        
        for (String s : new String[] {
                "%{view}",
                "%{view(mainFrame)(toolbar)}",
                "%{view=@rtds q M%}",
                "%{view{op=4235fd}}",
                "%{view{op=4235fd}{op=45fd}{op= rez f35fd | }}",
                "%{view{op=4235fd}=@rtds q M%}",
                "%{view(mainFrame)(toolbar){op=4235fd}=@rtds q M%}",
                "%{view(mainFrame)(toolbar){o1=4235fd}{o2=45fd}{o3= rez f35fd | }=@rtds q M%}",
                
        }) {

            reg("main", pattern, s);
            InjectionTemplate expr = parse(s);
            if (expr != null) {
                
                System.out.println("\tname = " + expr.getName());
                System.out.println("\tvalue = " + expr.getValue());
                if (expr.getOptions() != null) {
                    for (String key : expr.getOptions().keySet()) {
                        System.out.println("\t\t" + key + " = " + expr.getOptions().get(key));
                    }
                }
                // reg("arg", argPattern, (String) expr[2]);
                
            }
            System.out.println();

        }
        
        
    }
    
    
    
    // "%\{(\w+((\.\w+)|(\(\w+\))|(\[\d+\]))*)(\{\w+=.*\})*(\=(.*))?\}"
    
    
    
}

