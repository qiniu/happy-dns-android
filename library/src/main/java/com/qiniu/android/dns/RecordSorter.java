package com.qiniu.android.dns;

/**
 * Created by bailong on 15/7/24.
 */
public interface RecordSorter {
    Record[] sort(Record[] records);
}
