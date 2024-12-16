package com.github.muhammadabdurrahman.webcrawler.persistence.logging;

import com.p6spy.engine.spy.appender.MultiLineFormat;

public class SuppressedP6SpyMultiLineFormat extends MultiLineFormat {

  @Override
  public String formatMessage(int connectionId, String now, long elapsed, String category, String prepared, String sql, String url) {
    if (sql != null) {
      sql = sql.replaceAll("visited_pages='.*'", "visited_pages='[SUPPRESSED]'");
      sql = sql.replaceAll("(?i)(insert into crawl_result \\(created_at,ttl,visited_pages,starting_url\\) values \\('[^']*',\\d+),('[^']*'),", "$1,'[SUPPRESSED]',");    }
    return super.formatMessage(connectionId, now, elapsed, category, prepared, sql, url);
  }
}
