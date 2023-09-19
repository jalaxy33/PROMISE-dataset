package net.sourceforge.pbeans.servlet;

import java.util.*;

/**
 * @deprecated not used
 * @hidden
 */
class UpdateResult {
    private final Collection exceptions;

    UpdateResult(Collection exceptions) {
        this.exceptions = exceptions;
    }

    public boolean wasSuccessful() {
        return this.exceptions.size() == 0;
    }

    public Iterator getExceptions() {
        return this.exceptions.iterator();
    }
}
