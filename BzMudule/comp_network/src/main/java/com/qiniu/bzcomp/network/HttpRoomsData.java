package com.qiniu.bzcomp.network;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.util.List;

public class HttpRoomsData <T> implements Serializable {

    private String total;

    private List<T> rooms;
    private  String nextId;
    private String cnt;
    private int nextPageNum;
    private int pageSize;
    private int currentPageNum;
    private boolean endPage = false;

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getCnt() {
        return cnt;
    }

    public void setCnt(String cnt) {
        this.cnt = cnt;
    }

    public List<T> getRooms() {
        return rooms;
    }

    public void setRooms(List<T> rooms) {
        this.rooms = rooms;
    }

    public String getNextId() {
        return nextId;
    }

    public void setNextId(String nextId) {
        this.nextId = nextId;
    }

    public int getNextPageNum() {
        return nextPageNum;
    }

    public void setNextPageNum(int nextPageNum) {
        this.nextPageNum = nextPageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getCurrentPageNum() {
        return currentPageNum;
    }

    public void setCurrentPageNum(int currentPageNum) {
        this.currentPageNum = currentPageNum;
    }

    public boolean isEndPage() {
        return endPage;
    }

    public void setEndPage(boolean endPage) {
        this.endPage = endPage;
    }
}
