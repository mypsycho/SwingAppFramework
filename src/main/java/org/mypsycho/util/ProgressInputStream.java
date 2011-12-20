/*
 * Copyright (C) 2011 Peransin Nicolas.
 * Use is subject to license terms.
 */
package org.mypsycho.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * XXX Doc
 * <p>Detail ... </p>
 * @author Peransin Nicolas
 */
public class ProgressInputStream extends FilterInputStream {
    long count = 0;
    // int mark; do not what to do with mark
    
    public ProgressInputStream(InputStream in) {
        super(in);
    }
    
    static final int EOS = -1;
    public int read() throws IOException {
        int read = in.read();
        if (read != EOS)
            count++;
        return in.read();
    }
    
    public int read(byte b[], int off, int len) throws IOException {
        int read = in.read(b, off, len);
        if (read != EOS)
            count += read;
        return read;
    }


    public long skip(long n) throws IOException {
        long skip = in.skip(n);
        if (skip > 0)
            count += skip;
        return skip;
    }
    
    
    public long getCount() {
        return count;
    }
}
