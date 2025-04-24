package com.eimp.util;

public class SortOrder {
    private int op;
    private int oldOp;
    private String nowString;
    public SortOrder() {
        op = 1;
        oldOp = op;
        nowString = ASC_SORT_BY_NAME;
    }

    /**
     * 按名称升序
     */
    public static final String ASC_SORT_BY_NAME = "按名称升序";

    /**
     * 按名称降序
     */
    public static final String DESC_SORT_BY_NAME = "按名称降序";

    /**
     * 按时间升序
     */
    public static final String ASC_SORT_BY_TIME = "按时间升序";

    /**
     * 按时间降序
     */
    public static final String DESC_SORT_BY_TIME = "按时间降序";

    /**
     * 按大小升序
     */
    public static final String ASC_SORT_BY_SIZE = "按大小升序";

    /**
     * 按大小降序
     */
    public static final String DESC_SORT_BY_SIZE = "按大小降序";

    public void setOp(int op) {
        this.op = op;
    }

    public int getOp() {
        return op;
    }

    public void setNowString() {
        switch(op) {
            case 1:
                nowString = ASC_SORT_BY_NAME;
                break;
            case 2:
                nowString = DESC_SORT_BY_NAME;
                break;
            case 3:
                nowString = ASC_SORT_BY_TIME;
                break;
            case 4:
                nowString = DESC_SORT_BY_TIME;
                break;
            case 5:
                nowString = ASC_SORT_BY_SIZE;
                break;
            case 6:
                nowString = DESC_SORT_BY_SIZE;
                break;
        }
    }

    public String getNowString() {
        return nowString;
    }

    public int getOldOp() {
        return oldOp;
    }

    public void setOldOp(int oldOp) {
        this.oldOp = oldOp;
    }
}
