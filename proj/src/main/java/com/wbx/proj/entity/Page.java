package com.wbx.proj.entity;

public class Page {

    private int current = 1;
    private int limit = 10;
    private int rows;
    private String path;

    public void setCurrent(int current) {
        if (current > 0) {
            this.current = current;
        }
    }

    public void setLimit(int limit) {
        if (limit > 0 && limit <= 20)  {
            this.limit = limit;
        }
    }

    public void setRows(int rows) {
        if (rows > 0) {
            this.rows = rows;
        }
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getCurrent() {
        return current;
    }

    public int getLimit() {
        return limit;
    }

    public int getRows() {
        return rows;
    }

    public String getPath() {
        return path;
    }

    public int getTotal() {
        return (rows + limit - 1) / limit;
    }

    public int getOffset() {
        return (current - 1) * limit;
    }

    public int getFrom() {
        return Math.max(current - 2, 1);
    }

    public int getTo() {
        return Math.min(current + 2, this.getTotal());
    }


}
