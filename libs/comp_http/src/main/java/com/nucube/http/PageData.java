package com.nucube.http;

import java.util.List;

public class PageData<T> {

    public int total_count;
    public int page_total;
    public int end_page;
    public List<T> list;

}
