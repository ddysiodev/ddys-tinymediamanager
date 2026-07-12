package org.ddys.tinymediamanager;

import java.util.ArrayList;
import java.util.List;

final class DdysModels {
  private DdysModels() {
  }

  static final class Movie {
    String slug = "";
    String title = "";
    String originalTitle = "";
    String url = "";
    String poster = "";
    String fanart = "";
    String releaseDate = "";
    String overview = "";
    String typeName = "";
    String remarks = "";
    String runtime = "";
    int    year;
    float  rating;

    final List<String> countries = new ArrayList<>();
    final List<String> directors = new ArrayList<>();
    final List<String> actors    = new ArrayList<>();
    final List<String> tags      = new ArrayList<>();

    boolean isEmpty() {
      return slug.isBlank() && title.isBlank();
    }
  }

  static final class Resource {
    String name = "";
    String url = "";
    String groupName = "";
    String code = "";
    boolean direct;

    String label() {
      String label = groupName.isBlank() ? name : groupName + " - " + name;
      if (label.isBlank()) {
        label = url;
      }
      if (!code.isBlank() && !label.contains(code)) {
        label = label + " 提取码 " + code;
      }
      return label;
    }
  }
}
