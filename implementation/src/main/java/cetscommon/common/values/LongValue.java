package cetscommon.common.values;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;

import static cetscommon.util.Config.LongValueComparator;

public class LongValue extends Value {

    protected LongValue(double value) {
        super(value);
    }

    @Override
    public int compareTo(@Nonnull Value o) {
        Preconditions.checkNotNull(o);
        if (o instanceof LongValue) {
            return new LongValueComparator().compare(this, (LongValue) o);
        } else {
            throw new IllegalArgumentException("must be long");
        }
    }

    public void add(LongValue nv) {
        this.value = this.longVal() + nv.longVal();
    }

    @Override
    public String strVal() {
        throw new UnsupportedOperationException("not a str value");
    }
}


